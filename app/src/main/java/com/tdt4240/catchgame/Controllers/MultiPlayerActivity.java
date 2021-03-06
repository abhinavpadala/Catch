package com.tdt4240.catchgame.Controllers;

import android.app.Activity;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
//Quick game
import android.view.WindowManager;

//Google Sign In feature
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//Quick Game
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.Games;
import com.tdt4240.catchgame.R;
import com.tdt4240.catchgame.View.GameView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultiPlayerActivity extends AppCompatActivity implements
        View.OnClickListener {


    final static String TAG = "Catch";
    private static final int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    private GoogleSignInClient mGoogleSignInClient = null;
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;
    String mRoomId = null;
    RoomConfig mRoomConfig;

    // Multiplayer?
    boolean mMultiplayer = false;

    // Participants in the game
    ArrayList<Participant> mParticipants = null;

    //My participant Id in the currently active game
    String mMyId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[7];

    private String background;
    private String avatar;

    // Music
    private MediaPlayer backgroundMusic;
    private MediaPlayer buttonSound;
    private boolean soundEffectsOn;
    private boolean backgroundsoundOn;
    private boolean inGame;

    // Broadcast vars
    private int opponentScore;
    private int opponentLife;
    private int isGameOver;
    private int powerup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player);
        findViewById(R.id.view_signIn).setVisibility(View.VISIBLE);
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        for (int id : CLICKABLEs) {
            findViewById(id).setOnClickListener(this);
            System.out.println("-------Button Id--" + id);
        }

        // Enabled in quickGame()
        this.inGame = false;

        // Button sound
        this.buttonSound = MediaPlayer.create(this, R.raw.buttonclick);
        this.buttonSound.setVolume(1, 1);

        // Background sound
        this.backgroundMusic = MediaPlayer.create(this, R.raw.test_song);
        this.backgroundMusic.setVolume(1, 1);
        this.backgroundMusic.setLooping(true);

        this.backgroundsoundOn = getIntent().getExtras().getBoolean("backgroundSound");
        this.soundEffectsOn = getIntent().getExtras().getBoolean("soundEffects");

        this.background = getIntent().getStringExtra("background");
        this.avatar = getIntent().getStringExtra("avatar");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!this.inGame){
            if(mRealTimeMultiplayerClient==null){
                findViewById(R.id.button_quick_game).setVisibility(View.INVISIBLE);
                findViewById(R.id.button_sign_out).setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!this.inGame) {
            this.buttonSound.release();
            if(mRealTimeMultiplayerClient!=null){
                findViewById(R.id.button_sign_in).setVisibility(View.INVISIBLE);
            }
        }

        if(this.inGame) this.backgroundMusic.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!this.inGame){
            this.buttonSound = MediaPlayer.create(this, R.raw.buttonclick);
            this.buttonSound.setVolume(1, 1);
            signInSilently();
            if(mRealTimeMultiplayerClient!=null){
                findViewById(R.id.button_sign_in).setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        startActivity(new Intent(this, MenuActivity.class));
        this.backgroundMusic.release();
        this.buttonSound.release();
    }

    public void backgroundMusicOn() {
        this.backgroundMusic.start();
    }

    public void backgroundMusicOff() {
        this.backgroundMusic.pause();
    }

    public boolean getSoundEffectsOn(){
        return this.soundEffectsOn;
    }

    public boolean getBackgroundSoundOn(){
        return this.backgroundsoundOn;
    }


    public String getDifficulty() {
        return "medium";
    }


    public String getBackground(){
        return this.background;
    }
    public String getAvatar(){
        return this.avatar;
    }

    //Clickable buttons
    final static int[] CLICKABLEs = {
            R.id.button_sign_in, R.id.button_sign_out, R.id.button_quick_game, R.id.btn_mpgGoBack
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                this.buttonSound.start();
                // start the sign-in flow
                Log.d(TAG, "-----------Sign-in button clicked");
                startSignInIntent();
                break;
            case R.id.button_sign_out:
                this.buttonSound.start();
                startActivity(new Intent(this, MenuActivity.class));
                signOut();
                break;
            case R.id.button_quick_game:
                this.buttonSound.start();
                startQuickGame();
                break;
            case R.id.btn_mpgGoBack:
                this.buttonSound.start();
                startActivity(new Intent(this, MenuActivity.class));
                break;
        }

    }

    // Quick game with auto match
    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);
        switchToScreen(R.id.screen_wait);
        findViewById(R.id.btn_mpgGoBack).setVisibility(View.INVISIBLE);
        keepScreenOn();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    // Screens for multiplayer
    final static int[] SCREENS = {
            R.id.view_signIn, R.id.screen_wait
    };

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }

    }

    public void startSignInIntent() {
        Log.d(TAG, "--------------Start sign in Intent");
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    public void signInSilently(){
        Log.d(TAG, "---------signInsilently()");
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "------------signInsilently() success");
                            isSignedIn(true);
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "------------signInsilently() Faieled");
                            //onDisconnected();
                        }
                    }
                });
    }

    public void isSignedIn(boolean isSignedIn){
        if(isSignedIn == true){
            findViewById(R.id.button_sign_in).setVisibility(View.INVISIBLE);
            findViewById(R.id.button_quick_game).setVisibility(View.VISIBLE);
            findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "signout(): success");
                        } else {
                            Log.d(TAG, "signout() failed");
                        }
                        mRealTimeMultiplayerClient = null;
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, " account " + account);
                findViewById(R.id.button_quick_game).setVisibility(View.VISIBLE);
                findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    Log.d(TAG, "Message from onActivityResult catch");
                }
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            Log.d(TAG, "Select players request code");
        } else if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                startGame();
                //getResources().getIdentifier(fname, "raw", getPackageName());
            }
        } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM){
            leaveRoom();
        } else if (resultCode == Activity.RESULT_CANCELED){
            leaveRoom();
        }
        Log.d(TAG, "------------if statement failed in onActivityResult");
        super.onActivityResult(requestCode, resultCode, intent);
    }


    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private String mPlayerId;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): Connected to Google Api's - Account " + googleSignInAccount);
        if (mSignedInAccount != googleSignInAccount) {
            mSignedInAccount = googleSignInAccount;
            //update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            Log.d(TAG, "Real time multiple client " + mRealTimeMultiplayerClient);

            // get the players from the PlayerClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Player client failed");
                        }
                    });
        }
    }

    void showWaitingRoom(Room room) {
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        //show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "show waiting room failed");
                    }
                });
    }

    void leaveRoom(){
        Log.d(TAG, "----------Leaving room");
        mRoomConfig = null;
        mRoomId = null;
        switchToScreen(R.id.view_play);
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onRoomConnecting(@Nullable Room room) {

        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {

        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            Log.d(TAG, "-------onConnectedToRoom from RoomStatusUpdateCallback");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "-----------------<< CONNECTED TO ROOM>>");
            for (Participant p : mParticipants) {
                Log.d(TAG, "mParticipants " + p.getDisplayName());

            }

        }


        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {

        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {

        }

        @Override
        public void onP2PConnected(@NonNull String s) {

        }

        @Override
        public void onP2PDisconnected(@NonNull String s) {

        }
    };
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "------------RoomUpdateCallback onRoomCreate()");
            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();
            showWaitingRoom(room);

        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "--------------onJoined room");

        }

        @Override
        public void onLeftRoom(int i, @NonNull String s) {

        }

        @Override
        public void onRoomConnected(int i, @Nullable Room room) {

        }
    };

    void startGame() {
        this.inGame = true;
        this.backgroundMusic.start();
        setContentView(new GameView(this, this));
    }


    //Score SinglePlayer of other participants. We update this as we receive their scores from the network
    Map<String, Integer> mParticipantScore = new HashMap<>();

    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();
            Log.d(TAG, "Message received: " + (char) buf[0] + " Score : " + (int) buf[1] + "Lives : " + (int) buf[2] + " isGameover: " + (int) buf[3] + " Powerup: "+ (int) buf[4]);
            setOpponentScore(calculateActualScore(buf[5], buf[6]));
            setOpponentLife(buf[2]);
            setIsGameOver(buf[3]);
            setPowerup(buf[4]);
        }
    };

    private int calculateActualScore(byte byte1, byte byte2){
        int actualScore = byte1 + (byte2 * 128);
        if (actualScore < 0) {
            return actualScore * -1 ;
        }
        else {
            return actualScore;
        }
    }

    // Broadcast my score to everybody else
    public void broadcast(int myScore, int myLives, int isGameOver, int powerup) {

        if (myLives == 0) {
            isGameOver = 1;
        }

        //First byte in message indicates whether it's final score or not
        mMsgBuf[0] = 'U';

        //Second byte is the score
        //mMsgBuf[1] = (byte) CoreGame.pScore;
        mMsgBuf[1] = (byte) myScore;
        mMsgBuf[2] = (byte) myLives;
        mMsgBuf[3] = (byte) isGameOver; //1: True, 0: False
        mMsgBuf[4] = (byte) powerup;
        mMsgBuf[5] = (byte) (myScore % 128);
        mMsgBuf[6] = (byte) (myScore / 128);

        //send to every participant
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (true) {
                // interim score
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId, p.getParticipantId());
            }
        }
    }


    public int getOpponentScore() { return this.opponentScore; }

    public void setOpponentScore(int score) { this.opponentScore = score; }

    public int getOpponentLife() { return this.opponentLife; }

    public void setOpponentLife(int lives) { this.opponentLife = lives; }

    public int getIsGameOver() { return this.isGameOver; }

    public void setIsGameOver(int isGameOver) { this.isGameOver = isGameOver; }

    public int getPowerup() { return this.powerup; }

    public void setPowerup(int powerup) { this.powerup= powerup; }


    //Keeps the screen turned on
    void keepScreenOn() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}

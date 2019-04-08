package com.tdt4240.catchgame;

import android.app.Activity;
import android.nfc.Tag;
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
import com.google.android.gms.games.GamesCallbackStatusCodes;
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
import  com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;




import  java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultiPlayerActivity extends AppCompatActivity implements
        View.OnClickListener {


    final static String TAG = "Catch";


    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    //Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the real time multi player system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;


    //Room Id where the currently active game is taking place; null if we're not playing
    String mRoomId = null;

    //Holds the configuration of the current room
    RoomConfig mRoomConfig;

    // Multiplayer?
    boolean mMultiplayer = false;

    // Participants in the game
    ArrayList<Participant> mParticipants = null;

    //My participant Id in the currently active game
    String mMyId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player);
        findViewById(R.id.view_signIn).setVisibility(View.VISIBLE);

        //GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        // Create the client used to sign in
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        //Set up a click listener for everything
        for(int id: CLICKABLEs){
            findViewById(id).setOnClickListener(this);
            System.out.println("-------Button Id--" + id);
        }
    }

    public String getDifficulty(){
        return "easy";
    }

    public String getGametype(){ return "multi";}

    //Clickable buttons
    final static int[] CLICKABLEs = {
            R.id.button_sign_in, R.id.button_sign_out, R.id.button_quick_game
    };


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_sign_in:
                // start the sign-in flow
                Log.d(TAG, "-----------Sign-in button clicked");
                startSignInIntent();
                break;
            case R.id.button_sign_out:
                signOut();
                break;
            case R.id.button_quick_game:
                startQuickGame();
                break;
        }

    }

    // Quick game with auto match
    void startQuickGame(){
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();

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

    void switchToScreen(int screenId){
        // make the requested screen visible; hide all others
        for(int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE: View.GONE);
        }

    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent(){
        Log.d(TAG, "--------------Start sign in Intent");
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    private void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, " ----------Signed out successfully");
                                } else {
                                    Log.d(TAG, " ----------Signed out failed");
                                }
                            }
                        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        Log.d(TAG, "---------------- requestcode: " + requestCode);
        if(requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);
            Log.d(TAG, "--------------- task " + task);
           // handleSignInResult(task);
            try {
                Log.d(TAG, "--------------- try statement");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "--------------- account " + account);
                onConnected(account);
            } catch (ApiException apiException){
                String message = apiException.getMessage();
                if(message == null || message.isEmpty()) {
                    //message = getString(R.string.signin_other_error);
                    Log.d(TAG, "-------- Message from onActivityResult catch");
                }
            }
        } else if(requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI - ready to create the room
            //handleSelectPlayersResult(resultCode, intent);
            Log.d(TAG, "-------------Select players request code");
        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if(resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "--------Starting game (waiting room returned OK).");
                //start game here startGame(true);
                startGame();
            }
        }
        Log.d(TAG, "------------if statement failed in onActivityResult");
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.

    /*private void handleSelectPlayersResult(int response, Intent data) {
        if(response != Activity.RESULT_OK) {
            Log.w(TAG, "-------------Select players UI canceled, " + response);
            return;
        }
        Log.d(TAG, "Select players UI succeeded");

    } */


    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private String mPlayerId;
    private void onConnected(GoogleSignInAccount googleSignInAccount){
        Log.d(TAG, "---------onConnected(): Connected to Google Api's - Account " + googleSignInAccount );
        if(mSignedInAccount != googleSignInAccount) {
            mSignedInAccount = googleSignInAccount;
            Log.d(TAG, "------Crashing here.");
            //update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            Log.d(TAG, "------------Real time multiple client " + mRealTimeMultiplayerClient);

            // get the players from the PlayerClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();

                            //switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "-----------Player client failed");
                        }
                    });
        }
    }

    void showWaitingRoom(Room room){
        final int MIN_PLAYERS= Integer.MAX_VALUE;
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
                        Log.d(TAG, "-----------show waiting room failed");
                    }
                });
    }

    /*private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            //Signed in successfully, show authenticated UI.
            Log.d(TAG, "--------Account:"+ account);
        } catch(ApiException e){
            Log.w(TAG, "-----------signInResult:failed code=" + e.getStatusCode());
        }
    } */

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
            Log.d(TAG, "-----------------Room ID: " + mRoomId);
            Log.d(TAG, "-----------------My ID " + mMyId);
            Log.d(TAG, "-----------------<< CONNECTED TO ROOM>>");
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
        setContentView(new GameView(this, this));
    }

    public CharacterSprite characterSprite;
    public int mScore = characterSprite.getScore();

    //Score of other participants. We update this as we receive their scores from the network
    Map<String, Integer> mParticipantScore = new HashMap<>();

    private OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();
            Log.d(TAG, "-----------Message received: " + (char) buf[0] + "/" + (int) buf[1]);

            if(buf[0] == 'F' || buf[0] == 'U') {
                //score update
                int existingScore = mParticipantScore.containsKey(sender) ?
                        mParticipantScore.get(sender) : 0;
                int thisScore = (int) buf[1];
                if(thisScore > existingScore) {
                    mParticipantScore.put(sender, thisScore);
                }
                displayScore(false);

            }

        }
    };

    public void displayScore(boolean finalScore){
        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        Log.d(TAG, "----------------- Message buffer 1 from displayScore: " + mMsgBuf[1]);
    }


    //Keeps the screen turned on
    void keepScreenOn() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

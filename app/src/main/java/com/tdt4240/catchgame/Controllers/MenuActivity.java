package com.tdt4240.catchgame.Controllers;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tdt4240.catchgame.Model.Sprites;
import com.tdt4240.catchgame.R;


public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private String difficulty = "difficulty";
    MediaPlayer buttonSound;
    //private Enum character;

    public MenuActivity() {
        super();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        SwitchScreen(R.id.view_main_menu);
        this.buttonSound = MediaPlayer.create(this, R.raw.buttonclick);
        this.buttonSound.setVolume(1, 1);


        // Click listener for all clickable elements
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }

    /*
     * Menu views and logic for switching screens
     */

    final static int[] CLICKABLES = {R.id.btn_play, R.id.btn_rules, R.id.btn_score,
            R.id.btn_settings, R.id.btn_background, R.id.btn_avatar, R.id.switch_sound,
            R.id.switch_background_music, R.id.btn_easy, R.id.btn_medium, R.id.btn_hard,
            R.id.btn_play_single, R.id.btn_play_multi, R.id.btn_goBack, R.id.btn_avatar_crocodile, R.id.btn_avatar_gnu, R.id.btn_avatar_monkey
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                this.buttonSound.start();
                SwitchScreen(R.id.view_play);
                break;
            case R.id.btn_rules:
                this.buttonSound.start();
                SwitchScreen(R.id.view_rules);
                break;
            case R.id.btn_score:
                this.buttonSound.start();
                SwitchScreen(R.id.view_highscore);
                break;
            case R.id.btn_settings:
                this.buttonSound.start();
                SwitchScreen(R.id.view_settings_menu);
                break;
            case R.id.btn_easy:
                this.buttonSound.start();
                Intent intentEasy = new Intent(v.getContext(), SinglePlayerActivity.class);
                //send string to next activity
                intentEasy.putExtra(difficulty, "easy");
                intentEasy.putExtra("gametype", "single");
                startActivity(intentEasy);
                break;
            case R.id.btn_medium:
                this.buttonSound.start();
                Intent intentMedium = new Intent(v.getContext(), SinglePlayerActivity.class);
                intentMedium.putExtra(difficulty, "medium");
                intentMedium.putExtra("gametype", "single");
                startActivity(intentMedium);
                break;
            case R.id.btn_hard:
                this.buttonSound.start();
                Intent intentHard = new Intent(v.getContext(), SinglePlayerActivity.class);
                intentHard.putExtra(difficulty, "hard");
                intentHard.putExtra("gametype", "single");
                startActivity(intentHard);
                break;
            case R.id.btn_play_single:
                this.buttonSound.start();
                SwitchScreen(R.id.view_play_single);
                break;
            case R.id.btn_play_multi:
                this.buttonSound.start();
                //SwitchScreen(R.id.view_play_multi);

                //remains to "catch" this in the constructor of multiplayer
                // but i don't want to do that while abhi is working on it
                // just do the same as for singleplayer
                Intent intentMulti = new Intent(v.getContext(), MultiPlayerActivity.class);
                intentMulti.putExtra("gametype", "multi");
                startActivity(intentMulti);
                break;
            /*case R.id.button_sign_in:
                startActivity(new Intent(v.getContext(), MultiPlayerActivity.class));
                break;*/
            case R.id.btn_goBack:
                this.buttonSound.start();
                if (mCurScreen == R.id.view_play) {
                    SwitchScreen(R.id.view_main_menu);
                    break;
                }
                SwitchScreen(mLastScreen);
                break;
//choice of character
            case R.id.btn_avatar_crocodile:
                this.buttonSound.start();
                Intent intentAvatarCrocodile= new Intent(v.getContext(), CoreGame.class);
                //send string to next activity
                intentAvatarCrocodile.putExtra("avatar", Sprites.CROCODILE);
                startActivity(intentAvatarCrocodile);
                break;
            case R.id.btn_avatar_gnu:
                this.buttonSound.start();
                Intent intentAvatarGnu= new Intent(v.getContext(), CoreGame.class);
                //send string to next activity
                intentAvatarGnu.putExtra("avatar", Sprites.GNU);
                startActivity(intentAvatarGnu);
                break;
            case R.id.btn_avatar_monkey:
                this.buttonSound.start();
                Intent intentAvatarMonkey= new Intent(v.getContext(), CoreGame.class);
                //send string to next activity
                intentAvatarMonkey.putExtra("avatar", Sprites.MONKEY);
                startActivity(intentAvatarMonkey);
                break;
        }
    }

    /*
     * Menu views and logic for switching screens
     */

    final static int[] SCREENS = {
            R.id.view_main_menu, R.id.view_highscore, R.id.view_play,
            R.id.view_play_single, R.id.view_rules, R.id.view_settings_menu, R.id.btn_goBack
    };
    int mCurScreen = -1;
    int mLastScreen = -1;

    public void SwitchScreen(int screenId) {
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        if (screenId != R.id.view_main_menu)
            findViewById(R.id.btn_goBack).setVisibility(View.VISIBLE);

        mLastScreen = mCurScreen;
        mCurScreen = screenId;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.buttonSound.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.buttonSound = MediaPlayer.create(this, R.raw.buttonclick);
        this.buttonSound.setVolume(1, 1);
    }


}

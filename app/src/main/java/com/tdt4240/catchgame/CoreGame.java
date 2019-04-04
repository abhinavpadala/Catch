package com.tdt4240.catchgame;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class CoreGame extends Activity {

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    public CharacterSprite characterSprite;
    public MenuItem btn_exit;
    public MenuItem btn_sound;
    public MenuItem txt_score;
    private ArrayList<FallingObject> objectsOnScreen;
    public ScoreSinglePlayer scoreSinglePlayer;
    private int gameTime;
    private int baseFrequency;
    private int baseSpeed;
    private int fractionGood;
    private String difficulty;

    private String stringDiff = "difficulty";
    private String easy = "easy";
    private String medium = "medium";
    private String hard = "hard";

    private String good = "good";
    private String bad = "bad";
    private String powerup = "powerup";

    protected static Context context;
    private GameView gameview;
    private boolean soundOn;
    List<Integer> objectID;
    FallingObjectFactory fallingObjectFactory;

    SoundEffect soundeffect;

    public MenuItem txt_gameQuit;
    public MenuItem txt_gameOver;
    public MenuItem btn_yes;
    public MenuItem btn_no;


    public CoreGame(String difficulty, Context context, GameView gameview){
        this.gameview = gameview;
        this.context = context;
        this.objectsOnScreen = new ArrayList<>();
        this.objectID = new ArrayList<>();
        this.gameTime = 0;
        this.difficulty = difficulty;
        this.setDifficulty(difficulty);
        this.difficulty = difficulty;
        this.characterSprite = new CharacterSprite(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.sprites_monkey3),0.25));
        scoreSinglePlayer = new ScoreSinglePlayer(this);
        fallingObjectFactory = new FallingObjectFactory(this);

        this.soundeffect = new SoundEffect();


        this.soundOn = true;

        //menu items
        this.btn_exit = new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_exit),0.15));
        this.btn_sound = new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_sound_on),0.15));
        this.txt_score = new MenuItem("Score: "+characterSprite.getScore()+" Lives: "+characterSprite.getLives(), 16, 000000);

        //game over/exit items
        this.txt_gameQuit= new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.txt_quit),1.0));
        this.txt_gameQuit.setPos(screenWidth/2 - txt_gameQuit.getWidth()/2, screenHeight/2 - txt_gameQuit.getHeight()/2);
        this.btn_yes= new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_yes),1.0));
        this.btn_yes.setPos(txt_gameQuit.getPosX() - btn_yes.getWidth()/8, txt_gameQuit.getPosY() + txt_gameQuit.getHeight());
        this.btn_no= new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_no),1.0));
        this.btn_no.setPos(txt_gameQuit.getPosX() + btn_no.getWidth()/8, txt_gameQuit.getPosY() + txt_gameQuit.getHeight());
        //TODO: Replace with game over text
        //this.txt_gameOver= new MenuItem(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.txt_gameover),1.0));
        this.txt_gameOver = new MenuItem("GAME OVER (Click to continue)", 16, 000000);
        this.txt_gameOver.setPos(screenWidth/2 - txt_gameOver.getWidth()/2, screenHeight/2 - txt_gameOver.getHeight()/2);

    }

    public void draw(Canvas canvas){
        characterSprite.draw(canvas);
        btn_exit.draw(canvas, 0, 0);
        btn_sound.draw(canvas, screenWidth - btn_sound.getWidth(), 0);
        txt_score.draw(canvas, screenWidth/2 - txt_score.getWidth()/2, btn_exit.getHeight()/4);

        for(int i=0; i < objectsOnScreen.size(); i++){
            objectsOnScreen.get(i).draw(canvas);
        }
    }

    public SoundEffect getSoundEffect(){
        return this.soundeffect;
    }

    public void update(){
        characterSprite.update();
        if(characterSprite.getLives()==0){
            gameview.gameOver();
        }
        txt_score.updateScoreLife(characterSprite.getScore(), characterSprite.getLives());
        int fallingObjectType = getFallingObjectType();
        for(int i=0; i < objectsOnScreen.size(); i++) {
            FallingObject currentObject = objectsOnScreen.get(i);
            currentObject.update();
            currentObject.detectCollision(characterSprite);
            if (currentObject.collisionDetected()) {
                removeObject(currentObject);
            }
        }
        //continously get new fallingObjectType of object
        if(gameTime % 2 == 1){
            fallingObjectType = getFallingObjectType();
        }
        // TODO: Find a way to spawn the objects based on the gameloop-time from MainThread? and baseFrequency.
        if ((gameTime == 10 ||gameTime % 50 == 0) && objectID.size() > 0){
            if(fallingObjectType == 0){
                spawnObject(createObject(good), good);
            }
            if(fallingObjectType == 1){
                spawnObject(createObject(bad), bad);
            }
            // TODO: Method and logic for power ups
        }
        gameTime++;
    }

    public String getEasy() {

        return easy;
    }

    public String getMedium() {

        return medium;
    }

    public String getHard() {

        return hard;
    }

    public String getGood() {

        return good;
    }

    public String getBad() {
        return bad;
    }

    public String getPowerup() {
        return powerup;
    }

    public String getStringDiff() {
        return stringDiff;
    }

    //creates a list of 0s (good object) and 1s (bad object) according to fraction.
    // 10 numbers in total
    public void setFallingObjectType() {
        //add as many 0s as the fraction of the goodfood
        for (int i = 0; i < this.fractionGood; i++) {
            objectID.add(0);
        }
        //add as many 1s as the fraction of the badfood
        for (int j = 0; j < 10 - this.fractionGood; j++) {
            objectID.add(1);
        }
    }

    //method for getting random falling object according to percentage from level
    public int getFallingObjectType(){
        int id = (int)((Math.random())* (objectID.size() -1));
        return objectID.get(id);

        }



//Fraction of good/bad: 70/30 - 50/50 - 30/70
// remember to say what you want: good/bad/powerup

    public FallingObject createObject(String foodType){
        return fallingObjectFactory.getFallingObject(foodType);
    }

    public void spawnObject(FallingObject fallingObject, String type){
        fallingObject.setObjectPositionX(getRandomXPosition(fallingObject));
        fallingObject.setObjectSpeed(getRandomSpeed());
        fallingObject.setType(type);
        objectsOnScreen.add(fallingObject);
    }

    public void removeObject(FallingObject fallingObject){
        objectsOnScreen.remove(fallingObject);
    }

    public int getRandomXPosition(FallingObject fallingObject){
        return (int)(Math.random() * (screenWidth - fallingObject.getObjectWidth()));
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getRandomSpeed(){
        return (int)((Math.random() + 1) * baseSpeed);
    }

    public int getRandomFrequency(){
        return (int)(Math.random() * baseFrequency);
    }

    public void setLevelUp(){
        if(this.difficulty.equals(easy)){
            this.difficulty = medium;
            soundeffect.levelUpSound();
            setDifficulty(medium);

        }
        if(this.difficulty.equals(medium)){
            this.difficulty = hard;
            soundeffect.levelUpSound();
            setDifficulty(hard);
        }
    }
    //Temporary method to adjust game difficulty, should be in its own class?
    public void setDifficulty(String difficulty){
        if (difficulty.equals(easy)){
            this.baseFrequency = 1;
            this.baseSpeed = 5;
            this.fractionGood = 7;
            setFallingObjectType();
        }
        if (difficulty.equals(medium)){
            this.baseFrequency = 2;
            this.baseSpeed = 10;
            this.fractionGood = 5;
            setFallingObjectType();
        }
        if (difficulty.equals(hard)){
            this.baseFrequency = 3;
            this.baseSpeed = 15;
            this.fractionGood = 3;
            setFallingObjectType();

        }
    }

    public boolean onTouch(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                characterSprite.isBeingTouched((int) motionEvent.getX(), (int) motionEvent.getY());
                if(btn_exit.isTouched(motionEvent.getX(), motionEvent.getY())){
                    gameview.gameExit();
                }
                if(btn_sound.isTouched(motionEvent.getX(), motionEvent.getY())){
                    soundOn = !soundOn;
                    if(soundOn){
                        this.btn_sound.setImage(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_sound_on),0.15));
                        gameview.getSinglePlayerActivity().backgroundMusicOn();
                        soundeffect.volumeOn();

                    }
                    else{
                        this.btn_sound.setImage(getResizedBitmapObject(BitmapFactory.decodeResource(context.getResources(),R.drawable.button_sound_off),0.15));
                        gameview.getSinglePlayerActivity().backgroundMusicOff();
                        soundeffect.volumeOff();

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (characterSprite.isTouched()) {
                    characterSprite.setCharacterPositionX((int) motionEvent.getX());
                }
                break;

            case MotionEvent.ACTION_UP:
                if (characterSprite.isTouched()) {
                    characterSprite.setTouched(false);
                }
                break;
        }
        return true;
    }

    public static Context getContext(){
        return context;

    }

    /*
    * Help method
    * */
    public Bitmap getResizedBitmapObject(Bitmap bmp, double scaleFactorWidth) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        double newWidth = screenWidth * scaleFactorWidth;
        float scale = ((float) newWidth) / width;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap =
                Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
        bmp.recycle();
        return resizedBitmap;
    }
  
}

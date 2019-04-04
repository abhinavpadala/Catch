package com.tdt4240.catchgame;

public class ScoreSinglePlayer {

    public CoreGame coreGame;
    private int twolevelsup = 25;
    private int onelevelup = 15;
    private boolean levelUp = false;

    public ScoreSinglePlayer(CoreGame coreGame) {

        this.coreGame = coreGame;
    }

    // Method for incrementing players score
    public void incrementScore(int scoreInc) {
        coreGame.characterSprite.setScore(coreGame.characterSprite.getScore() + scoreInc);
        System.out.println("Player score: " + coreGame.characterSprite.getScore());

        //Check if "level up"
        if(!coreGame.getDifficulty().equals(coreGame.getHard())){
            //up two levels
            if (coreGame.getDifficulty().equals(coreGame.getMedium()) && coreGame.characterSprite.getScore() >= twolevelsup){
                coreGame.setLevelUp();
            }
            //up one level
            if (coreGame.characterSprite.getScore() >= onelevelup && levelUp == false){
                coreGame.setLevelUp();
                levelUp = true;
            }

        }

    }

    public void decrementScore(int scoreDec) {
        coreGame.characterSprite.setScore(coreGame.characterSprite.getScore() + scoreDec);

        //Check if "level gameover"
        // TODO: Find correct game-over variables and methods, need view
        if (coreGame.characterSprite.getScore() < 0) {

        }

    }


    public void caughtObject(FallingObject object) {
        int objectPoints = object.getScore();
        String typeOfGame = coreGame.getGametype();

        if(object.getType().equals(coreGame.getGood())){
            incrementScore(objectPoints);
        }
        if (object.getType().equals(coreGame.getBad())){
            decrementScore(objectPoints);
        }
        if (object.getType().equals(coreGame.getPowerup())){
            // TODO: Implement power-up logic
            incrementScore(objectPoints);

            /*POWER-UP RULES
            Gets points for catching, in addition to logics for which is caught:

            SINGLE PLAYER:

            #1 Starbeetle: get 10 points
            #2 Ladybug: level down
            #3 Beetle (lightning): get one additional life

            Multi PLAYER:

            #1 Starbeetle: get 10 points
            #2 Ladybug: level down
            #3 Beetle (lightning): faster opponent

            */
            if(typeOfGame.equals("single")) {
                if(objectPoints == 1) {
                    incrementScore(10);
                }
                if(objectPoints == 2){
                    coreGame.setLevelDown();
                }
                if(objectPoints == 3){
                    // TODO: this does not work, fix that the lives increases
                    int currentLives = coreGame.characterSprite.getLives();
                    int newLives = currentLives++;
                    coreGame.characterSprite.setLives(newLives);
                    System.out.println("catched a beetle: get life");
                    System.out.println(coreGame.characterSprite.getLives());
                }
            }

           /* if(typeOfGame.equals("multi")){
                if(objectPoints == 1) {
                    incrementScore(10);
                    // TODO: x2 points of the next caught items, don't know how yet
                }
                if(objectPoints == 2){
                    // TODO: increase size of own sprite and decrease opponent size
                    coreGame.characterSprite.maximizeCharacterSize();
                    //coreGame.opponent.minimizeCharacterSize();
                    // wait 5 seconds

                    coreGame.characterSprite.setCharacterSizeNormal();


                }
                if(objectPoints == 3){
                    // TODO: increase speed of opponent's falling objects
                }
            }*/

        }

    }

}
package org.projectxy.iv4xrLib;

public class NethackMoves {


    private Tile[][] tiles;
    private Player p1;
    private PlayerStatus ps;
    private boolean inventoryScreen, mainMenu, revealControls;
    private boolean playerTurn,aimingBow;

    int x= p1.getX();   // X position of the avatar
    int y = p1.getY();  // Y position of the avatar

    /**
     * Environment needs to set up first, via mapSetup() method.
     * Moves for the enemies also need to be implemented (?)
     */



    /**
     * Basic moves for walking on the map while playing.
     * Need to implement more moves for gameplay.
     *
     * Additional moves are needed for the inventory screen and main menu.
     */



    /**
     * - In order for the player (avatar) to move:
     *      Player has to be alive
     *      Should be player's turn to move
     *      Inventory screen has to be disabled
     *      Player should NOT be aiming with the bow
     *
     *      These conditions should be checked before calling the methods --> need to change/move if statements probably
     *
     *
     * - Player can walk on Floor Tiles and Item Tiles, CANNOT walk on Wall Tiles
     *
     * - If there is a monster on the tile the player is moving towards, player attacks the monster instead of moving
     *
     */


    // Up
    public void up() {
        if(ps.getAlive() && !inventoryScreen && !aimingBow && playerTurn)
        {
            if(tiles[x][y-1] instanceof FloorTile || tiles[x][y-1] instanceof ItemTile){
                tiles[x][y] = new FloorTile(x, y);
                p1.moveUp();
            } else if(tiles[x][y-1] instanceof Monster) {
                attackMonster(x, y - 1);
            }

            playerTurn = !playerTurn;

            if(tiles[x][y-1] instanceof Wall) {
                playerTurn = true;
            }

           // break;
        }
    }


    // Down
    public void down() {
        if(ps.getAlive() && !inventoryScreen && !aimingBow && playerTurn)
        {
            if(tiles[x][y+1] instanceof FloorTile || tiles[x][y+1] instanceof ItemTile){
                tiles[x][y] = new FloorTile(x, y);
                p1.moveDown();
            } else if(tiles[x][y+1] instanceof Monster) {
                attackMonster(x, y + 1);
            }

            playerTurn = !playerTurn;

            if(tiles[x][y+1] instanceof Wall) {
                playerTurn = true;
            }

            //break;
        }

    }


    // Right
    public void right() {
        if(ps.getAlive() && !inventoryScreen && !aimingBow && playerTurn)
        {
            if(tiles[x+1][y] instanceof FloorTile || tiles[x+1][y] instanceof ItemTile){
                tiles[x][y] = new FloorTile(x, y);
                p1.moveRight();
            } else if(tiles[x+1][y] instanceof Monster) {
                attackMonster(x + 1, y);
            }

            playerTurn = !playerTurn;

            if(tiles[x+1][y] instanceof Wall) {
                playerTurn = true;
            }

            //break;
        }

    }


    // Left
    public void left() {
        if(ps.getAlive() && !inventoryScreen && !aimingBow && playerTurn)
        {
            if(tiles[x-1][y] instanceof FloorTile || tiles[x-1][y] instanceof ItemTile){
                tiles[x][y] = new FloorTile(x, y);
                p1.moveLeft();
            } else if(tiles[x-1][y] instanceof Monster) {
                attackMonster(x - 1, y);
            }

            playerTurn = !playerTurn;

            if(tiles[x-1][y] instanceof Wall) {
                playerTurn = true;
            }

            //break;
        }
    }

}

package org.projectxy.iv4xrLib;

import A.B.Item;
import A.B.ItemTile;
import A.B.Mob;
import A.B.Monster;
import A.B.Screen;
import A.B.Tile;
import A.B.Weapon;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;

public class NethackMoves {

    Screen nethack ;
    

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

    
    WorldModel getNetHackState() {
        
        WorldModel wom = new WorldModel() ;
        wom.agentId = "player" ;
        wom.position = new Vec3(nethack.p1.getX(), nethack.p1.getY(), 0) ;
        wom.timestamp = nethack.moves ;
        for(Monster monster : nethack.mobs) {
            WorldEntity e = new WorldEntity(monster.ID, monster.image, true ) ;
            e.position = new Vec3(monster.getX(), monster.getY(), 0) ;
            e.properties.put("health",monster.health) ;
            e.properties.put("attackDmg",monster.attackDmg) ;
            wom.elements.put(e.id, e) ; 
        }
        for(ItemTile item : nethack.items) {
          // do something similar
        }        
        WorldEntity inv = new WorldEntity("Inventory", "Inventory", true) ;
        for(Item item : nethack.ps.inventory) {
            WorldEntity item_ = convertItem(item) ;
            // add properties ...
            inv.elements.put(item_.id,item_) ;
        }
        wom.elements.put(inv.id,inv) ;
        return wom ;
    }
    
    
    WorldEntity convertItem(Item item) {
        WorldEntity item_ = new WorldEntity(item.ID, item.getClass().getSimpleName(),true) ;
        if (item instanceof Weapon) {
            Weapon w = (Weapon) item ;
            // add weapon properties...
        }
        
        return item_ ;
    }
    
    

    // Up
    public WorldModel up() {
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
        return getNetHackState() ;
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

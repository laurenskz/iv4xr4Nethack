package org.projectxy.iv4xrLib;

import A.B.*;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;

import java.awt.event.KeyEvent;


import java.awt.Robot;
import java.awt.AWTException;
import java.io.IOException;
import javax.swing.JPanel;
import java.awt.event.KeyListener;




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
            e.properties.put("alive", monster.alive);               // ??
            e.properties.put("seenPlayer", monster.seenPlayer);     // ??
            e.properties.put("waitTurn", monster.waitTurn);         // ??
            wom.elements.put(e.id, e) ; 
        }

        for(ItemTile item : nethack.items) {

            WorldEntity itm = new WorldEntity(item.ID, item.image, true) ;
            itm.position = new Vec3(item.getX(), item.getY(), 0) ;
            // itm.properties.put("itemType", item.item);

            wom.elements.put(itm.id, itm) ;

            // do something similar


        }

        WorldEntity inv = new WorldEntity("Inventory", "Inventory", true) ;
        for(Item item : nethack.ps.inventory) {
            WorldEntity item_ = convertItem(item) ;
            // add properties ...

            inv.properties.put("amount", item.amount) ;  // ????
            inv.elements.put(item_.id,item_) ;
        }
        wom.elements.put(inv.id,inv) ;
        return wom ;
    }
    
    
    WorldEntity convertItem(Item item) {
        WorldEntity item_ = new WorldEntity(item.ID, item.getClass().getSimpleName(),true) ;

        if (item instanceof Weapon) {
            Weapon w = (Weapon) item ;
            item_.properties.put("weaponName", w.name);
            item_.properties.put("attackDmg", w.attackDmg);         // w.attackDmg or w.getAttack ???
            item_.properties.put("amount", w.amount);

            // add weapon properties...
        }

        if (item instanceof Gold) {
            Gold g = (Gold) item ;
            item_.properties.put("amount", g.amount);

            // add properties...
        }

        if (item instanceof HealthPotion) {
            HealthPotion hp = (HealthPotion) item;
            item_.properties.put("restoreAmount", hp.restoreAmt);
            item_.properties.put("amount", hp.amount);

            // add properties...
        }

        if (item instanceof Water) {
            Water water = (Water) item;
            item_.properties.put("restoreAmount", water.restoreAmt);
            item_.properties.put("amount", water.amount);

            // add properties...
        }

        if (item instanceof Food) {
            Food f = (Food) item ;
            item_.properties.put("restoreAmount", f.restoreAmt);
            item_.properties.put("amount", f.amount);

            // add properties...
        }
        
        return item_ ;
    }
    

///////////////////////////////////////////////////////////////////

/** */


    // Up
    public void up() throws IOException,
            AWTException, InterruptedException {

        KeyEvent e = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.VK_LEFT);

        //KeyEvent e;
        // e = new KeyEvent(a, 1, 20, 1, 10, 'a');

        //Robot robot = new Robot();
        //int key = e.getKeyCode();

        //KeyEvent k = KeyEvent.getExtendedKeyCodeForChar(key);
        //robot.keyPress(KeyEvent.VK_LEFT);

       // public void keyPressed(robot.keyPress(KeyEvent.VK_LEFT)) {


        }


       //nethack.keyPressed( (KeyEvent) key);//return move;
    }


 /*

    // Down
    public WorldModel down() {
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

     */

// }
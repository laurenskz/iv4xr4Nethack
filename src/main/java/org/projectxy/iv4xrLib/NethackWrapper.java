package org.projectxy.iv4xrLib;

import A.B.*;
import alice.tuprolog.Int;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;

import java.awt.event.KeyEvent;


import java.awt.Robot;
import java.awt.AWTException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.KeyListener;




public class NethackWrapper {

    JFrame nethackWindow ;
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
            e.properties.put("alive", monster.alive);               // ??   Probably not needed
            e.properties.put("seenPlayer", monster.seenPlayer);     // ??   ..
            e.properties.put("waitTurn", monster.waitTurn);         // ??   ..
            wom.elements.put(e.id, e) ; 
        }

        for(ItemTile item : nethack.items) {

            WorldEntity itm = new WorldEntity(item.ID, item.image, true) ;
            itm.position = new Vec3(item.getX(), item.getY(), 0) ;
            // itm.properties.put("itemType", item.item);

            wom.elements.put(itm.id, itm) ;


        }

        WorldEntity stairs = new WorldEntity("stairs", "StairTile", false );
        stairs.position = new Vec3(nethack.stairX, nethack.stairY, 0);
        wom.elements.put(stairs.id, stairs);

        System.out.println("stairs position: " + stairs.position);



        WorldEntity inv = new WorldEntity("Inventory", "Inventory", true) ;
        for(Item item : nethack.ps.inventory) {
            WorldEntity item_ = convertItem(item) ;

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

        }

        if (item instanceof Gold) {
            Gold g = (Gold) item ;
            item_.properties.put("amount", g.amount);

        }

        if (item instanceof HealthPotion) {
            HealthPotion hp = (HealthPotion) item;
            item_.properties.put("restoreAmount", hp.restoreAmt);
            item_.properties.put("amount", hp.amount);

        }

        if (item instanceof Water) {
            Water water = (Water) item;
            item_.properties.put("restoreAmount", water.restoreAmt);
            item_.properties.put("amount", water.amount);

        }

        if (item instanceof Food) {
            Food f = (Food) item ;
            item_.properties.put("restoreAmount", f.restoreAmt);
            item_.properties.put("amount", f.amount);

        }
        
        return item_ ;
    }
    

///////////////////////////////////////////////////////////////////

/** */

    public WorldModel observe() {
        return getNetHackState() ;
    }

    public void startNewGame() {
        System.out.println("Start New Game") ;
        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
        nethack.keyPressed(e);
    }

    public void restartGame() {
        System.out.println("Restart Game") ;
        if (!nethack.ps.getAlive()){

            KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
            nethack.keyPressed(e);

        }

    }

    
    public enum Movement { UP, DOWN, LEFT, RIGHT, DONOTHING }
    
    public WorldModel move(Movement mv) {
        
        int key ;
        switch(mv) {
          case UP : key = KeyEvent.VK_UP ; break ;
          case DOWN : key = KeyEvent.VK_DOWN ; break ;
          case LEFT : key = KeyEvent.VK_LEFT ; break ;
          case RIGHT : key = KeyEvent.VK_RIGHT ; break ;
          case DONOTHING: key = KeyEvent.VK_W ; break ;
          default : throw new IllegalArgumentException() ;
        }

        // System.out.println("### up()") ;
        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, key, KeyEvent.CHAR_UNDEFINED);

        //KeyEvent e;
        // e = new KeyEvent(a, 1, 20, 1, 10, 'a');

        //Robot robot = new Robot();
        //int key = e.getKeyCode();

        //KeyEvent k = KeyEvent.getExtendedKeyCodeForChar(key);
        //robot.keyPress(KeyEvent.VK_LEFT);

       // public void keyPressed(robot.keyPress(KeyEvent.VK_LEFT)) {
      nethack.keyPressed(e);
      return observe() ;
    }


    public enum Interact { OpenInv, SelectItemFromInv, AimWithBow, PickupItem, NavigateInv }

    public WorldModel action (Interact act) {

        int key ;
        switch(act) {
            case OpenInv : key = KeyEvent.VK_I ; break ;

            case SelectItemFromInv : if(nethack.inventoryScreen) {

                key = KeyEvent.VK_ENTER ;
                nethack.useItemFromInventory();

                break ;
            }

            case AimWithBow : key = KeyEvent.VK_SHIFT ; break ;

            case PickupItem : if(!nethack.inventoryScreen) {
                key = KeyEvent.VK_ENTER ;
                break ;
            }

            case NavigateInv: if(nethack.inventoryScreen) {
                key = KeyEvent.VK_W ;
                break ;
            }

            default : throw new IllegalArgumentException() ;
        }

        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, key, KeyEvent.CHAR_UNDEFINED);

        nethack.keyPressed(e);
        return observe() ;
    }


    public static void main(String[] args) throws IOException, AWTException, InterruptedException {
        
        NethackWrapper driver = new NethackWrapper() ;
        driver.nethack = new Screen() ;
        
        JFrame frame = new JFrame("NetHack Clone");
        driver.nethackWindow = frame ;
        
        frame.add(driver.nethack);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        // driver.nethack.animate();
        
        
        Thread t = new Thread( () -> driver.nethack.animate() ) ;
        t.start() ;


        //////////////////////////////////////////////////////////////////////////////////////////////////////
        //  This is a simple example of the player moving in the game and performing some initial actions  //
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        Thread.sleep(1000);
        driver.startNewGame();

        Thread.sleep(1000);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.move(Movement.RIGHT) ;
        Thread.sleep(500);

        driver.action(Interact.PickupItem);
        Thread.sleep(500);

        driver.action(Interact.AimWithBow);
        Thread.sleep(500);

        driver.action(Interact.OpenInv);
        Thread.sleep(500);

        driver.move(Movement.DOWN);
        Thread.sleep(500);

        driver.move(Movement.DOWN);
        Thread.sleep(500);

        driver.action(Interact.SelectItemFromInv);
        Thread.sleep(500);






        WorldModel wom = driver.observe() ;





        System.out.println("Player-position: " + wom.position) ;



        //System.out.println("type anything... ") ;
        //in = new Scanner(System.in);
        //in.nextLine() ;
        
    }
    
    

}
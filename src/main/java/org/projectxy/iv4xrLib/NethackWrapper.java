package org.projectxy.iv4xrLib;

import A.B.*;
import alice.tuprolog.Int;
import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.framework.spatial.meshes.Edge;

import java.awt.event.KeyEvent;

import java.awt.AWTException;
import java.io.IOException;
import java.util.*;
import javax.swing.JFrame;

public class NethackWrapper {

    JFrame nethackWindow;
    Screen nethack;

    /**
     * Launch an instance of Nethack and bind it to this wrapper. The game will be
     * run on its own thread. This method returns this thread, just in case you need
     * to do something with it.
     * 
     * @param conf Some initial configuration information specifying the desired
     *             setup for the instance of Nethack that we want to create. For now
     *             this conf is not used.
     * @return
     */
    public Thread launchNethack(NethackConfiguration conf) {
        // for now we ignore the configuration
        nethack = new Screen();
        JFrame frame = new JFrame("NetHack Clone");
        nethackWindow = frame;
        frame.add(nethack);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Thread t = new Thread(() -> nethack.animate());
        t.start();
        return t;
    }

    /**
     * Will close Nethack when next time we send an instruction there.
     */
    public void closeNethack() {
        nethack.stopAtNextUpdate();
    }

    public SimpleNavGraph getNavigationGraph() {
        SimpleNavGraph navgraph = new SimpleNavGraph();

        // find tiles which are walkable, and add them to the navgraph:
        Map<Integer, Tile> walkableTiles = new HashMap<>();
        int index = 0;
        for (Tile[] column : nethack.tiles) {
            for (Tile tile : column) {
                if (!(tile instanceof Wall)) {
                    Vec3 position = new Vec3(tile.x, tile.y, 0f);
                    navgraph.vertices.add(position);
                    walkableTiles.put(index, tile);
                    index++;
                }
            }
        }

        int numOfNodes = walkableTiles.size();
        for (int v = 0; v < numOfNodes; v++) {
            for (int z = 0; z < numOfNodes; z++) {
                // consider node v and node z
                Tile vTile = walkableTiles.get(v);
                Tile zTile = walkableTiles.get(z);
                if (Math.abs(zTile.y - vTile.y) == 1 && vTile.x == zTile.x) { // complete this for N/S neighbours

                    Edge edge = new Edge(v, z);
                    navgraph.edges.put(edge);
                }

                if (Math.abs(zTile.x - vTile.x) == 1 && vTile.y == zTile.y) {
                    Edge edge = new Edge(v, z);
                    navgraph.edges.put(edge);
                }

            }
        }

        // adding monsters as obstacles
        for (Monster m : nethack.mobs) {
            // for each monster, we create a line-intersectable wrapper for it,
            // and then we wrap it once more as an obstacle, and add it to the
            // nav-graph:
            Obstacle<LineIntersectable> monster_ = new Obstacle(new MonsterWrapper(m));
            monster_.isBlocking = true;
            navgraph.obstacles.add(monster_);
        }

        return navgraph;

    }

    /**
     * Construct the World-model representation of the current Nethack's game-state.
     */
    WorldModel getNetHackState() {

        WorldModel wom = new WorldModel();
        wom.agentId = "player";
        wom.position = new Vec3(nethack.p1.getX(), nethack.p1.getY(), 0);
        wom.timestamp = nethack.moves;

        // System.out.println("Player pos: " + wom.position);
        
        
        // Equipped Weapon
        Weapon weaponOnHands = nethack.ps.weap;
        WorldEntity equippedWeapon = new WorldEntity(weaponOnHands.ID, "equippedWeapon", true);
        equippedWeapon.properties.put("weaponName", weaponOnHands.name);
        equippedWeapon.properties.put("attackDmg", weaponOnHands.attackDmg);
        wom.elements.put(equippedWeapon.id, equippedWeapon);
        
        
        

        // monsters:
        for (Monster monster : nethack.mobs) {
            WorldEntity e = new WorldEntity(monster.ID, monster.image, true);
            e.position = new Vec3(monster.getX(), monster.getY(), 0);
            e.properties.put("health", monster.health);
            e.properties.put("attackDmg", monster.attackDmg);
            e.properties.put("alive", monster.alive); // ?? Probably not needed
            e.properties.put("seenPlayer", monster.seenPlayer); // ?? ..
            e.properties.put("waitTurn", monster.waitTurn); // ?? ..
            wom.elements.put(e.id, e);
        }

        
        
        // items that are still on the floor:
        for (ItemTile itemTile : nethack.items) {
            WorldEntity itm = convertItem(itemTile.item);
            itm.position = new Vec3(itemTile.getX(), itemTile.getY(), 0);
            wom.elements.put(itm.id, itm);
        }
        
        
        
        
        // Player Status - Health(???)
        PlayerStatus ps = nethack.ps;
        WorldEntity playerStatus = new WorldEntity(wom.agentId, "playerStatus", true);
        playerStatus.properties.put("health", ps.health );
        playerStatus.properties.put("maxhealth", ps.maxHealth );	// Maybe no need for this
        playerStatus.properties.put("isAlive",  ps.alive);			// Boolean
        wom.elements.put(playerStatus.id, playerStatus);

        
        
        // Stairs:
        
        Tile stairTile = nethack.tiles[nethack.stairX][nethack.stairY];
        WorldEntity stairs = new WorldEntity("Stairs", "Stairs", false);	// Id maybe should derive from stairTile.ID, but it is null in our case 
        stairs.position = new Vec3(nethack.stairX, nethack.stairY, 0);
        wom.elements.put(stairs.id, stairs);
        
        System.out.println("Stairs ID: " + stairs.id); //??


        
        
        // items in the inventory:
        WorldEntity inv = new WorldEntity("Inventory", "Inventory", true);
        for (Item item : nethack.ps.inventory) {
            WorldEntity item_ = convertItem(item);

            // WP: not needed. In fact, wrong. item.amount is already being tracked
            // by convertItem above
            // inv.properties.put("amount", item.amount) ; // ????
            inv.elements.put(item_.id, item_);
        }
        wom.elements.put(inv.id, inv);
        return wom;
    }
    

    /**
     * Construct the WorldEntity-representation of an item.
     */
    
    
    WorldEntity convertItem(Item item) {
        WorldEntity item_ = new WorldEntity(item.ID, item.getClass().getSimpleName(), true);

        if (item instanceof Weapon) {
            Weapon w = (Weapon) item;
            item_.properties.put("weaponName", w.name);
            item_.properties.put("attackDmg", w.attackDmg); // w.attackDmg or w.getAttack ???
            item_.properties.put("amount", w.amount);

        }

        if (item instanceof Gold) {
            Gold g = (Gold) item;
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
            Food f = (Food) item;
            item_.properties.put("restoreAmount", f.restoreAmt);
            item_.properties.put("amount", f.amount);

        }

        return item_;
    }

///////////////////////////////////////////////////////////////////

    public WorldModel observe() {
        return getNetHackState();
    }

    public void startNewGame() {
        System.out.println("Start New Game");
        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, KeyEvent.VK_ENTER,
                KeyEvent.CHAR_UNDEFINED);
        nethack.keyPressed(e);
    }

    public void restartGame() {
        System.out.println("Restart Game");
        if (!nethack.ps.getAlive()) {

            KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, KeyEvent.VK_SPACE,
                    KeyEvent.CHAR_UNDEFINED);
            nethack.keyPressed(e);

        }

    }

    public enum Movement {
        UP, DOWN, LEFT, RIGHT, DONOTHING
    }

    public WorldModel move(Movement mv) {

        int key;
        switch (mv) {
        case UP:
            key = KeyEvent.VK_UP;
            break;
        case DOWN:
            key = KeyEvent.VK_DOWN;
            break;
        case LEFT:
            key = KeyEvent.VK_LEFT;
            break;
        case RIGHT:
            key = KeyEvent.VK_RIGHT;
            break;
        case DONOTHING:
            key = KeyEvent.VK_W;
            break;
        default:
            throw new IllegalArgumentException();
        }

        // System.out.println("### up()") ;
        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, key, KeyEvent.CHAR_UNDEFINED);

        // KeyEvent e;
        // e = new KeyEvent(a, 1, 20, 1, 10, 'a');

        // Robot robot = new Robot();
        // int key = e.getKeyCode();

        // KeyEvent k = KeyEvent.getExtendedKeyCodeForChar(key);
        // robot.keyPress(KeyEvent.VK_LEFT);

        // public void keyPressed(robot.keyPress(KeyEvent.VK_LEFT)) {
        nethack.keyPressed(e);
        return observe();
    }

    /**
     * Use an item in the player's inventory.
     */
    public WorldModel useItem(int indexItemInInventory) {
        nethack.useItemFromInventory(indexItemInInventory);
        return observe();
    }

    public WorldModel useItem(String itemId) {
        int N = nethack.ps.inventory.size();
        for (int index = 0; index < N; index++) {
            Item item = nethack.ps.inventory.get(index);
            if (item.ID.equals(itemId)) {
            	System.out.println("THIS was called by SelectItemFromInv");
                return useItem(index);
                
               
            }
        }
        throw new IllegalArgumentException("Item " + itemId + " is not in the inventory.");
    }

    public enum Interact {
        OpenInv, SelectItemFromInv, AimWithBow, PickupItem, NavigateInvUp, NavigateInvDown
    }

    public WorldModel action(Interact act, String itemId) {

        int key = -1;

        switch (act) {
        case OpenInv:
            key = KeyEvent.VK_I;
            break;

        case SelectItemFromInv:
        	System.out.println("THIS is in SelectItemFromInv!");

            // String foodId = item.ID;
            useItem(itemId);
            System.out.println("Item Id: " + itemId);
            System.out.println("int key: " + key);

            break;

//            	 for(Item item : nethack.ps.inventory) {
//                  	if(item instanceof HealthPotion) {
//                  		
//                  		String HPid = item.ID;
//                  		useItem(HPid);
//                  		System.out.println("HP Id: "+ HPid);
//                  		//System.out.println("ssssssssssssssss");
//                  		System.out.println("int key: " + key);
//                  		
//
//                  		break;
//                  		
//                  	}
//                  }
//            	 

        // key = KeyEvent.VK_ENTER ;
        // nethack.useItemFromInventory();
        // break ;

        case AimWithBow:
            key = KeyEvent.VK_SHIFT;
            break;

        case PickupItem:
            if (!nethack.inventoryScreen) {
                key = KeyEvent.VK_ENTER;

                break;
            }

        case NavigateInvDown:
            if (nethack.inventoryScreen) {
                key = KeyEvent.VK_DOWN;
                break;
            }

        case NavigateInvUp:
            if (nethack.inventoryScreen) {
                key = KeyEvent.VK_UP;

                // System.out.println("Up!!");

                break;
            }

        default:
            throw new IllegalArgumentException();
        }

        KeyEvent e = new KeyEvent(nethackWindow, KeyEvent.KEY_PRESSED, 1, 0, key, KeyEvent.CHAR_UNDEFINED);
        nethack.keyPressed(e);

        return observe();
    }

    // just for a quick test of this wrapper:
    public static void main(String[] args) throws IOException, AWTException, InterruptedException {

        NethackConfiguration conf = new NethackConfiguration() ;
        NethackWrapper driver = new NethackWrapper();
        driver.launchNethack(conf) ;
        
        /*
        driver.nethack = new Screen();

        JFrame frame = new JFrame("NetHack Clone");
        driver.nethackWindow = frame;

        frame.add(driver.nethack);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        // driver.nethack.animate();

        Thread t = new Thread(() -> driver.nethack.animate());
        t.start();
        */

        //////////////////////////////////////////////////////////////////////////////////////////////////////
        // This is a simple example of the player moving in the game and performing some
        ////////////////////////////////////////////////////////////////////////////////////////////////////// initial
        ////////////////////////////////////////////////////////////////////////////////////////////////////// actions
        ////////////////////////////////////////////////////////////////////////////////////////////////////// //
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        Thread.sleep(1000);
        driver.startNewGame();

        Thread.sleep(1000);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        
        
  		
        
        
        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

        driver.move(Movement.RIGHT);
        Thread.sleep(500);

//        GoalLib.useFoodFromInventory();
//        Thread.sleep(500);
//        System.out.println("HERE after useFoodFromInventory!");

        driver.action(Interact.PickupItem, "");
        Thread.sleep(500);

        driver.action(Interact.AimWithBow, "");
        Thread.sleep(500);

        driver.action(Interact.OpenInv, "");
        Thread.sleep(500);

        driver.action(Interact.NavigateInvDown, "");
        Thread.sleep(500);

        driver.action(Interact.NavigateInvDown, "");
        Thread.sleep(500);

//        driver.action(Interact.SelectItemFromInv, "");
//        Thread.sleep(500);

        driver.action(Interact.OpenInv, "");
        Thread.sleep(500);

//        for(Item item : driver.nethack.ps.inventory) {
//        	if(item instanceof Food) {
//        		
//        		String foodId = item.ID;
//        		driver.useItem(foodId);
//        		System.out.println("Food Id: "+ foodId);
//        		break;
//        		
//        	}
//        }

//        System.out.println(driver.nethack.ps.inventory);
//        driver.useItem(1);
//        Thread.sleep(500);

        WorldModel wom = driver.observe();
        System.out.println("Player-position: " + wom.position);
        
        System.out.println("equip Weap: " + driver.nethack.ps.weap.name);

        Tile stairTile = driver.nethack.tiles[driver.nethack.stairX][driver.nethack.stairY];

        WorldEntity stair = wom.elements.get(stairTile.ID);
        System.out.println("Stair-position: " + stair.position);

//        System.out.println("type anything... ") ;
//        Scanner in = new Scanner(System.in);
//        in.nextLine() ;

        // now we can also close the Nethack-window:
        // driver.closeNethack();

    }

}
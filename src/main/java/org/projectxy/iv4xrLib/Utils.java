package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.*;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.*;
import nl.uu.cs.aplib.utils.Pair;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import org.projectxy.iv4xrLib.NethackWrapper.Interact;
import org.projectxy.iv4xrLib.NethackWrapper.Movement;

import A.B.Monster;
import A.B.HealthPotion;
import A.B.Food;
import A.B.Water;
import A.B.Tile;
import A.B.Wall;

public class Utils {

    /**
     * Convert a 3D coordinate p to a discrete tile-world coordinate. Basically, p
     * will be converted to a pair of integers (p.x,p.y).
     */
    public static Pair<Integer, Integer> toTileCoordinate(Vec3 p) {
        return new Pair((int) p.x, (int) p.y);
    }
    
    public static Vec3 toVec3(int x, int y) {
        return new Vec3(x,y,0) ;
    }
    
    /**
     * Check if two Vec3 represents the same tile coordinate.
     */
    public static boolean sameTile(Vec3 t1, Vec3 t2) {
        Pair<Integer, Integer> p1 = toTileCoordinate(t1) ;
        Pair<Integer, Integer> p2 = toTileCoordinate(t2) ;
        return p1.fst.equals(p2.fst) && p1.snd.equals(p2.snd) ;
    }
    
    public static Integer vec3ToNavgraphIndex(Vec3 p, SimpleNavGraph nav) {
        for(int i=0; i<nav.vertices.size(); i++) {
            if(sameTile(p,nav.vertices.get(i))) return i ;
        }
        return null ;
    }
 

    /**
     * Construct an action to guide an agent to either a tile-location or to a
     * non-monster entity, specified by its id. If the id null, a location must be
     * provided. If the id is not null, location is ignored.
     * 
     * @param monsterAvoidDistance set this to any value larger than 1 to make the
     *                             planned path to steer away as far as this
     *                             distance from any monster. Note that the action
     *                             calculates the path at the beginning. If during
     *                             the travel a monster moves, it might actually
     *                             come closer than this distance to the agent. This
     *                             action does not solve this situation; another
     *                             tactic should instead handle such a situation.
     */
    public static Action travelTo(String entityId, Vec3 destination, float monsterAvoidDistance) {
        
        return action("travel-to").do2((MyAgentState S) -> (List<Vec3> path) -> {
            
            S.setAPathToFollow(path) ;
            MyEnv env = (MyEnv) S.env() ;
            Vec3 agentCurrentPosition = S.wom.position ;
            
            //System.out.println(">>> agent @" +  agentCurrentPosition) ;
            //for(Vec3 nd : path) {
            //    System.out.print("-->" + nd) ;
            //}
            //System.out.println("") ;
            
            Vec3 nextTile = path.remove(0) ;
            
            int dx = (int) (nextTile.x - agentCurrentPosition.x) ;
            int dy = (int) (nextTile.y - agentCurrentPosition.y) ;
            if(dx>0) {
                env.move(Movement.RIGHT) ;
            }
            else if (dx<0) {
                env.move(Movement.LEFT) ;
            }
            else if (dy>0) {
                env.move(Movement.DOWN) ;
            }
            else if (dy<0) {
                env.move(Movement.UP) ;
            }
            else {
                throw new IllegalArgumentException() ;
            }
            S.updateState() ;
            return S ;
            })
            .on((MyAgentState S) -> { 
                if (! S.isAlive()) return null ;
                Vec3 agentCurrentPosition = S.wom.position ;
                Vec3 destination_ = destination ;
                if(entityId != null) {
                    WorldEntity e = S.wom.getElement(entityId) ;
                    if(e == null) {
                        throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
                    }
                    destination_ = e.position ;
                }
                
                List<Vec3> path0 = S.currentPathToFollow ;
                // if there is no path being planned, or if there is one planned,
                // but the destination is different, plan a new path:
                if(path0 == null
                	     ||   path0.size()==0

                	            ||      

                	             ! Utils.sameTile(destination_,path0.get(path0.size()-1)))

                	  {
                    float dw = Math.max(1, 2*monsterAvoidDistance) ;
                    ((MyNavGraph) S.simpleWorldNavigation).setMonstersDangerArea(dw) ;
                    path0 = S.getPath(agentCurrentPosition,destination_) ;
                    //System.out.print(">>> agent @" +  agentCurrentPosition) ;
                    //System.out.print(">>> Planing path to " +  destination_) ;
                    //System.out.println(", #" + path0.size()) ;
                    ((MyNavGraph) S.simpleWorldNavigation).resetMonstersDangerArea() ;
                    if(path0 == null) {
                        return null ;
                    }
                }    
                return path0 ;} )
                ;
    }
    
    public static Action travelToMonster(String monsterId, float monsterAvoidDistance) {
        
        // we will re-use the above defined travelTo-action, but will change its guard
        Action travelTo = travelTo(monsterId,null,monsterAvoidDistance) ;
        
        return travelTo.on((MyAgentState S) -> {
            if (! S.isAlive()) return null ;
            Vec3 agentCurrentPosition = S.wom.position ;
            WorldEntity e = S.wom.getElement(monsterId) ;
            //WorldEntity previouse = S.previousWom.getElement(monsterId) ;

            if(e == null) {
               // the monster is not there, or has been killed; well then we can't move to it either
               return null ;
            }
            
            
            
            List<Vec3> path0 = S.currentPathToFollow ;
            
            
//            if(e == null) {		 
//            	if(e != previouse) { // the monster has been killed
//            		return path0;
//            	}
//            	else {
//                // the monster is not there
//                return null ;
//            	}
//            	
//             }
            
            
            // check if re-planing is not needed:
            if(path0 != null  &&   path0.size()>0) {
                Vec3 last = path0.get(path0.size() - 1) ;
                int dx = (int) Math.abs(last.x - e.position.x) ;
                int dy = (int) Math.abs(last.y- e.position.y) ;
                if(dx + dy == 1) {
                    // the current path targets a tile next to the monster; no replanning is needed:
                    return path0 ;
                }
            }
            // else we need to re-plan.
            path0 = null ;
            float dw = Math.max(1, 2*monsterAvoidDistance) ;
            ((MyNavGraph) S.simpleWorldNavigation).setMonstersDangerArea(dw,monsterId) ;
            
            // we can't literally move to the monster's tile; so we choose a tile next to it:
            Vec3[] candidates = { 
                      Vec3.add(e.position, new Vec3(1,0,0)), 
                      Vec3.add(e.position, new Vec3(-1,0,0)), 
                      Vec3.add(e.position, new Vec3(0,1,0)), 
                      Vec3.add(e.position, new Vec3(0,-1,0)) 
                   };
            
            for(Vec3 targetLocation : candidates) {
                if (S.simpleWorldNavigation.vertices.contains(targetLocation)) {
                    path0 = S.getPath(agentCurrentPosition,targetLocation) ;
                    if (path0 != null) {
                        // found a reachable neighbor-tile
                        break ;
                    }
                }
            }
            ((MyNavGraph) S.simpleWorldNavigation).resetMonstersDangerArea() ;
            return path0 ;
        }) ;
    }
    
    
    /**
     * Will attack a monster, if there is one in a neighboring tile. Will use melee attack.
     */
    public static Action meleeAttack() {
        return action("melee-attack").do2((MyAgentState S) -> (Vec3 monsterLocation) -> {
            MyEnv env = (MyEnv) S.env() ;
            Vec3 agentCurrentPosition = S.wom.position ;
            int dx = (int) (monsterLocation.x - agentCurrentPosition.x) ;
            int dy = (int) (monsterLocation.y - agentCurrentPosition.y) ;
            if(dx>0) {
                //System.out.println(">>> Attack R") ;
                env.move(Movement.RIGHT) ;
            }
            else if (dx<0) {
                //System.out.println(">>> Attack L") ;
                env.move(Movement.LEFT) ;
            }
            else if (dy>0) {
                //System.out.println(">>> Attack D") ;
                env.move(Movement.DOWN) ;
            }
            else if (dy<0) {
                //System.out.println(">>> Attack U") ;
                env.move(Movement.UP) ;
            }
            else {
                throw new IllegalArgumentException() ;
            }
            // let's also reset the planned path, if there is any:
            S.currentPathToFollow = null ;
            S.updateState() ;
            return S ;
          }) 
          .on((MyAgentState S) -> { 
             if (! S.isAlive()) return null ;
             // check if one of the monsters is in a neighboring tile
             for(WorldEntity e : S.wom.elements.values()) {
                 if(e.type.equals(Monster.class.getSimpleName())) {
                     // e is a monster
                     int dx = (int) Math.abs(e.position.x - S.wom.position.x) ;
                     int dy = (int) Math.abs(e.position.y - S.wom.position.y) ;
                     if (dx + dy == 1) {
                         // then the monster is in a neighboring tile:
                         //System.out.println(">>> monster is near: " + e.position) ;
                         return e.position ;
                     }
                 }
             }
             return null ;       
          }) ;
    }
    
    
    /////////////////// 
    
    
    
    public static Action bowAttack() {
        return action("bow-attack").do2((MyAgentState S) -> (Vec3 monsterLocation) -> {
        	
        	//S.setAPathToFollow(path) ;
            
            MyEnv env = (MyEnv) S.env() ;
            Vec3 agentCurrentPosition = S.wom.position ;
            int mx = (int) monsterLocation.x; 					// monster's x coordinate
            int my = (int) monsterLocation.y;					// monster's y coordinate
            int ax = (int) agentCurrentPosition.x;				// agent's x coordinate
            int ay = (int) agentCurrentPosition.y;				// agent's y coordinate
            
            // int dx = (int) (monsterLocation.x - agentCurrentPosition.x) ;
            // int dy = (int) (monsterLocation.y - agentCurrentPosition.y) ;
            
            System.out.println(">>> FIRING BOW") ;
            if(mx == ax) {
            	
            	if(my > ay) {
            		
            		//int dy = my=ay;
            		
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.DOWN) ;	
            	}
            	else {
            		
            		// dy = ay=my;
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.UP) ;	
            		
            	}
            	
            	
                //System.out.println(">>> Attack R") ;
                //env.move(Movement.RIGHT) ;
            }
            else if (my==ay) {
            	
            	
            	if(mx > ax) { 
            		
            		//int dx = mx-ax;
            		
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.RIGHT) ;	
            	}
            	
            	else {
            		
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.LEFT) ;	
            }}
           
            else {
                throw new IllegalArgumentException() ;
            }
            // let's also reset the planned path, if there is any:
            S.currentPathToFollow = null ;
            S.updateState() ;
            return S ;
          }) 
        		
        		
          .on((MyAgentState S) -> { 
             if (! S.isAlive()) return null ;
             
             MyEnv env = (MyEnv) S.env() ;

             
             WorldModel current = S.wom ;
 	         String agentId = S.wom.agentId ;
 	         WorldEntity agentCurrentState = current.elements.get(agentId) ;
 	         String currentWeapon = agentCurrentState.getStringProperty("equippedWeaponName") ;
 	         String weaponNeeded = "Bow";
 	         
 	         if (!(currentWeapon.toLowerCase().contains(weaponNeeded.toLowerCase() ) )) return null;
 	         
 	         List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
             
             // check if one of the monsters is vertically or horizontally across the agent
             for(WorldEntity e : S.wom.elements.values()) {
                 if(e.type.equals(Monster.class.getSimpleName())) {
                     // e is a monster
                	 
                	 int mx = (int) e.position.x; 					// monster's x coordinate
                     int my = (int) e.position.y;					// monster's y coordinate
                     int ax = (int) S.wom.position.x;				// agent's x coordinate
                     int ay = (int) S.wom.position.y;				// agent's y coordinate
                	 
                     //int dx = (int) Math.abs(e.position.x - S.wom.position.x) ;
                     //int dy = (int) Math.abs(e.position.y - S.wom.position.y) ;
                     
             		 int dx = Math.abs(ax-mx);
             		 int dy = Math.abs(ay-my);
             		 int isWall;    
                     
                     if (	(mx == ax || my == ay) 	) {
                    	 
                    	 
//                    	 int dx = Math.abs(ax-mx);
//                    	 int dy = Math.abs(ay-my);
//                    	 int isWall;
                    	 
                    	 if (mx<ax) {
	                 		 isWall = dx;
	                 		 for (int i = mx; i<ax ; i++) {
	                 			 for (Vec3 v : walkableTiles) {
	                 				
	                 				 if(Utils.sameTile(v, new Vec3(i, my, 0))) {
	                 					 isWall--;
	                 					 //break;
	                 				 }	
	                 				System.out.println("isWall: "+ isWall);
	                 			 }
	                 		 }
	                 		 
                    	 }
                    	 else if(mx>ax) {
                    		 
                    		 isWall = dx;
 	                 		
	                 		 for (int i = ax; i<mx ; i++) {
	                 			 for (Vec3 v : walkableTiles) {
	                 				
	                 				 if(Utils.sameTile(v, new Vec3(i, my, 0))) {
	                 					 isWall--;
	                 					 //break;
	                 				 }	
	                 				
	                 			 }
	                 		 }
                    	 }
                    	 else {
	                 		 isWall = dy;

                    		 if (my<ay) {
    	                 		 //isWall = dy;
    	                 		
    	                 		 for (int i = my; i<ay ; i++) {
    	                 			 for (Vec3 v : walkableTiles) {
    	                 				 if(Utils.sameTile(v, new Vec3(mx, i, 0))) {
    	                 					 isWall--;
    	                 					 //break;
    	                 				 }		
    	                 			 }
    	                 		 }
    	                 		 
                        	 }
                    		 else if (my>ay){
                    			
    	                 		 for (int i = ay; i<my ; i++) {
    	                 			 for (Vec3 v : walkableTiles) {
    	                 				 if(Utils.sameTile(v, new Vec3(mx, i, 0))) {
    	                 					 isWall--;
    	                 					 //System.out.println("Position: "+ i +","+ mx);
    	                 					 //break;
    	                 					
    	                 				 }		
    	                 			 }
    	                 		 }
                    		 }
                    	 }
                 				
                    	 System.out.println("isWall: "+ isWall);
                 		 //if (isWall <= 2) {
                 		 if (isWall <= 0) {
                                	 
                     		//env.interact(S.wom.agentId, null, Interact.AimWithBow);

                 			return e.position ;
                 			
                 		 }
                    	 
                         
                     }
                 }
             }
             return null ;       
          }) ;
    }
    
    //////////////////
    public static Action equipBestAvailableWeapon() {
        return action("equip Best Available Weapon").do2((MyAgentState S) -> (String itemId) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        //WorldModel old = S.previousWom ;

	        //WorldEntity currentInv = current.getElement("Inventory"); 
	        //WorldEntity oldInv = old.getElement("Inventory"); 

			boolean bestWeaponEquipped = false;
			
	        //String agentId = S.wom.agentId ;
	        //WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        //int bestWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");

			
			//int oldInvSize = oldInv.elements.size();
			//System.out.println("old inv size: "+ oldInvSize);
			

      		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
      		
      		
      		bestWeaponEquipped = true;
   
		    
	        if(bestWeaponEquipped) {
	        	
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	        
	    })
		.on((MyAgentState S) -> { 
			WorldModel current = S.wom ;
	        //WorldModel old = S.previousWom ;

	        String bowWeapon = "Bow";
	        String swordWeapon = "Sword";

	        WorldEntity currentInv = current.getElement("Inventory"); 
	        //WorldEntity oldInv = old.getElement("Inventory"); 

	        String agentId = S.wom.agentId ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        int bestWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");

//			int oldInvSize = oldInv.elements.size();
//			int currentInvSize = currentInv.elements.size();

//			System.out.println("old inv size: "+ oldInvSize);
//			System.out.println("current inv size: "+ currentInvSize);
			
//			if (S.previousWom.getElement("Inventory").elements.size() <= S.wom.getElement("Inventory").elements.size()) {
//				System.out.print("YO");
//				return null;
//			}
//			else {
//			System.out.print("YO-YO");
			
			System.out.println("Iterating the inventory elements and looking for the weapon with the higher damage..");

		        for(WorldEntity item_ : currentInv.elements.values()) {
	          		System.out.println(item_.type + ","+ item_.id);
	          		if ((item_.type.toLowerCase().contains(bowWeapon.toLowerCase())) || (item_.type.toLowerCase().contains(swordWeapon.toLowerCase()))){      	
		          		int dmg = item_.getIntProperty("attackDmg");
		
			          	if( dmg > bestWeaponDmg   ) {
			          		//System.out.println("AND HERE!");
			          		
			          		bestWeaponDmg = dmg;
			          		String itemId = item_.id;
			          	          		
			          		
			          		
			          		System.out.println("Equipped item type: " + item_.type);
			          		System.out.println("Equipped item damage: " + dmg);
			          		
			          		return itemId;
			          		
			          		
			          		// Freeze the Nethack window until Enter key is pressed. 
			          		// So we can see the progress of the goals in the actual game.
			          		//System.out.println("Hit RETURN to continue.") ;
			                //new Scanner(System.in) . nextLine() ;
		
			          		//break;
			          	}
			          	
	          		}
		         }
			
	        return null;
			
         }) ;
        
    }
    
    ////////////////////// /////////////////////////////////////////////////////////////////////////////////////////
    
    
    public static Action useHealthToSurvive() {
        return action("use health item to stay alive").do2((MyAgentState S) -> (String itemId) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        

			boolean healthItemUsed = false;
			

      		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
      		
      		
      		healthItemUsed = true;
   
		    
	        if(healthItemUsed) {
	        	
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	        
	    })
		.on((MyAgentState S) -> { 
			WorldModel current = S.wom ;
	        //WorldModel old = S.previousWom ;

	        //String bowWeapon = "Bow";
	        //String swordWeapon = "Sword";

	        WorldEntity currentInv = current.getElement("Inventory"); 
	        //WorldEntity oldInv = old.getElement("Inventory"); 

	        String agentId = S.wom.agentId ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        //int bestWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");
	        
	        int currentHealth = agentCurrentState.getIntProperty("health") ;
	        
	        
	        if(currentHealth < 3 && currentHealth > 0) {
	        	System.out.println("Health::::---------------------------- "+ currentHealth);
	        	
	        	for(WorldEntity item_ : currentInv.elements.values()) {
	          		System.out.println("3. Iterating the inventory elements and looking for the needed item!");

		          	if( (item_.type.equals("Food") || item_.type.equals("Water") || item_.type.equals("HealthPotion")) ) {
		          		//System.out.println("AND HERE!");
		          		String itemId = item_.id;
		          	          		
		          		System.out.println("Item ID: " + itemId );
		          		System.out.println("Item Name: " + item_.type );
		          		
		          		//env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
		          		
		          		return itemId;
		          		
		       
		          		// move the part of if(foodFoundAndEaten)... in this if statement, no boolean needed
		          		
		          		//healthItemFoundAndUsed = true;
		          		
		          		
		          	
		          		// Freeze the Nethack window until Enter key is pressed. 
		          		// So we can see the progress of the goals in the actual game.
		          		//System.out.println("Hit RETURN to continue.") ;
		                //new Scanner(System.in) . nextLine() ;

		          		
		          	}
		          	
		          	
		         }
	        	
	        	
	        }
	        return null;


			
			
         }) ;
        
    }
    
    ////////////////////// /////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////// /////////////////////////////////////////////////////////////////////////////////////////
    
    
    public static Tactic collectHealthItemsIfNeeded(TestAgent agent, float monsterAvoidDistance) {
        Action deployNewGoal =  action("collect health items if not enough").do2((MyAgentState S) -> (String itemId) -> { 
        	
	        //MyEnv env_ = (MyEnv) S.env() ; 
	        WorldModel current = S.wom ;
        	String agentId = S.wom.agentId ; 
 	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
 	        Boolean isAlive = agentCurrentState.getBooleanProperty("isAlive"); //Indicating whether the avatar is alive or not
	        

			//boolean healthItemCollected = false;
			System.out.println("Health Item ID: "+ itemId);
			
			// deploy a new goal:
			//GoalStructure g2 = SEQ(entityVisited(itemId),GoalLib.pickUpItem());
			System.out.println(">>> deploying a new goal to get a health-item " + itemId) ;
			GoalStructure g2 = SEQ(
			    locationVisited_1(itemId,null,monsterAvoidDistance).lift(),
			    GoalLib.pickUpItem());
			agent.addBefore(g2) ;
			return S ;
	    })
		.on((MyAgentState S) -> { 
			
		    if (!S.isAlive()) return null ;
			
			WorldModel current = S.wom ;
	        WorldEntity inv = current.getElement("Inventory");
//	        String agentId = S.wom.agentId ; 
//	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
//	        Boolean isAlive = agentCurrentState.getBooleanProperty("isAlive"); //Indicating whether the avatar is alive or not

	        
	        
	        
	        int healthItemsCounter = 0;
	        String closestItemId = "";
	        
	        for(WorldEntity item_ : inv.elements.values()) {

	          	if( (item_.type.equals("Food") || item_.type.equals("Water") || item_.type.equals("HealthPotion")) ) {
	          		
	          		healthItemsCounter ++;
	          		
	          	}
	         }
	        
	        if (healthItemsCounter < 3) {
	        	System.out.println("Number of health items in inventory: "+ healthItemsCounter);
	        	
	        	
	        	// int minDistance = 70; //the maximum distance possible in our tile grid (90x50) /2
	        	int minDistance = 30; //the maximum distance possible in our tile grid (90x50) /2
                
	        	for(WorldEntity i : S.wom.elements.values()) {
	                 if(	(i.type.equals(HealthPotion.class.getSimpleName()) ) ||
	                		 (i.type.equals(Water.class.getSimpleName()) ) ||
	                		 (i.type.equals(Food.class.getSimpleName()) )
	                		 )
	                		  {	// looking for health items 
	                	 
	                	 //System.out.println("TYPE: "+ i.type);
	                	 
	                     // i is an item
	                	 
	                	 int ix = (int) i.position.x; 					// item's x coordinate
	                     int iy = (int) i.position.y;					// item's y coordinate
	                     int ax = (int) current.position.x;				// agent's x coordinate
	                     int ay = (int) current.position.y;				// agent's y coordinate
	                     
	                     int dx = (int) Math.abs(ax-ix) ; // agent-item distance in x axis
	                     int dy = (int) Math.abs(ay-iy) ; // agent-item distance in y axis
	                     
	                     
	                     
	                     if (dx + dy < minDistance) {
	                    	 
	                    	 minDistance = dx + dy;
	                    	 
	                    	 closestItemId = i.id;
	                    	 
	                    	 
	                    	 System.out.println("Health Item's id: "+i.id);
	                    	 System.out.println("Health Item's type: "+i.type);
	                    	 System.out.println("Health Item's position: "+i.position);
	                    	 
	                         
	                         //return i.id ;
	                     }
	                 }
	                 
	             }
	        	return closestItemId;
	        	
	        }
	        return null;
	        
         }) ;
        
        return SEQ(deployNewGoal.lift(), ABORT()) ;
        
    }
    
    ////////////////////// /////////////////////////////////////////////////////////////////////////////////////////
    
    public static Tactic abortIfDead() {
        Action abort = new Action.Abort() ;
        
        Action abortIfDead =  abort.on((MyAgentState S) -> { 
           
 	        // make the guard enabled when the character is dead:
			if (!S.isAlive()) {
			    return S ;
			}

			return null ;
	    });
		return abortIfDead.lift();
		
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * A goal to get an agent to the location of an non-monster entity.
     */
    public static GoalStructure entityVisited(TestAgent agent, String entityId, float monsterAvoidDistance) {
        return  locationVisited_2(agent,entityId,null,monsterAvoidDistance).lift() ;
    }
    
//    public static GoalStructure entityVisited1(String entityId) {
//        return  locationVisited1(entityId,null,0) ;
//    }
    
   // public static GoalStructure ok(Vec3 destination) {
    //    return  locationVisited(null,destination,0) ;
    //}
    
    public static GoalStructure locationVisited(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
        return locationVisited_2(agent,entityId,destination,monsterAvoidDistance).lift() ;
    }
    
    public static Goal locationVisited_1(String entityId, Vec3 destination, float monsterAvoidDistance) {
        
        String destinationName = entityId == null ? destination.toString() : entityId ;
        Goal g = goal(destinationName + " is visited") 
                .toSolve((MyAgentState S) -> {
                
              		//WorldEntity inv = S.wom.getElement("Inventory");
                	//int size = inv.elements.size();
        			//System.out.println("IInventory size: " + size );
                	
                	
                    Vec3 destination_ = destination ;
                    if(entityId != null) {
                        WorldEntity e = S.wom.getElement(entityId) ;
                        if(e == null) {
                            throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
                        }
                        destination_ = e.position ;
                    }
                    return Utils.sameTile(S.wom.position, destination_) ;
                })
                .withTactic(FIRSTof(
                		      abortIfDead(),
                			  useHealthToSurvive().lift(),
                			  equipBestAvailableWeapon().lift(),
                              bowAttack().lift(),
                              meleeAttack().lift(),
                              travelTo(entityId,destination,monsterAvoidDistance).lift(), 
                              travelTo(entityId,destination,0).lift(), 
                              ABORT()));
        
        return g ;
    }
    
    
    
    
    
    public static Goal locationVisited_2(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
        Goal g = locationVisited_1(entityId,destination,monsterAvoidDistance) ;
        Tactic baseTactic = g.getTactic() ;
        Tactic extendedTactic = FIRSTof(
                collectHealthItemsIfNeeded(agent,monsterAvoidDistance), // won't be enabled if dead
                baseTactic // will abort if dead
                ) ;                       
        return g.withTactic(extendedTactic) ; 
    }
   
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    ////////////////////////
    
    public static GoalStructure entityVisited_5_level(TestAgent agent, String entityId, float monsterAvoidDistance) {
        return  locationVisited_2_5_level(agent,entityId,null,monsterAvoidDistance).lift() ;
    }
    
    
    public static GoalStructure locationVisited_5_level(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
        return locationVisited_2_5_level(agent,entityId,destination,monsterAvoidDistance).lift() ;
    }
    
    public static Goal locationVisited_1_5_level(String entityId, Vec3 destination, float monsterAvoidDistance) {
        
        String destinationName = entityId == null ? destination.toString() : entityId ;
        Goal g = goal(destinationName + " is visited") 
                .toSolve((MyAgentState S) -> {
                
                	MyEnv env = (MyEnv) S.env() ;
                	
                	WorldModel current = S.wom ;
                	WorldModel previous = S.previousWom ;
                	
        	        String agentId = S.wom.agentId ;
        	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
        	        WorldEntity agentPreviousState = previous.elements.get(agentId) ;

        	        
        	        int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
        	        int previousLevel = agentPreviousState.getIntProperty("currentLevel");
                	
                	
                    Vec3 destination_ = destination ;
                    
//                    System.out.println("current level: ------------" + currentLevel);
//                    if (currentLevel != previousLevel) {
//                    	
//                    	env.nethackUnderTest.getNavigationGraph();
//                    	
//                    	
//                    }
                    if(entityId != null) {
                        WorldEntity e = S.wom.getElement(entityId) ;
                        if(e == null) {
                            throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
                        }
                        destination_ = e.position ;
                    }
                    return ( (Utils.sameTile(S.wom.position, destination_)) && (currentLevel==5) ) ;
                })
                .withTactic(FIRSTof(
                		      abortIfDead(),
                			  useHealthToSurvive().lift(),
                			  equipBestAvailableWeapon().lift(),
                              bowAttack().lift(),
                              meleeAttack().lift(),
                              travelTo(entityId,destination,monsterAvoidDistance).lift(), 
                              ABORT()));
        
        return g ;
    }
    
    
    
    
    
    public static Goal locationVisited_2_5_level(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
        Goal g = locationVisited_1_5_level(entityId,destination,monsterAvoidDistance) ;
        Tactic baseTactic = g.getTactic() ;
        Tactic extendedTactic = FIRSTof(
                collectHealthItemsIfNeeded(agent,monsterAvoidDistance), // won't be enabled if dead
                baseTactic // will abort if dead
                ) ;                       
        return g.withTactic(extendedTactic) ; 
    }
    
    
    
    ////////////////////////
    /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    public static GoalStructure closeToAMonster(TestAgent agent, String monsterId, float monsterAvoidDistance) {
   
        Goal g = goal("Close to monster " + monsterId)
                .toSolve((MyAgentState S) -> { 
                	
                	
                    WorldEntity m = S.wom.getElement(monsterId) ;
                    
                    if (m!=null) {
	                    
	                    int dx = (int) Math.abs(m.position.x - S.wom.position.x) ;
	                    int dy = (int) Math.abs(m.position.y - S.wom.position.y) ;
	                    boolean monsterIsAlive = m.getBooleanProperty("alive");
	                    
	                    
	                    return  (dx + dy == 1) || (!monsterIsAlive) ;
                    }
                    else return true;
                 })
                .withTactic(FIRSTof(
                		abortIfDead(),
                		collectHealthItemsIfNeeded(agent,monsterAvoidDistance),
                		useHealthToSurvive().lift(),
                		equipBestAvailableWeapon().lift(),
                		bowAttack().lift(),
                		meleeAttack().lift(),
                        travelToMonster(monsterId,monsterAvoidDistance).lift(), 
                        travelToMonster(monsterId,0).lift(), 
                        ABORT()));
        
        return g.lift() ;
    }

    
    // for debugging
    public static void debugPrintPath(Vec3 agentCurrentPosition, List<Vec3> path) {
        if(path == null) {
            System.out.println(">>> Path to follow is null.") ;
            return ;
        }
        if(path.isEmpty()) {
            System.out.println(">>> Path to follow is empty.") ;
            return ;
        }
        System.out.println(">>> Path to follow, size:" + path.size()) ;
        System.out.println("       agent @" + agentCurrentPosition) ;
        System.out.println("       path[0]: " + path.get(0)) ;
        System.out.println("       last: " + path.get(path.size() - 1)) ;
        int duplicates = 0 ;
        for(int k = 0 ; k<path.size()-1; k++) {
            for (int m = k+1; m<path.size(); m++) {
                var u1 = path.get(k) ;
                var u2 = path.get(m) ;
                if(Utils.sameTile(u1,u2)) {
                    duplicates++ ;
                    System.out.println("         Duplicated: " + u1) ;
                }
            }
        }
        System.out.println("       duplicates: " + duplicates) ;
        if (duplicates>0) throw new Error("The Pathdfinder produces a path containing duplicate nodes!!") ;
    }

}

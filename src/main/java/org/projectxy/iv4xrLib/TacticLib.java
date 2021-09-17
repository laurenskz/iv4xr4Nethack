package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static nl.uu.cs.aplib.AplibEDSL.action;

import java.util.List;

import org.projectxy.iv4xrLib.NethackWrapper.Interact;
import org.projectxy.iv4xrLib.NethackWrapper.Movement;

import A.B.Food;
import A.B.HealthPotion;
import A.B.Monster;
import A.B.Water;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.W3DAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic;

public class TacticLib {

	/**
	 * This constructs a tactic; when invoked by an agent, the tactic will return the observed state of the SUT.
	 * 
	 * The tactic is always enabled.
	 */
	public static Tactic observe() { 
		Action observe = 
				// lets first define an "action" that will do the "observe" :
				action("Observe")

				// specify what the action does. Conceptually, it is a function from agent-state to agent-state,
				// which may in between also update this state as well (not the case here, but it can):

				.do1((W3DAgentState belief) -> {
					// Since MyEnv already provide a method observe(), we can invoke it like this:
					//
					//     belief.env().observe(belief.wom.agentId);
					//
					// BUT the agent's runtime automatically call it anyway to refresh the agent
					// state. So we can just do:

					return belief;
				});

		// ok so you have an action; but what you want is a Tactic. We can lift an action to
		// turn it to a tactic:
		return observe.lift();
	}

	/**
	 * This constructs a tactic. When it is invoked by an agent, it will cause the agent to interact with
	 * the given entity.
	 * 
	 * The tactic is only enabled when interaction is possible. You have to implement a proper guard that
	 * can decide/predict if interaction would be possible or otherwise.
	 
	public static Tactic interact(String entityId) { 

		Tactic interact = 
				// let's first define an action that will do "interact". Your MyEnv provides such a method:
				action("Interact")

				// specify what the action does. Conceptually, it is a function from agent-state to agent-state,
				// which may in between also update this state as well:

				. do2((W3DAgentState belief) -> (WorldEntity e) -> {

					// invoke the method inteact() in MyEnv, and stored the resulting observed
					// SUT-state in some variable:
				    throw new UnsupportedOperationException() ;
					//var obs = ((MyEnv) belief.env()).interact(belief.wom.agentId,entityId);

					// We can alternatively do this through wom.interact(), which will eventually forward
					// the call to env.interact():
					// var obs = belief.wom.interact(belief.env(),"Interact", e);


					// we will merge the obtained observation into belief.wom. This wom acts as
					// aggregate of all observations collected so far:
					//belief.wom.mergeNewObservation(obs);

					//return belief;
				})

				// Next we need to set a guard, when "interact" would be possible. E.g. the SUT might not
				// allow "interact" if the agent is still to far from the target entity:

				. on((W3DAgentState belief) -> {

					// some check here if the agent would be able to interact. If so, return the entity
					// else null:

					var e = belief.wom.getElement(entityId);
					if (e==null) return null;
					// check if e can be interacted to, e.g. if it is within some distance epsilon:
					float epsilon = 0.5f;
					boolean canInteract = Vec3.dist(e.position,belief.wom.position) <= epsilon;

					return canInteract ? e : null;

				})

				// finally, lift the action to turn it to a tactic:
				. lift();

		return interact;

	}
	*/

	public static Action bowAttack() {
	        return action("bow-attack").do2((MyAgentState S) -> (Vec3 monsterLocation) -> {
	        	
	        	//S.setAPathToFollow(path) ;
	            
	            MyNethackEnv env = (MyNethackEnv) S.env() ;
	            Vec3 agentCurrentPosition = S.wom.position ;
	            int mx = (int) monsterLocation.x; 					// monster's x coordinate
	            int my = (int) monsterLocation.y;					// monster's y coordinate
	            int ax = (int) agentCurrentPosition.x;				// agent's x coordinate
	            int ay = (int) agentCurrentPosition.y;				// agent's y coordinate
	            
	            // int dx = (int) (monsterLocation.x - agentCurrentPosition.x) ;
	            // int dy = (int) (monsterLocation.y - agentCurrentPosition.y) ;
	            
	            System.out.println(">>> FIRING BOW") ;
	            
//	            int previousMonsterLife = 0;
//	            int currentMonsterLife = 10;
//	            int playerAttackDamage = 0;
	            
	            
	            
//	            for(WorldEntity e : S.wom.elements.values()) {
//	                 if(e.type.equals(Monster.class.getSimpleName())) {
//	                	 
//	                	 if (	(e.position.x == monsterLocation.x) && (e.position.y == monsterLocation.y)	) {
//	                		 
//	                		 WorldModel current = S.wom ;
//	        	 	         String monsterId = e.id ;
//	        	 	         WorldEntity monsterCurrentState = current.elements.get(monsterId) ;
//	                		 
//	                		 
//	                		 previousMonsterLife =monsterCurrentState.getIntProperty("health");
//	                		 System.out.println("Monster previous life: "+ previousMonsterLife);
//	                		 
//	                		 
//	                	 }
//	                	 
//	                 }
//	            }
	            
	            
	            
	            
	            
	            
	            
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
	            
	            
	            
//	            for(WorldEntity e : S.wom.elements.values()) {
//	                 if(e.type.equals(Monster.class.getSimpleName())) {
//	                	 
//	                	 if (	(e.position.x == monsterLocation.x) && (e.position.y == monsterLocation.y)	) {
//	                		 
//	                		 String monsterId = e.id ;
//	                		 
//	                		 
//	                		 WorldModel current = S.wom ;
//	        	 	         WorldEntity monsterCurrentState = current.elements.get(monsterId) ;
//	                		 currentMonsterLife = monsterCurrentState.getIntProperty("health");
//	                		 
//	                		 System.out.println("Monster current life: "+ currentMonsterLife);
//	                		 
//	                		 
//	                	
//	                		 
//	                		 
//	                		 
//	                	 }
//	                	 
//	                 }
//	            }
	            
	            
	            
	            
	            
	            // let's also reset the planned path, if there is any:
	            S.currentPathToFollow = null ;
	            S.updateState() ;
	            
	            
//	            for(WorldEntity e : S.wom.elements.values()) {
//	                 if(e.type.equals(Monster.class.getSimpleName())) {
//	                	 
//	                	 if (	(e.position.x == monsterLocation.x) && (e.position.y == monsterLocation.y)	) {
//	                		 
//	                		 String monsterId = e.id ;
//	                		 
//	                		 
//	                		 WorldModel previous = S.previousWom ;
//	        	 	         WorldEntity monsterPreviousState = previous.elements.get(monsterId) ;
//	                		 previousMonsterLife = monsterPreviousState.getIntProperty("health");
//	                		 
//	                		 System.out.println("Monster previous life: "+ previousMonsterLife);
//	                		 
//	                		 
//	                		 
//	                	 }
//	                	 
//	                 }
//	            }
//	            
//	            
//	            playerAttackDamage = previousMonsterLife - currentMonsterLife;
//	            System.out.println("Player's attack Damage: "+ playerAttackDamage);
//	            
	            
	            
	            return S ;
	          }) 
	        		
	        		
	          .on((MyAgentState S) -> { 
	             if (! S.isAlive()) return null ;
	             
	            
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
		                 				//System.out.println("isWall: "+ isWall);
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

	/**
	 * When the agent's has less than three health-items, and there is a health-item H close enough
	 * to the agent, this tactic will deploy a new goal to get H first. The current goal is aborted.
	 * After H is picked-up (or if H no longer exist), the agent resumes the current goal.
	 */
	public static Tactic collectHealthItemsIfNeeded(TestAgent agent, float monsterAvoidDistance) {
	    Action deployNewGoal =  action("collect health items if not enough").do2((MyAgentState S) -> (String itemId) -> { 
	    	
			System.out.println("Health Item ID: "+ itemId);
			
			System.out.println(">>> deploying a new goal to get a health-item " + itemId) ;
			GoalStructure g2 = 
			    FIRSTof(
			        SEQ(GoalLib.locationVisited_1(itemId,null,monsterAvoidDistance).lift(),
			            GoalLib.pickUpItem()),
			        SUCCESS());
			agent.addBefore(g2) ;
			return S ;
	    })
		.on((MyAgentState S) -> { 
			
		    if (!S.isAlive()) return null ;
			
			WorldModel current = S.wom ;
	        WorldEntity inv = current.getElement("Inventory");
	        String agentId = S.wom.agentId ; 
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
	        
	        int healthItemsCounter = 0;
	        String closestItemId = null ;
	        
	        
	        if (currentLevel%5 !=0) {
	        	
	        	//System.out.println("currentLevel%5: "+ currentLevel%5);
	        
		        for(WorldEntity item_ : inv.elements.values()) {
	
		          	if( (item_.type.equals("Food") || item_.type.equals("Water") || item_.type.equals("HealthPotion")) ) {
		          		
		          		healthItemsCounter ++;
		          		
		          	}
		         }
		        
		        if (healthItemsCounter < 3) {
		        	
		        	
		        	System.out.println("Number of health items in inventory: "+ healthItemsCounter);
		        	
		        	int minDistance = 30; //the maximum distance possible in our tile grid (90x50) /2
		        	WorldEntity stairs = S.wom.getElement("Stairs") ;
		        	for(WorldEntity i : S.wom.elements.values()) {
		        	     if(	(i.type.equals(HealthPotion.class.getSimpleName()) ) ||
		                		 (i.type.equals(Water.class.getSimpleName()) ) ||
		                		 (i.type.equals(Food.class.getSimpleName()) )
		                		 )
		                		  {	// looking for health items 
		                	 
		                	 //System.out.println("TYPE: "+ i.type);
	
		        	         // ignore it if it is a health item which happens to be ON the stairs:
		                     if(stairs!=null && Utils.sameTile(stairs.position, i.position)) continue ;
		                       
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
		        	if(closestItemId != null) return closestItemId;
		        	
		        }
	        }
	        return null;
	        
	     }) ;
	    
	    return SEQ(deployNewGoal.lift(), ABORT()) ;
	    
	}

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

	public static Tactic checkIfEntityNoLongerExists(String entityId) {
	    Action check = action("Checking if entity id " + entityId + " is not on the map anymore.") 
	            .do1((MyAgentState S) -> S) 
	            .on((MyAgentState S) -> {
	                if(entityId == null) return null ;
	                if(S.wom.getElement(entityId) == null) {
	                    return S ;
	                }
	                return null ;
	            }) ;
	    
	    return check.lift() ;
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
	        MyNethackEnv env = (MyNethackEnv) S.env() ;
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
	                    // throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
	                    System.out.println(">>> Trying to travel to entity " + entityId + ", but does not exists!") ;
	                    return null ;
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
	                
	                if(entityId!="Stairs") {
	                    // unless the goal to travel to is the stairs, we will instruct
	                    // the pathfinder to avoid stairs:
	                    WorldEntity theStairs = S.wom.getElement("Stairs") ;                   	
	                	((MyNavGraph) S.simpleWorldNavigation).setStairsAvoid(theStairs) ;
	                }
	                
	                path0 = S.getPath(agentCurrentPosition,destination_) ;
	                //System.out.print(">>> agent @" +  agentCurrentPosition) ;
	                //System.out.print(">>> Planing path to " +  destination_) ;
	                //System.out.println(", #" + path0.size()) ;
	                ((MyNavGraph) S.simpleWorldNavigation).resetMonstersDangerArea() ;
	                ((MyNavGraph) S.simpleWorldNavigation).resetStairsAvoid() ;
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
	            
	            //((MyNavGraph) S.simpleWorldNavigation).setStairsAvoidArea(1,null) ;
	            	
	            
	            
	            // we can't literally move to the monster's tile; so we choose a tile next to it:
	            Vec3[] candidates = { 
	                      Vec3.add(e.position, new Vec3(1,0,0)), 
	                      Vec3.add(e.position, new Vec3(-1,0,0)), 
	                      Vec3.add(e.position, new Vec3(0,1,0)), 
	                      Vec3.add(e.position, new Vec3(0,-1,0)) 
	                   };
	            
	            
	            WorldEntity theStairs = S.wom.getElement("Stairs") ;                       
	            ((MyNavGraph) S.simpleWorldNavigation).setStairsAvoid(theStairs) ;
	            
	            
	            
	            for(Vec3 targetLocation : candidates) {
	                //System.out.println(">>>> candidate " + targetLocation) ;
	                if (S.simpleWorldNavigation.vertices.contains(targetLocation)) {
	                    path0 = S.getPath(agentCurrentPosition,targetLocation) ;
	                    //System.out.println(">>>> in navgraph; path: " + path0) ;
	                    if (path0 != null) {
	                        // found a reachable neighbor-tile
	                        break ;
	                    }
	                }
	            }
	            ((MyNavGraph) S.simpleWorldNavigation).resetMonstersDangerArea() ;
	            ((MyNavGraph) S.simpleWorldNavigation).resetStairsAvoid() ;
	            return path0 ;
	        }) ;
	    }

	/**
	 * Will attack a monster, if there is one in a neighboring tile. Will use melee attack.
	 */
	public static Action meleeAttack() {
	    return action("melee-attack").do2((MyAgentState S) -> (Vec3 monsterLocation) -> {
	        MyNethackEnv env = (MyNethackEnv) S.env() ;
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

	//////////////////
	    public static Action equipBestAvailableWeapon() {
	        return action("equip Best Available Weapon").do2((MyAgentState S) -> (String itemId) -> { 
		        MyNethackEnv env_ = (MyNethackEnv) S.env() ;
		        WorldModel current = S.wom ;
		        
				boolean bestWeaponEquipped = false;
				
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
		       
		        String bowWeapon = "Bow";
		        String swordWeapon = "Sword";
	
		        WorldEntity currentInv = current.getElement("Inventory"); 
		        
		        String agentId = S.wom.agentId ;
		        WorldEntity agentCurrentState = current.elements.get(agentId) ;
		        int bestWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");
	
		        for(WorldEntity item_ : currentInv.elements.values()) {
	          		//System.out.println("In inv: " + item_.type + ","+ item_.id);
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
			          		
			          	}
			          	
	          		}
		         }
				
		        return null;
				
	         }) ;
	        
	    }

	/**
	 * A tactic that uses a health-item to increase the agent's health, if the agent's health is too low, 
	 * and it has a health-item in its inventory.
	 */
	public static Action useHealthToSurvive() {
	    return action("use health item to stay alive").do2((MyAgentState S) -> (String itemId) -> { 
	        MyNethackEnv env_ = (MyNethackEnv) S.env() ;
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
	        
	        
	        if(currentHealth <= 3 && currentHealth > 0) {
	        	//System.out.println("Health::::---------------------------- "+ currentHealth);
	        	
	        	//System.out.println("3. Iterating the inventory elements and looking for the needed item! #Inv:" + currentInv.elements.size());
	
	        	for(WorldEntity item_ : currentInv.elements.values()) {
	          		
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

	
	public static Action loadNewLevel() {
	return action("reload navigation grapgh when moving to a new level").do1((MyAgentState S) -> { 
		
	MyNethackEnv env_ = (MyNethackEnv) S.env() ;
		
	
	boolean levelLoaded = false;
	
	
	S.setUpNextLevel(env_);
	S.updateState() ;
	
	levelLoaded = true;
	
	System.out.println(">>> a new level is loaded.") ;
	for (WorldEntity e : S.wom.elements.values()) {
		System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
	}
	
	
	
	
	if(levelLoaded) {
		
	    S.updateState() ;
	    return S ;
	}
	else {
	    return null ;
	}	
		
	
	
	
	})
	.on((MyAgentState S) -> { 
	
	WorldModel current = S.wom ;
	WorldModel previous = S.previousWom ;
	
	
	String agentId = S.wom.agentId ;
	WorldEntity agentCurrentState = current.elements.get(agentId) ;
	WorldEntity agentPreviousState = previous.elements.get(agentId) ;
		
	int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
	int previousLevel = agentPreviousState.getIntProperty("currentLevel");	
	
	
	if (currentLevel>previousLevel) {
		return true;
		
	}
	
	return null;
	
	}) ;
	
	}

	public static Tactic killBossFirst(TestAgent agent) {
	    Action deployNewGoal =  action("kill boss first").do2((MyAgentState S) -> (String targetEntityId) -> { 
	    	
			System.out.println("Boss ID: "+ targetEntityId);
			
			// deploy a new goal:
			System.out.println(">>> deploying a new goal to kill the boss " + targetEntityId) ;
			
			GoalStructure g2 = GoalLib.closeToAMonster(agent, targetEntityId, 0);
			agent.addBefore(g2) ;
			return S ;
	    })
		.on((MyAgentState S) -> { 
			
		    if (!S.isAlive()) return null ;
			
			WorldModel current = S.wom ;
			
			String agentId = S.wom.agentId ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        	        
	        int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
	        
	        int NumberOfMonsters = 0;
	        
	        for (WorldEntity e: current.elements.values()) {
	        	
				if	( e.type.equals(Monster.class.getSimpleName()) ) 
	            {
					NumberOfMonsters++;
	            }
				
			}
	        
	        
	        if ( ((currentLevel %5) == 0) && (NumberOfMonsters==1) ) {
	        	
	        	//System.out.println("currentLevel %5: " + currentLevel %5);
	        	
	        	WorldEntity targetEntity = null;
				for (WorldEntity e: current.elements.values()) {
					
					if	(e.type.equals(Monster.class.getSimpleName())  ) 
		            {
						
						
						System.out.println("######### THERE IS A BOSS AT THIS LEVEL!");
						System.out.println("At position: "+ e.position);
	
						targetEntity = e ;
						break ;
		            }
					
					
				}
				
				if (targetEntity == null) { 
					
					System.out.println("########  NO BOSS AT THIS LEVEL");
					
					return null;
					
				}
				
				return targetEntity.id;
				
	        }
	        
	        return null;
	
	
	     }) ;
	    
	    return SEQ(deployNewGoal.lift(), ABORT()) ;
	    
	}

}

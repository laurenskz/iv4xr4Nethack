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
import java.util.function.Function;

import org.projectxy.iv4xrLib.NethackWrapper.Interact;
import org.projectxy.iv4xrLib.NethackWrapper.Movement;

import A.B.Monster;
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
                        || ! Utils.sameTile(destination_,path0.get(path0.size()-1))) {
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
            if(path0 != null) {
                Vec3 last = path0.get(path0.size() - 1) ;
                int dx = (int) Math.abs(last.x - agentCurrentPosition.x) ;
                int dy = (int) Math.abs(last.y- agentCurrentPosition.y) ;
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
            
            
            if(mx == ax) {
            	
            	if(my > ay) {
            		
            		int dy = my=ay;
            		
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.DOWN) ;	
            	}
            	else {
            		
            		int dy = ay=my;
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.UP) ;	
            		
            	}
            	
            	
                //System.out.println(">>> Attack R") ;
                //env.move(Movement.RIGHT) ;
            }
            else if (my==ay) {
            	
            	
            	if(mx > ax) { 
            		
            		int dx = mx-ax;
            		
            		env.interact(S.wom.agentId, null, Interact.AimWithBow);
            		env.move(Movement.RIGHT) ;	
            	}
            	
            	else {
//            		int dx = ax-mx;
//            		int isWall = dx;
//            		
//            		List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
//            		
//            		
//            		for (int i = mx+1; i<ax-1 ; i++) {
//            			for (Vec3 v : walkableTiles) {
//            				
//            				if(Utils.sameTile(v, new Vec3(i, my, 0))) {
//            					
//            					isWall--;
//            					System.out.println("isWall: "+ isWall);
//            					//break;
//            					
//            				}		
//            			}
//            		}
//            				
//            		
//            		if (isWall <= 2) {
//            			
//            			env.interact(S.wom.agentId, null, Interact.AimWithBow);
//                		env.move(Movement.LEFT) ;	
//            			
//            		}
//            			
//            	}
//            	
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
                     
             		 List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
             		 int dx = Math.abs(ax-mx);
             		 int dy = Math.abs(ay-my);
             		 int isWall;

                     
                     
                     if (	(mx == ax || my == ay) 	) {
                    	 
                    	 
//                    	 int dx = Math.abs(ax-mx);
//                    	 int dy = Math.abs(ay-my);
//                    	 int isWall;
                    	 
                    	 if (mx<ax) {
	                 		 isWall = dx;
	                 		
	                 		 //List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
	                 		
	                 		
	                 		 for (int i = mx; i<ax ; i++) {
	                 			 for (Vec3 v : walkableTiles) {
	                 				
	                 				 if(Utils.sameTile(v, new Vec3(i, my, 0))) {
	                 					
	                 					 isWall--;
	                 					 System.out.println("isWall: "+ isWall);
	                 					 //break;
	                 					
	                 				 }		
	                 			 }
	                 		 }
	                 		 
                    	 }
                    	 else if(mx>ax) {
                    		 
                    		 isWall = dx;
 	                 		
	                 		 //List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
	                 		
	                 		
	                 		 for (int i = ax; i<mx ; i++) {
	                 			 for (Vec3 v : walkableTiles) {
	                 				
	                 				 if(Utils.sameTile(v, new Vec3(i, my, 0))) {
	                 					
	                 					 isWall--;
	                 					 System.out.println("isWall: "+ isWall);
	                 					 //break;
	                 					
	                 				 }		
	                 			 }
	                 		 }
                    		 
                    		 
                    	 }
                    	 else {
	                 		 isWall = dy;

                    		 if (my<ay) {
    	                 		 //isWall = dy;
    	                 		
    	                 		 //List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
    	                 		
    	                 		
    	                 		 for (int i = my; i<ay ; i++) {
    	                 			 for (Vec3 v : walkableTiles) {
    	                 				
    	                 				 if(Utils.sameTile(v, new Vec3(mx, i, 0))) {
    	                 					
    	                 					 isWall--;
    	                 					 System.out.println("isWall: "+ isWall);
    	                 					 //break;
    	                 					
    	                 				 }		
    	                 			 }
    	                 		 }
    	                 		 
                        	 }
                    		 else if (my>ay){
                    			 
                    			 //isWall = dy;
     	                 		
    	                 		 //List<Vec3> walkableTiles = S.simpleWorldNavigation.vertices;
    	                 		
    	                 		
    	                 		 for (int i = ay; i<my ; i++) {
    	                 			 for (Vec3 v : walkableTiles) {
    	                 				
    	                 				 if(Utils.sameTile(v, new Vec3(mx, i, 0))) {
    	                 					
    	                 					 isWall--;
    	                 					 System.out.println("isWall: "+ isWall);
    	                 					 System.out.println("Position: "+ i +","+ mx);
    	                 					 //break;
    	                 					
    	                 				 }		
    	                 			 }
    	                 		 }
                    			 
                    			 
                    		 }
                    		 
                    		 
                    		 
                    	 }
                 				
                 		
                 		 if (isWall <= 2) {
                 			
                 			return e.position ;
                 			
                 		 }
                    	 
                    	 
                    	 
                    	 
                    	 
                    	 
                    	 
                    	 
                         // then the monster is vertically or horizontally across our agent
                         //System.out.println(">>> monster is near: " + e.position) ;
                         
                     }
                 }
             }
             return null ;       
          }) ;
    }
    
    //////////////////
    
    
    
    /**
     * A goal to get an agent to the location of an non-monster entity.
     */
    public static GoalStructure entityVisited(String entityId) {
        return  locationVisited(entityId,null,0) ;
    }
    
    public static GoalStructure locationVisited(Vec3 destination) {
        return  locationVisited(null,destination,0) ;
    }
    
    public static GoalStructure locationVisited(String entityId, Vec3 destination, float monsterAvoidDistance) {
        
        String destinationName = entityId == null ? destination.toString() : entityId ;
        Goal g = goal(destinationName + " is visited") 
                .toSolve((MyAgentState S) -> {
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
                              bowAttack().lift(),
                              travelTo(entityId,destination,monsterAvoidDistance).lift(), 
                              ABORT()));
        
        return g.lift() ;
    }
    
    public static GoalStructure closeToAMonster(String monsterId, float monsterAvoidDistance) {
   
        Goal g = goal("Close to monster " + monsterId)
                .toSolve((MyAgentState S) -> { 
                    WorldEntity m = S.wom.getElement(monsterId) ;
                    int dx = (int) Math.abs(m.position.x - S.wom.position.x) ;
                    int dy = (int) Math.abs(m.position.y - S.wom.position.y) ;
                    boolean monsterIsAlive = m.getBooleanProperty("alive");
                    
                    
                    return  (dx + dy == 1) || (!monsterIsAlive)   ;
                 })
                .withTactic(FIRSTof(
                		bowAttack().lift(),
                        travelToMonster(monsterId,monsterAvoidDistance).lift(), 
                        ABORT()));
        
        return g.lift() ;
    }


}

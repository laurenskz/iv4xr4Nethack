package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.*;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.*;
import nl.uu.cs.aplib.utils.Pair;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import java.util.List;

import org.projectxy.iv4xrLib.NethackWrapper.Movement;

public class Utils {

    /**
     * Conver a 3D coordinate p to a discrete tile-world coordinate. Basically, p
     * will be converted to a pair of integers (p.x,p.y).
     */
    static Pair<Integer, Integer> toTileCoordinate(Vec3 p) {
        return new Pair((int) p.x, (int) p.y);
    }
    
    static Vec3 toVec3(int x, int y) {
        return new Vec3(x,y,0) ;
    }
    
    static boolean sameTile(Vec3 t1, Vec3 t2) {
        Pair<Integer, Integer> p1 = toTileCoordinate(t1) ;
        Pair<Integer, Integer> p2 = toTileCoordinate(t2) ;
        return p1.fst.equals(p2.fst) && p1.snd.equals(p2.snd) ;
    }
    
    static Integer vec3ToNavgraphIndex(Vec3 p, SimpleNavGraph nav) {
        for(int i=0; i<nav.vertices.size(); i++) {
            if(sameTile(p,nav.vertices.get(i))) return i ;
        }
        return null ;
    }
 

    static Action travelTo(String entityId, Vec3 destination) {
        
        return action("plan a path").do2((MyAgentState S) -> (List<Vec3> path) -> {
            
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
                    path0 = S.getPath(agentCurrentPosition,destination_) ;
                    if(path0 == null) {
                        return null ;
                    }
                }    
                return path0 ;} )
                
                ;
    }
    
    public static GoalStructure entityVisited(String entityId) {
        return  locationVisited(entityId,null) ;
    }
    
    public static GoalStructure locationVisited(Vec3 destination) {
        return  locationVisited(null,destination) ;
    }
    
    public static GoalStructure locationVisited(String entityId, Vec3 destination) {
        
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
                .withTactic(travelTo(entityId,destination).lift());
        
        return g.lift() ;
    }


}

package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import java.util.List;

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
 
    static Action forcePlanPath(Vec3 destination) {
        return action("plan a path").do1((MyAgentState S) -> {
            Vec3 agentCurrentPosition = S.wom.position ;
            List<Vec3> path = S.getPath(agentCurrentPosition, destination) ;
            if(path == null) return null ;
            S.setAPathToFollow(path) ;
            return S ;
            }) ;
    }
    
    static Action travelTo(Vec3 destination) {
        
        return action("plan a path").do1((MyAgentState S) -> {
            Vec3 agentCurrentPosition = S.wom.position ;
            List<Vec3> path = S.getPath(agentCurrentPosition, destination) ;
            if(path == null) return null ;
            S.setAPathToFollow(path) ;
            return S ;
            }) ;
        
        
        //Action forcePlan = forcePlanPath(destination) ;
        //forcePlan = forcePlan.on_((MyAgentState S)  -> S.currentPathToFollow == null) ;
    }


}

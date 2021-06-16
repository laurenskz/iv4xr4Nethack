package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.utils.Pair;
import java.util.List;


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

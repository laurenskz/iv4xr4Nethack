package org.projectxy.iv4xrLib;

import java.util.Collection;

import A.B.Monster;
import eu.iv4xr.framework.spatial.Box;
import eu.iv4xr.framework.spatial.Line;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Vec3;

/**
 * A wrapper over a stair, to have a bounding box of some size, so that the
 * agent can avoid it when it does not want to go the next level.
 */
public class StairWrapper implements  LineIntersectable {
    
    private Box box ;
    
    public Vec3 getPosition() { return box.center ; }
    
    public StairWrapper(int x, int y) {
        Vec3 center = new Vec3(0,0,0) ;
        center.x = x ;
        center.y = y ;
        Vec3 width = new Vec3(1,1,1) ;
        box = new Box(center,width) ;
    }
    
    @Override
    public Collection<Vec3> intersect(Line line) {
        return box.intersect(line) ;
    }
}

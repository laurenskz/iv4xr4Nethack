package org.projectxy.iv4xrLib;

import java.util.Collection;

import A.B.StairTile;
import A.B.Screen;
import eu.iv4xr.framework.spatial.*;


public class StairsWrapper implements  LineIntersectable {
    
    
    StairTile stair ;
    private Box box ;
    
    public StairsWrapper (StairTile s) {
    	stair = s ;
        Vec3 center = new Vec3(0,0,0) ;
        Vec3 width = new Vec3(1,1,1) ;
        box = new Box(center,width) ;
    }
    
    public Vec3 getAvoidanceBox() {
        return box.width ;
    }
    
    public void setAvoidanceDistance(float adist) {
        box.width.x = adist ;
        box.width.y = adist ;
        box.width.z = adist ;    
    }

    @Override
    public Collection<Vec3> intersect(Line line) {
        box.center.x = stair.x;
        box.center.y = stair.y ;
        return box.intersect(line) ;
    }


}

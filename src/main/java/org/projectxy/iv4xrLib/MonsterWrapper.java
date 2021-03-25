package org.projectxy.iv4xrLib;

import java.util.Collection;

import A.B.Monster;
import eu.iv4xr.framework.spatial.*;

/**
 * A wrapper over a monster, to have a bounding box of some size, just in case we need
 * to implement path-planing that avoids monsters.
 *
 */
public class MonsterWrapper implements  LineIntersectable {
    
   
    Monster monster ;
    private Box box ;
    
    public MonsterWrapper(Monster m) {
        monster = m ;
        box.center = new Vec3(0,0,0) ;
        box.width = new Vec3(1,1,1) ;
    }
    
    public Vec3 getAvoidanceBox() {
        return box.width ;
    }
    
    public void setAvoidanceDistance(int adist) {
        box.width.x = adist ;
        box.width.y = adist ;
        box.width.z = adist ;    
    }

    @Override
    public Collection<Vec3> intersect(Line line) {
        box.center.x = monster.x ;
        box.center.y = monster.y ;
        return box.intersect(line) ;
    }


}

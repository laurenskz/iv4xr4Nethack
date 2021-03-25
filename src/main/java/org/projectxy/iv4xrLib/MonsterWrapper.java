package org.projectxy.iv4xrLib;

import java.util.Collection;

import A.B.Monster;
import eu.iv4xr.framework.spatial.*;

public class MonsterWrapper implements  LineIntersectable {
    
   
    Monster monster ;
    private Box box ;
    
    public MonsterWrapper(Monster m) {
        monster = m ;
        box.center = new Vec3(0,0,0) ;
        box.width = new Vec3(0.5f,0.5f,0.5f) ;
    }

    @Override
    public Collection<Vec3> intersect(Line line) {
        box.center.x = monster.x ;
        box.center.y = monster.y ;
        return box.intersect(line) ;
    }


}

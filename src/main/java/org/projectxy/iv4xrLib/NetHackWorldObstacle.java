package org.projectxy.iv4xrLib;

import A.B.Monster;
import eu.iv4xr.framework.spatial.Box;
import eu.iv4xr.framework.spatial.Vec3;

public class NetHackWorldObstacle extends Box {
    
    public NetHackWorldObstacle(Vec3 center, Vec3 width) {
        super(center, width);
        // TODO Auto-generated constructor stub
    }
    

    Monster monster ;
    
    public NetHackWorldObstacle(Monster m) {
        monster = this ;
    }


}

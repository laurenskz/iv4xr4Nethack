package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;

/**
 * A variation of SimpleNavGraph; in this variation we will use mahattan
 * distance rather than geometric distance. Mahattan is more suitable for
 * tile-based worlds where you cannot move diagonally.
 */
public class MyNavGraph extends SimpleNavGraph {

    /**
     * The distance between two NEIGHBORING vertices. If the connection is not
     * blocked, it is defined to be their Manhattan distance, and else +inf.
     */
    @Override
    public float distance(int from, int to) {
        // use super to check if the taget vertex is blocked:
        float dist0 = super.distance(from, to);
        if (dist0 < Float.POSITIVE_INFINITY) {
            // so the connection is not blocked:
            Vec3 p1 = vertices.get(from);
            Vec3 p2 = vertices.get(to);
            return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
        } else {
            return dist0;
        }
    }

    /**
     * Heuristic distance between any two vertices. Here it is chosen to be the
     * Manhattan distance between them.
     */
    @Override
    public float heuristic(int from, int to) {
        Vec3 p1 = vertices.get(from);
        Vec3 p2 = vertices.get(to);
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
    }
    
    public void setMonstersDangerArea(float areaWidth) {
        setMonstersDangerArea(areaWidth,null) ;
    }
    
    public void setMonstersDangerArea(float areaWidth, String exceptThisOne) {
        for(Obstacle<LineIntersectable> o : obstacles) {
            MonsterWrapper monster = (MonsterWrapper) o.obstacle ; 
            if(exceptThisOne!=null && monster.monster.ID.equals(exceptThisOne)) continue ;
            monster.setAvoidanceDistance(areaWidth);
        }
    }
    
    public void resetMonstersDangerArea() {
        setMonstersDangerArea(1f) ;
    }

}

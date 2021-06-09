package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
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
     * The stairs in the current level. This is to be treated as an obstacle,
     * unless we specifically want to travel to it.
     */
    Obstacle<LineIntersectable> stairs ;

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
     * Hmm... ok let's not do this. Keep using straight-line distance as heuristic.
    @Override
    public float heuristic(int from, int to) {
        Vec3 p1 = vertices.get(from);
        Vec3 p2 = vertices.get(to);
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
    }
    */
    
    public void setMonstersDangerArea(float areaWidth) {
        setMonstersDangerArea(areaWidth,null) ;
    }
    
    public void setMonstersDangerArea(float areaWidth, String exceptThisOne) {
        for(Obstacle<LineIntersectable> o : obstacles) {
            if(! (o.obstacle == null || o.obstacle instanceof MonsterWrapper)) continue ;
            MonsterWrapper monster = (MonsterWrapper) o.obstacle ; 
            if(exceptThisOne!=null && monster.monster.ID.equals(exceptThisOne)) continue ;
            monster.setAvoidanceDistance(areaWidth);
        }
    }
    
    public void resetMonstersDangerArea() {
        setMonstersDangerArea(1f) ;
    }
    
    
    public void setStairsAvoid(WorldEntity stairsx) {
        if (stairsx==null) {
            // the level does not have a stairs
            return ;
        }
        var position_ =  Utils.toTileCoordinate(stairsx.position) ;
        if (this.stairs == null) {
            this.stairs = new Obstacle(new StairWrapper(position_.fst, position_.snd)) ;
            obstacles.add(this.stairs) ;
        }
        else if(! Utils.sameTile(((StairWrapper) this.stairs.obstacle).getPosition(), stairsx.position)) {
            this.stairs.obstacle = new StairWrapper(position_.fst, position_.snd) ;      
        }
        this.stairs.isBlocking = true ;
        //System.out.println("========= re-inserting stairs-obstacle") ;
        //System.out.println("========= stairsx @" + stairsx.position) ;
        //System.out.println("========= obstaclewrapper @" + ((StairWrapper) this.stairs.obstacle).getPosition()) ;
     }
    
    public void resetStairsAvoid() {
    	if (stairs!=null) {
    		
    		this.stairs.isBlocking = false ;
    		
    		
    	}
    	
    	
    }


}

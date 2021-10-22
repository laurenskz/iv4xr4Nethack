package org.projectxy.iv4xrLib;

import A.B.NethackConfiguration;
import org.junit.jupiter.api.Test;

import A.B.Monster;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.utils.Pair;

public class nh_navgraph_Test {
    
    NethackWrapper driver ;
    // NH world is 90x50
    // world is transposed array:
    boolean[][] world = new boolean[50][90];
    
    @BeforeEach
    public void launchNH() {
        driver = new NethackWrapper() ;
        driver.launchNethack(new NethackConfiguration()) ;
        SimpleNavGraph nav = driver.getNavigationGraph() ;
        // fill in world:
        for(Vec3 node : nav.vertices) {
            Pair<Integer,Integer> t = Utils.toTileCoordinate(node) ;
            world[t.snd][t.fst] = true ;
        }
    }
    
    @AfterEach
    public void closeNH() {
        driver.closeNethack();
    }
    
    /**
     * Check that the nodes in the produced nav-graph indeeds represent a clear/walkable
     * space in the NH world, and that neighbors indeed are neighboring tiles.
     * 
     * The check of the first part is visuallty :D (you need to visually compare two ascii maps).
     */
    @Test
    public void check_nodes_and_neighbors() throws InterruptedException {
        SimpleNavGraph nav = driver.getNavigationGraph() ;
        System.out.println(">>> ") ;
        for(int y=0; y<world.length; y++) {
            boolean[] row = world[y] ;
            for(int x=0; x<row.length; x++) {
                if(row[x]) {
                    System.out.print(" ") ;
                }
                else {
                    System.out.print("#") ;
                }                   
            }
            System.out.println("") ;
        }
        for(int nd=0; nd<nav.vertices.size(); nd++) {
            Pair<Integer,Integer> p1 = Utils.toTileCoordinate(nav.vertices.get(nd)) ;
            // check that p1 is marked as unoccupied:
            assertTrue(world[p1.snd][p1.fst]) ;
            for(int neighbor : nav.edges.neighbours(nd) ) {
                Pair<Integer,Integer> p2 = Utils.toTileCoordinate(nav.vertices.get(neighbor)) ;
                int dx = Math.abs(p1.fst - p2.fst) ;
                int dy = Math.abs(p1.snd - p2.snd) ;
                
                assertTrue(dx <= 1 && dy <= 1 && dx+dy>0) ;
            }
            
        }
    }
    
    /**
     * Chekc if the navgraph contains all monsters as obstacles (check this visually),
     * and that every monster stands on a walkable tile.
     */
    @Test
    public void check_monsters() {
        SimpleNavGraph nav = driver.getNavigationGraph() ;
        System.out.println(">>> test 2") ;
        for (Obstacle<LineIntersectable> ob : nav.obstacles) {
            assertTrue(ob.isBlocking) ;
            Monster m = ((MonsterWrapper) ob.obstacle).monster ;
            System.out.println("Monster " + m.ID + " @(" + m.x + "," + m.y + ")" ) ; 
            // check if m stands on a walkable tile:
            assertTrue(world[m.y][m.x]) ;
            
        }
        assertTrue(nav.obstacles.size() == 7) ;
    }
    
    
    

}

package org.projectxy.iv4xrLib;

import java.util.*;

import eu.iv4xr.framework.extensions.pathfinding.AStar;
import eu.iv4xr.framework.extensions.pathfinding.Pathfinder;
import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.agents.State;
import nl.uu.cs.aplib.mainConcepts.Environment;

public class MyAgentState extends State {

    public WorldModel previousWom = null;
    public WorldModel wom;

    /**
     * If not null, this contains a path that the agent intent to follow towards a
     * destination. The last element in the list is the destination. The first
     * element in the list is the next tile the agent should move to. After moving
     * to that tile, it is removed from the list. (so, this assumes that that first
     * element is indeed a neiboring tile of the tile where the agent currently is).
     * 
     * The tiles in the paths are represented by Vec3 locations.
     */
    public List<Vec3> currentPathToFollow;

    public SimpleNavGraph simpleWorldNavigation;
    Pathfinder pathfinder = new AStar() ;
 
    /**
     * Attaching MyEnv to this agent-state. Initializing the first World Model, and
     * initializing the navigation-graph. Assumption: the game is already launched.
     */
    @Override
    public MyAgentState setEnvironment(Environment env) {
        if (!(env instanceof MyEnv))
            throw new IllegalArgumentException();

        MyEnv env_ = (MyEnv) env;

        super.setEnvironment(env_);

        env_.startNewGame();

        this.wom = env_.nethackUnderTest.observe();
        this.simpleWorldNavigation = env_.nethackUnderTest.getNavigationGraph();
       
        return this;
    }

     
    //@Override
    public MyAgentState setUpNextLevel(Environment env) {
        
    	
    	currentPathToFollow = null;
    	previousWom = null;
    	wom = null; 
    	simpleWorldNavigation = null;
    	
    	
    	//MyAgentState.super.setEnvironment(MyAgentState.this.env());
    	
    	MyEnv env_ = (MyEnv) env;
        //super.setEnvironment(env);

        //env_.startNewGame();

        env_.navgraph = env_.nethackUnderTest.getNavigationGraph();
        
        this.simpleWorldNavigation = env_.navgraph;
        this.wom = env_.nethackUnderTest.observe();
    	
//    	MyAgentState.this.wom = 
 
        
        return this;

        
    }
    
    /**
     * TODO: you need to do something when we transition to the next level. The nav-graph must
     * be loaded anew, for example.
     */
    
    
    
    
    @Override
    public void updateState() {
        super.updateState();
        previousWom = wom;
        MyEnv env_ = (MyEnv) this.env();
        this.wom = env_.nethackUnderTest.observe();
        // update the monsters in the nav-graph obstacles; we remove killed monsters:
        simpleWorldNavigation.obstacles
           .removeIf((Obstacle<LineIntersectable> o) -> { 
               if (o.obstacle == null || !(o.obstacle instanceof MonsterWrapper)) return false ;
               MonsterWrapper mw = (MonsterWrapper) o.obstacle ;
               String mw_id = mw.monster.ID ;
               
               if (! wom.elements.keySet().contains(mw_id)) {
                   // if the monster-id is no longer in the world .. it is dead. Remove it from the nav-graph
                   return true ;
               }
               return false ; 
        }) ;
    };
    
    
    public void setAPathToFollow(List<Vec3> path ) {
        currentPathToFollow = path ;
    }
    
    /**
     * Check the navigation graph if there exists a path from the given source-tile
     * location to the destination-tile. If there is a path, one will be returned, and
     * else null.
     * 
     * Tiles are represented by Vec3 structures.
     */
    public List<Vec3> getPath(Vec3 source, Vec3 destination) {
        int src  = Utils.vec3ToNavgraphIndex(source, simpleWorldNavigation) ;
        int dest = Utils.vec3ToNavgraphIndex(destination, simpleWorldNavigation) ;
        // find a path to the destination. If a path is found, the destination will 
        // be the last in the path. 
        // If no path can be found, then it is null.
        List<Integer> path = pathfinder.findPath(simpleWorldNavigation, src, dest) ;
        if (path == null) return null ;
        // if a path is found, the first element is actually the same as the source. We
        // will drop it:
        path.remove(0) ;
        List<Vec3> path_ = new LinkedList<>() ;
        for(Integer nd : path) {
            path_.add(simpleWorldNavigation.vertices.get(nd)) ;
        }
        Utils.debugPrintPath(wom.position,path_) ;
        return path_ ;
    }
    
    public boolean isAlive() {
        WorldEntity playerStatus = wom.getElement(wom.agentId) ;
        return playerStatus.getBooleanProperty("isAlive") ;        
    }

}

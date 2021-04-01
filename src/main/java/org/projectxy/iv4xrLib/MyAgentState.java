package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.W3DAgentState;
import eu.iv4xr.framework.mainConcepts.W3DEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nl.uu.cs.aplib.agents.State;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

public class MyAgentState extends State {
    
    public WorldModel previousWom = null ;
    public WorldModel wom;
    
    
    public SimpleNavGraph simpleWorldNavigation ;
    
    /**
     * Attaching MyEnv to this agent-state. Initializing the first World Model, and
     * initializing the navigation-graph. Assumption: the game is already launched.
     */
    @Override
    public MyAgentState setEnvironment(Environment env) {
        if (!(env instanceof MyEnv)) throw new IllegalArgumentException() ;
        
        MyEnv env_ = (MyEnv) env ;
        
        super.setEnvironment(env_) ;
        
        env_.startNewGame();
        
        this.wom = env_.nethackUnderTest.observe() ;
        this.simpleWorldNavigation = env_.nethackUnderTest.getNavigationGraph() ;
        
        return this ;
    }
    
    @Override
    public void updateState() {
        super.updateState();
        previousWom = wom ;
        MyEnv env_ = (MyEnv) this.env() ;
        this.wom = env_.nethackUnderTest.observe() ;
    };

}

package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.projectxy.iv4xrLib.NethackWrapper.Movement;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic;

public class Example_using_pathfinding {
    
    @Test
    public void test1() throws InterruptedException {
        // launch the game:
        NethackWrapper driver = new NethackWrapper() ;
        driver.launchNethack(new NethackConfiguration()) ;
        
        // Create an agent, and attaching to it a clean state and environment:
        TestAgent agent = new TestAgent() ;
        MyAgentState state = new MyAgentState() ;
        agent.attachState(state) ;
        MyEnv env = new MyEnv(driver) ;
        agent.attachEnvironment(env) ;
        
        // give a goal-structure to the agent:
        GoalStructure g = Utils.entityVisited("149") ;
        agent.setGoal(g) ;
        
        // run the agent to control the game:
 
        int turn = 0 ;
        while(!g.getStatus().success()) {
            agent.update();
            turn++ ;
            System.out.println("[" + turn + "] agent@" + state.wom.position ) ;
            Thread.sleep(500);
            if(turn > 100) {
                // forcing break the agent seems to take forever...
                break ;
            }
        }
      
        for(WorldEntity e : state.wom.elements.values()) {
            System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position) ;
        }
    }

}

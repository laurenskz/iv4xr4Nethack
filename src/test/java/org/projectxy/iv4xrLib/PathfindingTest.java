package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;

import A.B.NethackConfiguration;
import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

public class PathfindingTest {
    
    @Test
    public void test1() throws InterruptedException {
        NethackWrapper driver = new NethackWrapper() ;
        driver.launchNethack(new NethackConfiguration()) ;
        
        TestAgent agent = new TestAgent() ;
        MyAgentState state = new MyAgentState() ;
        agent.attachState(state) ;
        MyNethackEnv env = new MyNethackEnv(driver) ;
        agent.attachEnvironment(env) ;
        
        // give a goal-structure to the agent:
        //GoalStructure g = SEQ(GoalLib.equipBow(),Utils.entityVisited(agent,"150",0)) ; 
        GoalStructure g = SEQ(
                GoalLib.entityVisited(agent,"47",0),
                GoalLib.pickUpItem(),
                GoalLib.entityVisited(agent,"149",0)
                ) ;  // bow 
        agent.setGoal(g) ;
        
        
        // run the agent to control the game:
        for(WorldEntity e : state.wom.elements.values()) {
            System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position) ;
        }
        int turn = 0 ;
        while(g.getStatus().inProgress()) {
            agent.update();
            turn++ ;
            System.out.println("[" + turn + "] agent@" + state.wom.position ) ;
            Thread.sleep(100);
            if(turn > 800) { // forcing break the agent seems to take forever...
                break ;
            }
        }
        
        
        for(WorldEntity e : state.wom.elements.values()) {
            System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position) ;
        }
        System.out.println("Goal status: " + g.getStatus()) ;
    }

}

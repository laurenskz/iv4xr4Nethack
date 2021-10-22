package org.projectxy.iv4xrLib; 

import A.B.NethackConfiguration;
import nl.uu.cs.aplib.mainConcepts.*;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import org.junit.jupiter.api.Test;
import org.projectxy.iv4xrLib.NethackWrapper.Movement;

import eu.iv4xr.framework.mainConcepts.TestAgent;

public class simple_example_controlling_NH_with_agent { 
    
    @Test
    public void test1() throws InterruptedException {

        // let's create a simple goal that requires the character to move
        Goal g = goal("player has moved") ;
        g.toSolve((MyAgentState S) -> 
             S.previousWom != null && S.wom != null && 
            !  S.previousWom.position.equals(S.wom.position)) ;
        
        // let's define a solver for this goal:
        Action doNothing = action("do nothing").do1((MyAgentState S) -> S) ;
        Action moveEast  = action("moveEast").do1((MyAgentState S) -> {
            //Vec3 currentPosition = S.wom.position ;
            //Vec3 east = Vec3.add(currentPosition, new Vec3(1,0,0)) ;
            MyNethackEnv env = (MyNethackEnv) S.env() ;
            env.move(Movement.RIGHT) ; 
            return S ;
        }) ;
        Tactic solver = SEQ(moveEast.lift(), doNothing.lift()) ;
        
        // connect this solver to the goal:
        g.withTactic(solver) ;

        // launch the game:
        NethackWrapper driver = new NethackWrapper() ;
        driver.launchNethack(new NethackConfiguration()) ;
        
        // Create an agent, and attaching to it a clean state and environment:
        TestAgent agent = new TestAgent() ;
        MyAgentState state = new MyAgentState() ;
        agent.attachState(state) ;
        MyNethackEnv env = new MyNethackEnv(driver) ;
        agent.attachEnvironment(env) ;
        
        // give the goal-structure to the agent:
        // we first lift g to a GoalStructure, because the agent wants a GoalStructure rather than a goal:
        //GoalStructure g_ = g.lift() ;
        
        
        GoalStructure g_ = GoalLib.equipBestAvailableWeapon();
        //GoalStructure g_ = GoalLib.restoreHealthFromInventory();
        //GoalStructure g_ = SEQ(GoalLib.equipBow(), GoalLib.aimWithBow());
        //GoalStructure g_ = GoalLib.equipBow();
        
        agent.setGoal(g_) ;
        
        // run the agent to control the game:
        int turn = 0 ;
        while(!g_.getStatus().success()) {
            agent.update();
            turn++ ;
            System.out.println("[" + turn + "] agent@" + state.wom.position ) ;
            Thread.sleep(50);
            if(turn > 20) {
                // forcing break the agent seems to take forever...
                break ;
            }
        }
        
    }
    

}

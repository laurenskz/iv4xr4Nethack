package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.ANYof;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;

import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;

import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
	public void test_navigate_to_a_location() throws InterruptedException {
		// launch the game:
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration());

		// Create an agent, and attaching to it a clean state and environment:
		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);

		// give a goal-structure to the agent:
		Vec3 destination = new Vec3(40, 6, 0);

		// a goal to guide agent to the given location; with monster-avoindance distance
		// set to 3:
		GoalStructure g = Utils.locationVisited(null, destination, 3);
		agent.setGoal(g);

		// run the agent to control the game:
		// System.out.println("type anything... ") ;
		// new Scanner(System.in) . nextLine() ;

		int turn = 0;
		while (!g.getStatus().success()) {
			agent.update();
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(100);
			if (turn > 100) {
				// forcing break the agent seems to take forever...
				break;
			}
		}
		assertTrue(Utils.sameTile(state.wom.position, destination));
	}
 
	// this test fails because a monster moves to block a tile along the path;
	// this needs to be handled. todo.
	@Test
	public void test_navigate_to_an_entity() throws InterruptedException {
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration());

		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);

		// give a goal-structure to the agent:
		GoalStructure g =  Utils.entityVisited("72");
		//GoalStructure g = SEQ(GoalLib.equipBow(), Utils.entityVisited("85"));
		agent.setGoal(g);

		// run the agent to control the game:
		// for(WorldEntity e : state.wom.elements.values()) {
		// System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position) ;
		// }
		int turn = 0;
		while (g.getStatus().inProgress()) {
			agent.update();
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(350);
			if (turn > 100) { // forcing break the agent seems to take forever...
				break;
			}
		}

		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}
		System.out.println("Goal status: " + g.getStatus());
	}

	@Test
	public void test_navigate_to_a_monster() throws InterruptedException { 
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration());

		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);

		// give a goal-structure to the agent:
		//GoalStructure g =  Utils.entityVisited("162");
		//GoalStructure g = SEQ(GoalLib.equipBestAvailableWeapon(), Utils.closeToAMonster("161", 3));
		 GoalStructure g = Utils.closeToAMonster("157",3) ;
		
		agent.setGoal(g);

		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}

		int turn = 0;
		while (g.getStatus().inProgress()) {
			agent.update();
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(300);
			if (turn > 100) { // forcing break the agent seems to take forever...
				break;
			}
		}
		System.out.println("Goal status: " + g.getStatus());
		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}

	}

	@Test
	public void test_navigate_to_an_entity_and_pickitup() throws InterruptedException {
		// launch the game:
  
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration());

		// Create an agent, and attaching to it a clean state and environment:
		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);

		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}

		// give a goal-structure to the agent:
		//GoalStructure g = SEQ(Utils.entityVisited("78"), GoalLib.pickUpItem(), Utils.entityVisited("144"));
		GoalStructure g = SEQ(Utils.entityVisited("77"), GoalLib.pickUpItem(), Utils.closeToAMonster("160", 3),Utils.closeToAMonster("154", 3),Utils.closeToAMonster("159", 3));

		
		//GoalStructure g = SEQ( Utils.closeToAMonster("161", 3),Utils.entityVisited("78"));

		agent.setGoal(g);

		// run the agent to control the game:

		int turn = 0;
		while (g.getStatus().inProgress()) {
			agent.update();
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(350);
			if (turn > 500) {
				// forcing break the agent seems to take forever...
				break;
			}
		}

		//////
		/*
		 * GoalStructure g_ = GoalLib.pickUpItem(); agent.setGoal(g_) ;
		 * 
		 * int turn1 = 0 ; while(!g_.getStatus().success()) { agent.update(); turn1++ ;
		 * 
		 * Thread.sleep(350); if(turn > 100) {
		 * 
		 * break ; } }
		 */
		///////
		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}
		System.out.println(">>> Goal status:" + g.getStatus());
	}

}

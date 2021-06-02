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


import static nl.uu.cs.aplib.AplibEDSL.* ;


import A.B.Monster;
import A.B.HealthPotion;
import A.B.Food;
import A.B.Water;
import A.B.Gold;



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
		GoalStructure g = Utils.locationVisited(agent,null, destination, 3);
		agent.setGoal(SEQ(g)); // have to pack it inside a SEQ for dynamic goal to work

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
		GoalStructure g =  Utils.entityVisited(agent,"17",3);
		//GoalStructure g = SEQ(GoalLib.equipBow(), Utils.entityVisited("85"));
		agent.setGoal(SEQ(g));

		// run the agent to control the game:
		for(WorldEntity e : state.wom.elements.values()) {
		 System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position) ;
		 }
		int turn = 0;
		while (g.getStatus().inProgress()) {
			agent.update();
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(100);
			if (turn > 200) { // forcing break the agent seems to take forever...
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
		 GoalStructure g = Utils.closeToAMonster(agent,"157",3) ;
		
		agent.setGoal(SEQ(g)); // have to pack it inside a SEQ for dynamic goal to work...

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
		GoalStructure g = SEQ(Utils.entityVisited(agent,"77",3), 
		                      GoalLib.pickUpItem(), 
		                      Utils.closeToAMonster(agent, "160", 3),
		                      Utils.closeToAMonster(agent, "154", 3),
		                      Utils.closeToAMonster(agent, "159", 3));

		
		//GoalStructure g = SEQ( Utils.closeToAMonster("161", 3),Utils.entityVisited("78"));

		agent.setGoal(g);

		// run the agent to control the game:

		int turn = 0;
		while (g.getStatus().inProgress()) {
		    agent.update() ;
			//try {agent.update();} catch (Exception e) {
			//	for (WorldEntity we : state.wom.elements.values()) {
			//		System.out.println(">>> " + we.type + ", id=" + we.id + ", @" + we.position);
			//	}
			//	g.printGoalStructureStatus();
			//	throw e;
			//} 
			turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position + ", Alive:" + state.isAlive());
			Thread.sleep(250);
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
		g.printGoalStructureStatus();
		System.out.println(">>> Goal status:" + g.getStatus());
		
	}

	
	@Test
	public void reach_the_stairs_until_fifth_level() throws InterruptedException {
		// This goal lets the agent going through levels by reaching the stairs, until it reaches the 5th level (first Boss)
		
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
		GoalStructure g = SEQ(//Utils.entityVisited(agent,"77",3), 
		                      //GoalLib.pickUpItem(), 
		                      Utils.entityVisited_5_level(agent,"Stairs",3)
		                      
		                      
		                      );

		

		agent.setGoal(g);

 
		int turn = 0;
		while (g.getStatus().inProgress()) {
		    agent.update();
		    turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(250);
			if (turn > 500) {
				// forcing break the agent seems to take forever...
				break;
			}
		}

		
		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}
		System.out.println(">>> Goal status:" + g.getStatus());
	}
	
	
//	@Test
//	public void interact_with_everything_and_reach_the_stairs() throws InterruptedException {
//		// This goal lets the agent going through levels by reaching the stairs, until it reaches the 5th level (first Boss)
//		
//		// launch the game:
//		NethackWrapper driver = new NethackWrapper();
//		driver.launchNethack(new NethackConfiguration());
//
//		// Create an agent, and attaching to it a clean state and environment:
//		TestAgent agent = new TestAgent();
//		MyAgentState state = new MyAgentState();
//		agent.attachState(state);
//		MyEnv env = new MyEnv(driver);
//		agent.attachEnvironment(env);
//
//		for (WorldEntity e : state.wom.elements.values()) {
//			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
//		}
//
//		// give a goal-structure to the agent:
//		//GoalStructure g = SEQ(Utils.entityVisited("78"), GoalLib.pickUpItem(), Utils.entityVisited("144"));
//		GoalStructure g = SEQ(//Utils.entityVisited(agent,"77",3), 
//		                      //GoalLib.pickUpItem(), 
//		                      Utils.entityVisited_all(agent,"Stairs",3)
//		                      
//		                      
//		                      );
//
//		
//
//		agent.setGoal(g);
//
// 
//		int turn = 0;
//		while (g.getStatus().inProgress()) {
//		    agent.update();
//		    turn++;
//			System.out.println("[" + turn + "] agent@" + state.wom.position);
//			Thread.sleep(250);
//			if (turn > 500) {
//				// forcing break the agent seems to take forever...
//				break;
//			}
//		}
//
//		
//		for (WorldEntity e : state.wom.elements.values()) {
//			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
//		}
//		System.out.println(">>> Goal status:" + g.getStatus());
//	
//		
//		//////
//		/*
//		 * GoalStructure g_ = GoalLib.pickUpItem(); agent.setGoal(g_) ;
//		 * 
//		 * int turn1 = 0 ; while(!g_.getStatus().success()) { agent.update(); turn1++ ;
//		 * 
//		 * Thread.sleep(350); if(turn > 100) {
//		 * 
//		 * break ; } }
//		 */
//		///////
//		for (WorldEntity e : state.wom.elements.values()) {
//			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
//		}
//		g.printGoalStructureStatus();
//		System.out.println(">>> Goal status:" + g.getStatus());
//		
//	}
	
	@Test
	public void interact_with_everything_and_reach_the_stairs() throws InterruptedException {
		// This goal lets the agent going through levels by reaching the stairs, until it reaches the 5th level (first Boss)
		
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
		
		while (true) {
			
			//state.updateState();
			
			WorldEntity targetEntity = null;
			
			for (WorldEntity e: state.wom.elements.values()) {
				
				if(	(e.type.equals(HealthPotion.class.getSimpleName()) ) ||
	            		 (e.type.equals(Water.class.getSimpleName()) ) ||
	            		 (e.type.equals(Gold.class.getSimpleName()) ) ||
	            		 (e.type.equals(Food.class.getSimpleName()) ) ||
	            		 (e.type.equals(Monster.class.getSimpleName() ) )
	            		 )
	            {
					
					targetEntity = e ;
					break ;
	            }	
				
			}
			
			if (targetEntity == null) break ;
			
			GoalStructure g1;
			
			if(targetEntity.type == "Monster") {
				
				g1 = FIRSTof(
						Utils.closeToAMonster(agent, targetEntity.id, 0),
						SUCCESS() );
				
			}
			else {
				
				g1 = FIRSTof(
						SEQ( Utils.entityVisited(agent, targetEntity.id,3),
			                 GoalLib.pickUpItem() ),
						SUCCESS() );
				
			}
			
			agent.setGoal(g1);
			
			int turn = 0;
			while (g1.getStatus().inProgress()) {
			    agent.update();
			    turn++;
				//System.out.println("[" + turn + "] agent@" + state.wom.position);
				Thread.sleep(250);
				if (turn > 500) {
					// forcing break the agent seems to take forever...
					break;
				}
			}
			
			
		}
		

		GoalStructure g2 = Utils.entityVisited_all(agent,"Stairs",3);

		agent.setGoal(g2);

 
		int turn = 0;
		while (g2.getStatus().inProgress()) {
		    agent.update();
		    turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(250);
			if (turn > 500) {
				// forcing break the agent seems to take forever...
				break;
			}
		}

		
//		for (WorldEntity e : state.wom.elements.values()) {
//			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
//		}
		System.out.println(">>> Goal status:" + g2.getStatus());
	
		g2.printGoalStructureStatus();
		
	}
	
	
	
}
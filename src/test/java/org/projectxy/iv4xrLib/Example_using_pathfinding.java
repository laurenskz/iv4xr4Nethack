package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.*;


import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;

import nl.uu.cs.aplib.mainConcepts.GoalStructure;

import A.B.Monster;
import A.B.HealthPotion;
import A.B.Food;
import A.B.Water;
import A.B.Gold;
import A.B.Sword;
import A.B.Bow;


@Ignore
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
        GoalStructure g = GoalLib.locationVisited(agent, null, destination, 3);
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
        GoalStructure g = GoalLib.entityVisited(agent, "17", 3);
        //GoalStructure g = SEQ(GoalLib.equipBow(), Utils.entityVisited("85"));
        agent.setGoal(SEQ(g));

        // run the agent to control the game:
        for (WorldEntity e : state.wom.elements.values()) {
            System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
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
        GoalStructure g = GoalLib.closeToAMonster(agent, "157", 3);

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
        GoalStructure g = SEQ(GoalLib.entityVisited(agent, "77", 3),
                GoalLib.pickUpItem(),
                GoalLib.closeToAMonster(agent, "160", 3),
                GoalLib.closeToAMonster(agent, "154", 3),
                GoalLib.closeToAMonster(agent, "159", 3));


        //GoalStructure g = SEQ( Utils.closeToAMonster("161", 3),Utils.entityVisited("78"));

        agent.setGoal(g);

        // run the agent to control the game:

        int turn = 0;
        while (g.getStatus().inProgress()) {
            agent.update();
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
        GoalStructure g = SEQ(

                // get a bow first
                SEQ(GoalLib.entityVisited(agent, "47", 1),
                        GoalLib.pickUpItem()),

                GoalLib.entityVisited_5_level(agent, "Stairs", 3)

        );

        agent.setGoal(g);

        int turn = 0;
        while (g.getStatus().inProgress()) {

            agent.update();
            turn++;
            System.out.println("[" + turn + "] agent@" + state.wom.position);


            System.out.println("----------------------------PLAYER'S ATTACK DAMAGE------------------------------------");

            for (WorldEntity e : state.wom.elements.values()) {

                if (e.type.equals(Monster.class.getSimpleName()) && state.previousWom != null) {

                    String monsterId = e.id;

                    WorldModel current = state.wom;
                    WorldModel previous = state.previousWom;

                    WorldEntity monsterCurrentState = current.elements.get(monsterId);
                    WorldEntity monsterPreviousState = previous.elements.get(monsterId);


                    int currentMonsterLife = monsterCurrentState.getIntProperty("health");
                    int previousMonsterLife = monsterPreviousState.getIntProperty("health");


                    // the player's equipped weapon
                    String agentID = state.wom.agentId;

                    WorldEntity agentCurrentState = current.elements.get(agentID);
                    String equippedWeapon = agentCurrentState.getStringProperty("equippedWeaponName");
                    int equippedWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");


                    int lifeDif = previousMonsterLife - currentMonsterLife;
                    System.out.println("Agent at position " + state.wom.position + " attacks with " + equippedWeapon + " for " + equippedWeaponDmg + " damage");
                    System.out.println();

                    System.out.println("Attack on: monster with id " + e.id + ", at position " + e.position);
                    System.out.println("Monster's current life: " + currentMonsterLife);
                    System.out.println("Monster's previous life: " + previousMonsterLife);
                    System.out.println("Player's attack damage: " + lifeDif);


                    if (lifeDif != 0) {

                        if (lifeDif == equippedWeaponDmg || currentMonsterLife == 0) {

                            System.out.println("The attack damage is equal to the equipped weapon damage, or the monster was killed on this attack");
                            System.out.println();

                        } else {

                            System.out.println("There was damage on the monster, but not the correct amount of it");
                            System.out.println();
                        }

                    } else {

                        System.out.println("There was no damage at all on this turn");
                        System.out.println();
                    }


                }


            }

            System.out.println("-------------------------------------------------------------------------------------------");
            System.out.println("-------------------------------------------------------------------------------------------");


            Thread.sleep(50);
            if (!state.isAlive() || turn > 1000) {

                // forcing break the agent seems to take forever...
                break;
            }
        }

//		for (WorldEntity e : state.wom.elements.values()) {
//			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
//		}
        System.out.println(">>> Agent alive:" + state.isAlive());
        System.out.println(">>> Goal status:" + g.getStatus());
    }


    @Test
    public void interact_with_everything_and_reach_the_stairs() throws InterruptedException {

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

            int minDist = 140; //90+50 max distance

            WorldEntity targetEntity = null;
            WorldEntity stairs = state.wom.getElement("Stairs");
            for (WorldEntity e : state.wom.elements.values()) {
                //int minDist = 140;
                //System.out.println("closes item is in distance: " + minDist);

                if ((e.type.equals(HealthPotion.class.getSimpleName())) ||
                        (e.type.equals(Water.class.getSimpleName())) ||
                        (e.type.equals(Gold.class.getSimpleName())) ||
                        (e.type.equals(Food.class.getSimpleName())) ||
                        (e.type.equals(Sword.class.getSimpleName())) ||
                        (e.type.equals(Bow.class.getSimpleName())) ||
                        (e.type.equals(Monster.class.getSimpleName()))
                ) {


                    if (stairs != null && Utils.sameTile(stairs.position, e.position)) {
                        // not going to test an entity that is ON stairs
                        continue;
                    }


//					targetEntity = e ;
//					break ;
                    int ix = (int) e.position.x;                    // item's x coordinate
                    int iy = (int) e.position.y;                    // item's y coordinate
                    int ax = (int) state.wom.position.x;                // agent's x coordinate
                    int ay = (int) state.wom.position.y;                // agent's y coordinate

                    int dx = (int) Math.abs(ax - ix); // agent-item distance in x axis
                    int dy = (int) Math.abs(ay - iy); // agent-item distance in y axis


                    if (dx + dy < minDist) {

                        minDist = dx + dy;

                        targetEntity = e;
                    }
                }
                //minDist=140;

            }

            if (targetEntity == null) break;

            GoalStructure g1;
            final String targetId = targetEntity.id;

            if (targetEntity.type.equals(Monster.class.getSimpleName())) {
                System.out.println("######### Testing monster " + targetId);
                g1 = GoalLib.closeToAMonster(agent, targetId, 0);

            } else {
                System.out.println("######### Testing entity " + targetEntity.type + " " + targetId);
                g1 = FIRSTof(
                        SEQ(GoalLib.entityVisited(agent, targetEntity.id, 3),
                                IFELSE((MyAgentState S) -> S.wom.elements.get(targetId) != null,
                                        GoalLib.pickUpItem(),
                                        SUCCESS())
                        ));
            }

            agent.setGoal(g1);

            int turn = 0;
            while (g1.getStatus().inProgress()) {
                System.out.println(">>> Agent @" + state.wom.position + ", alive:" + state.isAlive());


                System.out.println("----------------------------MONSTER'S ATTACK DAMAGE------------------------------------");

                //if (state.previousWom != null) {

                for (WorldEntity e : state.wom.elements.values()) {

                    if (e.type.equals(Monster.class.getSimpleName()) && state.previousWom != null) {

                        int monsterXPos = (int) e.position.x;
                        int monsterYPos = (int) e.position.y;

                        int agentXPos = (int) state.wom.position.x;
                        int agentYPos = (int) state.wom.position.y;


                        int dx = (int) Math.abs(agentXPos - monsterXPos); // agent-monster distance in x axis
                        int dy = (int) Math.abs(agentYPos - monsterYPos); // agent-monster distance in y axis

                        if (dx + dy <= 1) {


                            String agentId = state.wom.agentId;
                            String monsterId = e.id;

                            WorldModel current = state.wom;
                            WorldModel previous = state.previousWom;

                            WorldEntity agentCurrentState = current.elements.get(agentId);
                            WorldEntity agentPreviousState = previous.elements.get(agentId);


                            //get the monster's attack damage
                            WorldEntity monsterCurrentState = current.elements.get(monsterId);
                            //WorldEntity monsterPreviousState = previous.elements.get(monsterId) ;

                            int monsterDmg = monsterCurrentState.getIntProperty("attackDmg");


                            int currentAgentLife = agentCurrentState.getIntProperty("health");
                            int previousAgentLife = agentPreviousState.getIntProperty("health");


                            // every 8 moves, player loses 1 life point
                            int movePoints = 0;
                            int moves = (int) state.wom.timestamp;
                            if (moves % 8 == 0) {
                                System.out.println("1 life point was lost due to 8 moves player made");
                                System.out.println(">>>>>>>	Moves: " + moves);
                                System.out.println(">>>>>>>	Turn: " + turn);
                                System.out.println();
                                movePoints++;
                            }


                            int lifeDif = previousAgentLife - currentAgentLife + movePoints;

                            System.out.println("Agent current life: " + currentAgentLife);
                            System.out.println("Agent previous life: " + previousAgentLife);
                            System.out.println("Monster's attack damage: " + lifeDif);
                            System.out.println("Actual Monster's attack damage: " + monsterDmg);
                            System.out.println();


                            //lifeDif = 0;


                        }


                    }
                }


//		    		 String agentID = state.wom.agentId ;
//		    		 
//		    		 WorldModel current = state.wom ;
//		    		 WorldModel previous = state.previousWom ;
//		    		 
//		    		 WorldEntity agentCurrentState = current.elements.get(agentID) ;
//		 	         WorldEntity agentPreviousState = previous.elements.get(agentID) ;
//		 	         
//		 	         
//		 	         int currentAgentLife = agentCurrentState.getIntProperty("health");
//		    		 int previousAgentLife = agentPreviousState.getIntProperty("health");
//		    		 
//		    		 // monster's attack damage
//		    		 int monsterAttackDmg = 
//		    		 
//		    		 
//		    		
//		    		 
//		    		 int lifeDif = previousAgentLife - currentAgentLife;
//		    		 
//		    		 
//		    		 // every 8 moves, player loses 1 life point
//		    		 long moves = state.wom.timestamp;
//		    		 if (moves % 8 == 0) {
//		    			 System.out.println("1 life point was lost due to 8 moves player made");
//		    			 System.out.println(">>>>>>>	Moves: "+ moves);
//		    			 System.out.println(">>>>>>>	Turn: "+ turn);
//		    			 lifeDif++;
//		    		 }
//		    		 
//		    		 
//		    		 System.out.println("Agent current life: "+ currentAgentLife);
//		    		 System.out.println("Agent previous life: "+ previousAgentLife);
//		    		 System.out.println("Monster's attack damage: "+ lifeDif);
//		    		 
//		    		 
//		    		 
//		    		 lifeDif = 0;
                //}


                System.out.println("-------------------------------------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------------------------------------");


                agent.update();
                turn++;

                Thread.sleep(50);
                if (!state.isAlive() || turn > 500) {
                    // forcing break the agent seems to take forever...
                    break;
                }
            }
            if (!state.isAlive()) {
                // well... the agent is dead...
                return;
            }
        }


        System.out.println("######### Going to next the stairs");

        GoalStructure g2 = GoalLib.entityVisited_all(agent, "Stairs", 3);

        agent.setGoal(g2);

        int turn = 0;
        while (g2.getStatus().inProgress()) {
            agent.update();
            turn++;
            System.out.println("[" + turn + "] agent@" + state.wom.position);
            Thread.sleep(50);
            if (turn > 500) {
                // forcing break the agent seems to take forever...
                break;
            }
        }


        System.out.println(">>> Goal status:" + g2.getStatus());

        g2.printGoalStructureStatus();

    }


}
package org.projectxy.iv4xrLib;


import static nl.uu.cs.aplib.AplibEDSL.*;
import org.projectxy.iv4xrLib.NethackWrapper.Interact;

import A.B.Screen;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic;



/**
 * A set of three typical goals for testing an SUT using an iv4XR agent. First let's go 
 * through some terminologies.
 * 
 *   (1) a goal represents some SUT state that the agent wants. To be in that state, it needs
 *   to drive the SUT. To do this, a "solver" is needed. This is called "tactic". Every goal
 *   will thus need a tactic. When we define a goal, we should therefore also specify what
 *   the tactic we want to use for solving it.
 *   
 *   (2) We can structure multiple goals structurally, e.g. if we want to achieve all of them,
 *   but in a certain order. A hierarchically structured goals is called a "goal structure".
 *   
 *   (3) So below we actually provide a set of typical "goal structures". Again, for each goal
 *   in the structure, we will have to specify the corresponding tactic.
 *   
 *   (4) The provided goal-structures are also "parameterized"! So, although below we have to
 *   invest effort to define the goal-structures, after that we can use them endlessly to
 *   solve various instances.
 *   
 * We give examples of three typical goal-structure:
 * 
 *   (1) entityInCloseRange(e,epsilon) : to steer the test-agent towards the in-SUT entity e,
 *                                       to within the distance epsilon.
 *                                       
 *   (2) entityInteracted(e) : to steer the test-agent to get close enough to the entity e, and
 *                             then to interact with it.
 *                             
 *   (3) entityInvariantChecked(a,e,p) : to steer the test-agent a to get close enough to the entity e
 *                             and then to check whether the predicate p(e) holds. This counts as
 *                             "checking". If the predicate holds, a "pass" verdict will be logged,
 *                             and else a "fail" verdict will be logged.                       
 *                             
 *  A template implementation is provided, which assumes the existence of some tactics. The
 *  tactics can be expected to be rather SUT dependent, so we do not provide much sample code
 *  for them. They can be found in the class TacticLib.                                                             
 */

public class GoalLib {
	Screen nethack; 

	// the first food that you can find in the inv
	public static GoalStructure restoreHealthFromInventory() {
  		//System.out.println("AND HERE!");

	    
	    Goal g1 = goal("restore health") ;
	    
	    g1.toSolve((MyAgentState S) -> {
	        WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        String agentId = S.wom.agentId ;
	        WorldEntity agentOldState = old.elements.get(agentId) ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        int oldHealth = agentOldState.getIntProperty("health") ;
	        int currentHealth = agentCurrentState.getIntProperty("health") ;
	        
	        System.out.println("oldHealth:" + oldHealth);
	        System.out.println("currentHealth:" + currentHealth);
	       
	        
	        return (	(currentHealth > oldHealth) || ( (oldHealth == 10) && (currentHealth == 10) )	) ;
	    }) ;
	    
	    Action restoreHealth = action("restore health") ;
	    
	    // figure out how to use food ... preferably through env_, and use it
	    
  		System.out.println("1. HERE in restoreHealthFromInventory!");
  		
  		///////////////////
  		
  		
  		//////////////////
  		

	    restoreHealth.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        
	        
      		System.out.println("2. AND HERE!");
	        
	        WorldEntity inv = current.getElement("Inventory");
	        
			boolean healthItemFoundAndUsed = false;

            System.out.println("3. Iterating the inventory elements and looking for the needed item! #Inv:" + inv.elements.size());	        
	        
            for(WorldEntity item_ : inv.elements.values()) {

	          	if( (item_.type.equals("Food") || item_.type.equals("Water") || item_.type.equals("HealthPotion")) ) {
	          		//System.out.println("AND HERE!");
	          		String itemId = item_.id;
	          	          		
	          		System.out.println("Item ID: " + itemId );
	          		System.out.println("Item Name: " + item_.type );
	          		
	          		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
	       
	          		
	          		healthItemFoundAndUsed = true;
	          		
	          		
	          	
	          		// Freeze the Nethack window until Enter key is pressed. 
	          		// So we can see the progress of the goals in the actual game.
	          		//System.out.println("Hit RETURN to continue.") ;
	                //new Scanner(System.in) . nextLine() ;

	          		break;
	          	}
	         }
	        
	       
	        if(healthItemFoundAndUsed) {
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    Tactic restoreHealthTactic = restoreHealth.lift() ;
	    
	    g1.withTactic(FIRSTof(restoreHealthTactic, ABORT())) ;
	    
	    GoalStructure g1_ = g1.lift() ;
	    
	    return g1_ ;
	    
	}
	
	 
	public static GoalStructure equipSword() {
  		//System.out.println("AND HERE!");

	    Goal g2 = goal("equip sword") ;
	    
	    g2.toSolve((MyAgentState S) -> {
	        WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        String agentId = S.wom.agentId ;
	        WorldEntity agentOldState = old.elements.get(agentId) ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        String oldWeapon = agentOldState.getStringProperty("equippedWeaponName");
	        String currentWeapon = agentCurrentState.getStringProperty("equippedWeaponName") ;
	        
	        
	        //System.out.println("curr weap:" + currentWeapon);
	        //System.out.println("prev weap:" + oldWeapon);
	        //System.out.println("weap DMG: " + dmg);

	        
	        String weaponNeeded = "Sword";
	        return (	(oldWeapon != currentWeapon) && (currentWeapon.toLowerCase().contains(weaponNeeded.toLowerCase() ) )		);
	    }) ;
	    
	    
	    
	    Action equipSword = action("equip sword") ;
	    
	    
  		System.out.println("1. HERE in equipSword()!");

  		equipSword.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        String weaponNeeded = "Sword";
	        
      		System.out.println("2. AND HERE in equipSword() AGAIN!");
	        
	        WorldEntity inv = current.getElement("Inventory");
	        
			boolean SwordFoundAndUsed = false;

            System.out.println("3. Iterating the inventory and looking for the needed SWORD WEAPON! #Inv:" + inv.elements.size());
	        
	        for(WorldEntity item_ : inv.elements.values()) {

	          	if( item_.type.toLowerCase().contains(weaponNeeded.toLowerCase()) ) {
	          		//System.out.println("AND HERE!");
	          		String itemId = item_.id;
	          	          		
	          		System.out.println("Item ID: " + itemId );
	          		System.out.println("Item Name: " + item_.type );
	          		
	          		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
	       
	          		SwordFoundAndUsed = true;
	          		
	          		
	          		// Freeze the Nethack window until Enter key is pressed. 
	          		// So we can see the progress of the goals in the actual game.
	          		//System.out.println("Hit RETURN to continue.") ;
	                //new Scanner(System.in) . nextLine() ;

	          		break;
	          	}
	         }
	        
	       
	        if(SwordFoundAndUsed) {
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    Tactic equipSwordTactic = equipSword.lift() ;
	    
	    g2.withTactic(FIRSTof(equipSwordTactic, ABORT())) ;
	    
	    GoalStructure g2_ = g2.lift() ;
	    
	    return g2_ ;
	    
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static GoalStructure equipBow() {
  		//System.out.println("AND HERE!");

	    Goal g3 = goal("equip bow") ;
	    
	    g3.toSolve((MyAgentState S) -> {
	        WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        String agentId = S.wom.agentId ;
	        WorldEntity agentOldState = old.elements.get(agentId) ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        String oldWeapon = agentOldState.getStringProperty("equippedWeaponName");
	        String currentWeapon = agentCurrentState.getStringProperty("equippedWeaponName") ;
	        String weaponNeeded = "Bow";
	        
	        System.out.println("curr weap:" + currentWeapon);
	        System.out.println("prev weap:" + oldWeapon);
	        
	        return (	(oldWeapon != currentWeapon) && (currentWeapon.toLowerCase().contains(weaponNeeded.toLowerCase() ) )		);
	    }) ;
	    
	    
	    
	    Action equipBow = action("equip bow") ;
	    
	    
  		System.out.println("1. HERE in equipBow()!");

  		equipBow.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        String weaponNeeded = "Bow";
	        
      		System.out.println("2. AND HERE in equipBow() AGAIN!");
	        
	        WorldEntity inv = current.getElement("Inventory");
	        
			boolean BowFoundAndUsed = false;

            System.out.println("3. Iterating the inventory and looking for the needed BOW WEAPON! #Inv:" + inv.elements.size());	        
	        for(WorldEntity item_ : inv.elements.values()) {

	          	if( item_.type.toLowerCase().contains(weaponNeeded.toLowerCase()) ) {

	          		String itemId = item_.id;
	          	          		
	          		System.out.println("Item ID: " + itemId );
	          		System.out.println("Item Name: " + item_.type );
	          		
	          		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
	       
	          		// move the part of if(foodFoundAndEaten)... in this if statement, no boolean needed
	          		
	          		BowFoundAndUsed = true;
	          		

	          		// Freeze the Nethack window until Enter key is pressed. 
	          		// So we can see the progress of the goals in the actual game.
	          		//System.out.println("Hit RETURN to continue.") ;
	                //new Scanner(System.in) . nextLine() ;

	          		break;
	          	}
	         }
	        
	       
	        if(BowFoundAndUsed) {
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    Tactic equipBowTactic = equipBow.lift() ;
	    
	    g3.withTactic(FIRSTof(equipBowTactic, ABORT())) ;
	    
	    GoalStructure g3_ = g3.lift() ;
	    
	    return g3_ ;
	    
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	 
	public static GoalStructure pickUpItem() {
  		//System.out.println("AND HERE!");

	    
	    Goal g3 = goal("pick up item") ;
	    
	    g3.toSolve((MyAgentState S) -> {
	        WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        //String agentId = S.wom.agentId ;
	        
	        WorldEntity oldInv = old.getElement("Inventory");
	        WorldEntity currentInv = current.getElement("Inventory");
	        
	        int oldInvSize = oldInv.elements.size();
	        int currentInvSize = currentInv.elements.size();
	        
	        
	        //WorldEntity agentOldState = old.elements.get(agentId) ;
	        //WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        
	        //int oldHealth = agentOldState.getIntProperty("health") ;
	        //int currentHealth = agentCurrentState.getIntProperty("health") ;
	       
	        //System.out.println("old size: "+ oldInvSize);
	        //System.out.println("current size: "+ currentInvSize);
	        return (currentInvSize > oldInvSize)  ;
	    }) ;
	    
	    
	    Action pickUpItem = action("pick up item") ;
	    
  		//System.out.println("1. HERE in pickUpItem!");
  		


  		
  
  		pickUpItem.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        
	        
      		
	        
			boolean itemFoundAndPicked = false;
			
			
	        env_.interact(current.agentId, null, Interact.PickupItem);
			
	        itemFoundAndPicked = true;
			
	       
	        if(itemFoundAndPicked) {
	            S.updateState() ;

	            return S ;
	            
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    Tactic pickUpItemTactic = pickUpItem.lift() ;
	    
	    g3.withTactic(FIRSTof(pickUpItemTactic, ABORT())) ;
	    
	    GoalStructure g3_ = g3.lift() ;
	    
	    return g3_ ;
	    
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
 
	public static GoalStructure aimWithBow() {
  		//System.out.println("AND HERE!");

	    Goal g4 = goal("aim-with-bow") ;
	    
	    g4.toSolve((MyAgentState S) -> {
	        //WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        String agentId = S.wom.agentId ; 
	        //WorldEntity agentOldState = old.elements.get(agentId) ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        //String oldWeapon = agentOldState.getStringProperty("equippedWeapon");
	        String currentWeapon = agentCurrentState.getStringProperty("equippedWeaponName") ;
	        Boolean isAiming = agentCurrentState.getBooleanProperty("aimingBow");
	        //Boolean isSeen = agentCurrentState.getBooleanProperty("seenPlayer");
	        String weaponNeeded = "Bow";
	        
	        
	        System.out.println("curr weap: " + currentWeapon);
	        System.out.println("Is aiming?: " + isAiming);
	        //System.out.println("Is seen?: " + isSeen);

	        
	        return (	(currentWeapon.toLowerCase().contains(weaponNeeded.toLowerCase() ) )	&&	isAiming);
	    }) ;
	    
	    
	     
	    Action aimWithBow = action("aim-with-bow") ;
	    
	    
  		

  		aimWithBow.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        
      		
	        
	        
	        
			boolean aimedWithBow = false;

	        
	        
          	

	          	
      		env_.interact(current.agentId, null, Interact.AimWithBow);
	       
       		
	          		
      		aimedWithBow = true;
	          		

	          		
	        if(aimedWithBow) {
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    Tactic aimWithBowTactic = aimWithBow.lift() ;
	    
	    g4.withTactic(FIRSTof(aimWithBowTactic, ABORT())) ;
	    
	    GoalStructure g4_ = g4.lift() ;
	    
	    return g4_ ;
	    
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	// Equip the best available weapon from inventory (the one with the highest damage)
	public static GoalStructure equipBestAvailableWeapon() {
  		//System.out.println("AND HERE!");

	    
	    Goal g1 = goal("Equip Best Available Weapon") ;
	    
	    g1.toSolve((MyAgentState S) -> {
	        WorldModel old = S.previousWom ;
	        WorldModel current = S.wom ;
	        String agentId = S.wom.agentId ;
	        WorldEntity agentOldState = old.elements.get(agentId) ;
	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        String oldWeapon = agentOldState.getStringProperty("equippedWeaponName");
	        String currentWeapon = agentCurrentState.getStringProperty("equippedWeaponName") ;


	        int oldWeaponDamage = agentOldState.getIntProperty("equippedWeaponDmg");
	        int currentWeaponDamage = agentCurrentState.getIntProperty("equippedWeaponDmg");
	        
	        return (	(oldWeapon != currentWeapon) || (oldWeaponDamage <= currentWeaponDamage) 	) ;
	    }) ;
	    
	    Action equipBestAvailableWeapon = action("Equip Best Available Weapon") ;
	    
	    
  		

	    equipBestAvailableWeapon.do1((MyAgentState S) -> { 
	        MyEnv env_ = (MyEnv) S.env() ;
	        WorldModel current = S.wom ;
	        
	        String bowWeapon = "Bow";
	        String swordWeapon = "Sword";


	        WorldEntity inv = current.getElement("Inventory"); 
	        
			boolean bestWeaponEquipped = false;
			int bestWeaponDmg = 0;
	        
			System.out.println("Iterating the inventory elements and looking for the weapon with the higher damage.. #Inv:" +inv.elements.values().size());
            
	        for(WorldEntity item_ : inv.elements.values()) {
          		//System.out.println(item_);
          		if ((item_.type.toLowerCase().contains(bowWeapon.toLowerCase())) || (item_.type.toLowerCase().contains(swordWeapon.toLowerCase()))){
          			
     			
	          		int dmg = item_.getIntProperty("attackDmg");
	          		//String itemId = "";
	          		
	
	          		
	          		
	
		          	if( dmg > bestWeaponDmg   ) {
		          		//System.out.println("AND HERE!");
		          		
		          		bestWeaponDmg = dmg;
		          		String itemId = item_.id;
		          	          		
		          		//System.out.println("Item ID: " + itemId );
		          		//System.out.println("Item Name: " + item_.type );
		          		
		          		env_.interact(current.agentId, itemId, Interact.SelectItemFromInv);
		          		
		          		System.out.println("Equipped item type: " + item_.type);
		          		System.out.println("Equipped item damage: " + dmg);
		          		
		          		bestWeaponEquipped = true;
		       
		          		// move the part of if(foodFoundAndEaten)... in this if statement, no boolean needed
		          		
		          		
		          		
		          	
		          		// Freeze the Nethack window until Enter key is pressed. 
		          		// So we can see the progress of the goals in the actual game.
		          		//System.out.println("Hit RETURN to continue.") ;
		                //new Scanner(System.in) . nextLine() ;
	
		          		//break;
		          	}
          		}
	         }
	       
	        if(bestWeaponEquipped) {
	            S.updateState() ;
	            return S ;
	        }
	        else {
	            return null ;
	        }
	    }) ;
	    
	    
	    Tactic equipBestAvailableWeaponTactic = equipBestAvailableWeapon.lift() ;
	    
	    g1.withTactic(FIRSTof(equipBestAvailableWeaponTactic, ABORT())) ;
	    
	    GoalStructure g1_ = g1.lift() ;
	    
	    return g1_ ;
	    
	}
	
	  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  

	
	
	/**
	 * Construct a goal structure that will make an agent to move towards the given entity,
	 * until it is in the interaction-distance with the entity; and then interacts with it.
	 
	public static GoalStructure entityInteracted(String entityId) {

		float interactionDistance = 0.5f; // specify whatever the interaction distance is...

		// the goal for the interaction part:
		Goal interacton = 

				// create a goal, give it a name etc:

				goal(String.format("This entity is interacted: [%s]", entityId))

				// specify the predicate to solve. In this case we don't want to solve anything.
				// we will put the solving tactic instead.
				. toSolve((W3DAgentState belief) -> true) 

				// Specify the solving tactic: we interact, if interaction is some how not possible,
				// the goal as failed by invoking ABORT:

				. withTactic(
						FIRSTof( 
								TacticLib.interact(entityId),// interact with the entity
								ABORT()))   // abort if we can't interact
				;

		// the final goal structure is a composition of moving close to the entity, and then interacting with it:

		return SEQ(entityInCloseRange(entityId,interactionDistance), interacton.lift());
	}
	*/

	public static GoalStructure closeToAMonster(TestAgent agent, String monsterId, float monsterAvoidDistance) {
	
	    Goal g = goal("Close to monster " + monsterId)
	            .toSolve((MyAgentState S) -> { 
	            	
	            	
	                WorldEntity m = S.wom.getElement(monsterId) ;
	                
	                if (m!=null) {
	                    
	                    int dx = (int) Math.abs(m.position.x - S.wom.position.x) ;
	                    int dy = (int) Math.abs(m.position.y - S.wom.position.y) ;
	                    boolean monsterIsAlive = m.getBooleanProperty("alive");
	                    
	                    
	                    return  (dx + dy == 1) || (!monsterIsAlive) ;
	                }
	                else return true;
	             })
	            .withTactic(FIRSTof(
	            		TacticLib.abortIfDead(),
	            		TacticLib.checkIfEntityNoLongerExists(monsterId),
	            		//collectHealthItemsIfNeeded(agent,monsterAvoidDistance),
	            		TacticLib.useHealthToSurvive().lift(),
	            		TacticLib.equipBestAvailableWeapon().lift(),
	            		TacticLib.bowAttack().lift(),
	            		TacticLib.meleeAttack().lift(),
	            		//interactWithEverything(agent, monsterAvoidDistance),
	                    TacticLib.travelToMonster(monsterId,monsterAvoidDistance).lift(), 
	                    TacticLib.travelToMonster(monsterId,0).lift(), 
	                    // action("DEBUG").do1(S -> S).on(S -> { System.out.println(">>> debug") ; return null ; }).lift(),
	                    ABORT()));
	    
	    return g.lift() ;
	}

	/**
	 * A goal to get an agent to the location of an non-monster entity.
	 */
	public static GoalStructure entityVisited(TestAgent agent, String entityId, float monsterAvoidDistance) {
	    return  GoalLib.locationVisited_2(agent,entityId,null,monsterAvoidDistance).lift() ;
	}

	public static GoalStructure entityVisited_5_level(TestAgent agent, String entityId, float monsterAvoidDistance) {
	    return  GoalLib.locationVisited_2_5_level(agent, entityId,null,monsterAvoidDistance).lift() ;
	}

	public static GoalStructure entityVisited_all(TestAgent agent, String entityId, float monsterAvoidDistance) {
		return  GoalLib.locationVisited_2_all(agent, entityId,null,monsterAvoidDistance).lift() ;
	}

	public static GoalStructure locationVisited(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
	    return GoalLib.locationVisited_2(agent,entityId,destination,monsterAvoidDistance).lift() ;
	}

	public static GoalStructure locationVisited_5_level(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
	    return GoalLib.locationVisited_2_5_level(agent, entityId,destination,monsterAvoidDistance).lift() ;
	}

	public static GoalStructure locationVisited_all(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
		return GoalLib.locationVisited_2_all(agent, entityId,destination,monsterAvoidDistance).lift() ;
	}

	public static Goal locationVisited_1(String entityId, Vec3 destination, float monsterAvoidDistance) {
	    
	    String destinationName = entityId == null ? destination.toString() : entityId ;
	    Goal g = goal(destinationName + " is visited") 
	            .toSolve((MyAgentState S) -> {
	            
	          		//WorldEntity inv = S.wom.getElement("Inventory");
	            	//int size = inv.elements.size();
	    			//System.out.println("IInventory size: " + size );
	            	
	            	
	                Vec3 destination_ = destination ;
	                if(entityId != null) {
	                    WorldEntity e = S.wom.getElement(entityId) ;
	                    if(e == null) {
	                        // the case when the target-entity does not exist, we consider
	                        // the goal to be solved:
	                        return true ;
	                        // throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
	                    }
	                    destination_ = e.position ;
	                }
	                return Utils.sameTile(S.wom.position, destination_) ;
	            })
	            .withTactic(FIRSTof(
	            		      TacticLib.abortIfDead(),
	            		      TacticLib.checkIfEntityNoLongerExists(entityId),
	            			  TacticLib.useHealthToSurvive().lift(),
	            			  TacticLib.equipBestAvailableWeapon().lift(),
	                          TacticLib.bowAttack().lift(),
	                          TacticLib.meleeAttack().lift(),
	                          TacticLib.travelTo(entityId,destination,monsterAvoidDistance).lift(), 
	                          TacticLib.travelTo(entityId,destination,0).lift(), 
	                          ABORT()));
	    
	    return g ;
	}

	public static Goal locationVisited_2(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
	    Goal g = locationVisited_1(entityId,destination,monsterAvoidDistance) ;
	    Tactic baseTactic = g.getTactic() ;
	    Tactic extendedTactic = FIRSTof(
	            TacticLib.collectHealthItemsIfNeeded(agent,monsterAvoidDistance), // won't be enabled if dead
	            baseTactic // will abort if dead
	            ) ;                       
	    return g.withTactic(extendedTactic) ; 
	}

	public static Goal locationVisited_1_5_level(String entityId, Vec3 destination, float monsterAvoidDistance) {
	    
	    String destinationName = entityId == null ? destination.toString() : entityId ;
	    Goal g = goal(destinationName + " is visited") 
	            .toSolve((MyAgentState S) -> {
	            
	            	WorldModel current = S.wom ;
	            	
	    	        String agentId = S.wom.agentId ;
	    	        WorldEntity agentCurrentState = current.elements.get(agentId) ;
	    	       
	
	    	        
	    	        int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
	    	       
	            	
	                Vec3 destination_ = destination ;
	                
	
	                if(entityId != null) {
	                    WorldEntity e = S.wom.getElement(entityId) ;
	                    if(e == null) {
	                        throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
	                    }
	                    destination_ = e.position ;
	                }
	                //System.out.println("### stairs: " + entityId + ", @" + destination_) ;
	                //System.out.println("### curlevel: " + currentLevel) ;
	                return ( (Utils.sameTile(S.wom.position, destination_)) && (currentLevel==5) ) ;
	            })
	            .withTactic(FIRSTof(
	            		      TacticLib.abortIfDead(),
	            		      TacticLib.loadNewLevel().lift(),
	            			  TacticLib.useHealthToSurvive().lift(),
	            			  TacticLib.equipBestAvailableWeapon().lift(),
	                          TacticLib.bowAttack().lift(),
	                          TacticLib.meleeAttack().lift(),
	                          TacticLib.travelTo(entityId,destination,monsterAvoidDistance).lift(), 
	                          TacticLib.travelTo(entityId,destination,0).lift(), 
	                          ABORT()));
	    
	    return g ;
	}

	public static Goal locationVisited_2_5_level(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
	    Goal g = locationVisited_1_5_level(entityId,destination,monsterAvoidDistance) ;
	    Tactic baseTactic = g.getTactic() ;
	    Tactic extendedTactic = FIRSTof(
	            TacticLib.loadNewLevel().lift(), // FIX <---
	            TacticLib.collectHealthItemsIfNeeded(agent,monsterAvoidDistance),
	            TacticLib.killBossFirst(agent),// won't be enabled if dead
	            baseTactic // will abort if dead
	            ) ;                       
	    return g.withTactic(extendedTactic) ; 
	}

	public static Goal locationVisited_1_all(String entityId, Vec3 destination, float monsterAvoidDistance) {
	
		String destinationName = entityId == null ? destination.toString() : entityId ;
		Goal g = goal(destinationName + " is visited") 
		.toSolve((MyAgentState S) -> {
		
			WorldModel current = S.wom ;
			
			String agentId = S.wom.agentId ;
			WorldEntity agentCurrentState = current.elements.get(agentId) ;
			
			
			
			int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
			
			
			
			Vec3 destination_ = destination ;
			
			
			if(entityId != null) {
				WorldEntity e = S.wom.getElement(entityId) ;
				
				if(e == null) {
					throw new IllegalArgumentException("Entity " + entityId + " does not exists!") ;
				}
				
			destination_ = e.position ;
			
			}
			//System.out.println("### stairs: " + entityId + ", @" + destination_) ;
			//System.out.println("### curlevel: " + currentLevel) ;
			return ( (Utils.sameTile(S.wom.position, destination_)) && (currentLevel==1) ) ;
		})
		.withTactic(FIRSTof(
					TacticLib.abortIfDead(),
					TacticLib.loadNewLevel().lift(),
					TacticLib.useHealthToSurvive().lift(),
					TacticLib.equipBestAvailableWeapon().lift(),
					TacticLib.bowAttack().lift(),
					TacticLib.meleeAttack().lift(),
					TacticLib.travelTo(entityId,destination,monsterAvoidDistance).lift(), 
					TacticLib.travelTo(entityId,destination,0).lift(), 
					ABORT()
					)	);
		
		return g ;
	}

	public static Goal locationVisited_2_all(TestAgent agent, String entityId, Vec3 destination, float monsterAvoidDistance) {
	Goal g = locationVisited_1_all(entityId,destination,monsterAvoidDistance) ;
	Tactic baseTactic = g.getTactic() ;
	Tactic extendedTactic = FIRSTof(
			TacticLib.collectHealthItemsIfNeeded(agent, monsterAvoidDistance),
			//Utils.interactWithEverything(agent,monsterAvoidDistance), // won't be enabled if dead
			baseTactic // will abort if dead
	) ;                       
	return g.withTactic(extendedTactic) ; 
	}

}

package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.exception.Iv4xrError;
import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.W3DEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.framework.spatial.meshes.Mesh;
import nl.uu.cs.aplib.mainConcepts.Environment.EnvOperation;
import nl.uu.cs.aplib.utils.Pair;
import A.B.*;



/**
 * This class MyEnv provides the interface between iv4XR test agents and the game Nethack
 * (more precisely, with a simplified Java-clone of the original Nethack).
 * We will not interact directly with Nethack, but rather MyEnv interacts with it through
 * a "wrapper" cally NethackWrapper. The latter provides a set of more convenient methods
 * for interactions, that also return observations in term of World Models.
 */
public class MyEnv extends W3DEnvironment {

    /**
     * The navigation graph of the current level.
     */
    public SimpleNavGraph navgraph ;
    
	NethackWrapper nethackUnderTest ;
	
	public MyEnv(NethackWrapper nhwrapper) {
	    super() ;
	    nethackUnderTest = nhwrapper ;
	    
	}


	/**
	 * In principle this is the primary method you need to implement. This method is the one
	 * that will send a command to the SUT and reports back the state of the SUT.
	 * 
	 * IMPORRTANT: the method should return an instance of WorldModel (eu.iv4xr.framework.mainConcepts.WorldModel).
	 * The SUT might send something else; but this method sendCommand_() should
	 * then transform it into an instance of WorldModel. It can be literally an
	 * instance of WorldModel, or a subclass of it if you have created your own
	 * subclass to enrich it.
	 * 
	 * The method sendCommand_() sends a command, which is represented as an instance 
	 * of the class EnvOperation (nl.uu.cs.aplib.mainConcepts.Environment.EnvOperation).
	 * 
	 * To create a command we can use the constructor of EnvOperation:
	 * 
	 *    new EnvOperation(String invokerId, 
	 *                     String targetId, 
	 *                     String command, 
	 *                     Object arg, 
	 *                     Class expectedTypeOfResult)
	 *    
	 *    invokerId: the id/name of the agent at the SUT-side which we want to command.
	 *    command:   the command for the agent meant by invokerId to do something (e.g. 
	 *               to move, or to interact with something else)
	 *    targetId:  if the command is a command like "interact" it usually also involves 
	 *               the target of the interaction (e.g. imagine a command for agent A in the SUT to
	 *               open a door in the SUT; A would be invokerId, and the door would be the target).
	 *               
	 *    arg:       a single object that is the parameter of the command. For example a command like "move"
	 *               might require a direction to be specified as a parameter. Only one parameter is
	 *               accomodated in this protocol. If you need multiple parameters, you should wrap it
	 *               in a single object and deal with packing and unpacking.
	 *               
	 *    expectedTypeOfResult: can be ignored for now.
	 *    
	 * Examples: 
	 *     
	 *        new EnvOperation("agent0",null,"Observe",null,Dontcare.class)
	 *        new EnvOperation("agent0","button1","Interact",null,Dontcare.class)
	 *        
	 */ 
	protected Object sendCommand_(EnvOperation cmd) {

		// finish this implementation

		Object RawSUTstate ; // in the switch below, put the value sent back by the SUT in this variable

		WorldModel SUTstate = null ;
		
		switch(cmd.command) {
			case "Observe":
				SUTstate = nethackUnderTest.observe();
				break;

			case "Move":
				SUTstate = nethackUnderTest.move((NethackWrapper.Movement) cmd.arg);
				/* send this command to the SUT, wait for its response and repackage it */
				;
				break;

			case "Interact":
				
				SUTstate = nethackUnderTest.action((NethackWrapper.Interact) cmd.arg, cmd.targetId);
				/* send this command to the SUT, wait for its response and repackage it */
				;
				break;

			case "StartNewGame":
				nethackUnderTest.startNewGame();
				navgraph = nethackUnderTest.getNavigationGraph() ;
				return null ;

			case "RestartGame":
				nethackUnderTest.restartGame();
                navgraph = nethackUnderTest.getNavigationGraph() ;
				return null ;
		}

		// at this point the variable RawSUTstate should contain a representation of the SUT-state, 
		// as sent back by the SUT through the above command.
		// Next, we need to either cast it, or somehow post-process it to an instance of WorldModel:

		return SUTstate ;
	}

	/**
	 * Send a command to the SUT to to send back its state. You should have this method,
	 * as the agent's framework automatically invoke it at every update cycle.
	 * 
	 * The effect of this method is equivalent to calling:
	 * 
	 *     sendCommand_(new EnvOperation(agentId,null,"Observe",null,Dontcare.class)
	 *     
	 *     
	 * the result is cast to WorldModel (eu.iv4xr.framework.mainConcepts.WorldModel).    
	 * 
	 * In principle you don't need to redefine this method as long as you define the
	 * corresponding logic in sendCommand_().
	 */
	@Override
	public WorldModel observe(String agentId) {
	    throw new UnsupportedOperationException() ;
		// return (WorldModel) sendCommand("player",null,"Observe",null,WorldModel.class) ;
	}	
	
	
	public WorldModel observe() {
        return (WorldModel) sendCommand("player",null,"Observe",null,WorldModel.class) ;
    }

	/**
	 * Command the in-SUT agentId to interact on another in-SURT entity identified by
	 * targetId. This method pass the call to sendCommand(..) from an ancestor class, 
	 * which in turn will call your sendCommand_() above. The effect is equivalent to
	 * calling:
	 * 
	 *     sendCommand_(new EnvOperation(agentId,targetId,"Interact",null,Dontcare.class)
	 *     
	 *     
	 * the result of sendCommand_ is then cast to WorldModel (eu.iv4xr.framework.mainConcepts.WorldModel).    
	 * 
	 * In principle you don't need to redefine this method as long as you define the
	 * corresponding logic in sendCommand_().
	 */
	public WorldModel interact(String agentId, String targetId, NethackWrapper.Interact typeOfInteract ) {
		return (WorldModel) sendCommand(agentId, targetId, "Interact", typeOfInteract, WorldModel.class);
	}


	/**
	 * A command to instruct the in-SUT agentId to move a small distance towards the given
	 * target location. The method pass the call to sendCommand(..) from an ancestor class, 
	 * which in turn will call your sendCommand_() above. The effect is equivalent to
	 * calling:
	 * 
	 *     sendCommand_(new EnvOperation(agentId,
	 *                                   null,
	 *                                   "Move",
	 *                                   Pair(agentLocation,targetLocation),
	 *                                   Dontcare.class)
	 *     
	 *     
	 * the result is cast to WorldModel (eu.iv4xr.framework.mainConcepts.WorldModel).    
	 * 
	 * In principle you don't need to redefine this method as long as you define the
	 * corresponding logic in sendCommand_().
	 */
	@Override
	public WorldModel moveToward(String agentId, Vec3 agentLocation, Vec3 targetLocation) {
		throw new UnsupportedOperationException() ;
	}
	
	
	public WorldModel move(NethackWrapper.Movement direction) {
        return (WorldModel) sendCommand("player",null,
                "Move",
                direction,
                WorldModel.class) ;
    }

	/**
	 * Send a command to the real environment that should cause it to send over
	 * the navigation-mesh of its 3D world (well, it has it).  This mesh is 
	 * assumed to be static (does not change through out the agents' runs).
	 * 
	 * In principle you don't need to redefine this method as long as you define the
	 * corresponding logic in sendCommand_().
	 */
	public void dd() {
	    throw new UnsupportedOperationException() ;
	}
	
	
   public void startNewGame() {
        sendCommand(null,null,"StartNewGame",null,Mesh.class) ;
    }

   public void restartGame() {
       sendCommand(null,null,"RestartGame",null,Mesh.class) ;
   }


}

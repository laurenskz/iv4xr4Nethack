package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.action;

import eu.iv4xr.framework.mainConcepts.W3DAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Tactic;

public class TacticLib {

	/**
	 * This constructs a tactic; when invoked by an agent, the tactic will return the observed state of the SUT.
	 * 
	 * The tactic is always enabled.
	 */
	public static Tactic observe() { 
		Action observe = 
				// lets first define an "action" that will do the "observe" :
				action("Observe")

				// specify what the action does. Conceptually, it is a function from agent-state to agent-state,
				// which may in between also update this state as well (not the case here, but it can):

				.do1((W3DAgentState belief) -> {
					// Since MyEnv already provide a method observe(), we can invoke it like this:
					//
					//     belief.env().observe(belief.wom.agentId);
					//
					// BUT the agent's runtime automatically call it anyway to refresh the agent
					// state. So we can just do:

					return belief;
				});

		// ok so you have an action; but what you want is a Tactic. We can lift an action to
		// turn it to a tactic:
		return observe.lift();

	}

	/**
	 * This constructs a tactic. When it is invoked by an agent, it will cause the agent to interact with
	 * the given entity.
	 * 
	 * The tactic is only enabled when interaction is possible. You have to implement a proper guard that
	 * can decide/predict if interaction would be possible or otherwise.
	 
	public static Tactic interact(String entityId) { 

		Tactic interact = 
				// let's first define an action that will do "interact". Your MyEnv provides such a method:
				action("Interact")

				// specify what the action does. Conceptually, it is a function from agent-state to agent-state,
				// which may in between also update this state as well:

				. do2((W3DAgentState belief) -> (WorldEntity e) -> {

					// invoke the method inteact() in MyEnv, and stored the resulting observed
					// SUT-state in some variable:
				    throw new UnsupportedOperationException() ;
					//var obs = ((MyEnv) belief.env()).interact(belief.wom.agentId,entityId);

					// We can alternatively do this through wom.interact(), which will eventually forward
					// the call to env.interact():
					// var obs = belief.wom.interact(belief.env(),"Interact", e);


					// we will merge the obtained observation into belief.wom. This wom acts as
					// aggregate of all observations collected so far:
					//belief.wom.mergeNewObservation(obs);

					//return belief;
				})

				// Next we need to set a guard, when "interact" would be possible. E.g. the SUT might not
				// allow "interact" if the agent is still to far from the target entity:

				. on((W3DAgentState belief) -> {

					// some check here if the agent would be able to interact. If so, return the entity
					// else null:

					var e = belief.wom.getElement(entityId);
					if (e==null) return null;
					// check if e can be interacted to, e.g. if it is within some distance epsilon:
					float epsilon = 0.5f;
					boolean canInteract = Vec3.dist(e.position,belief.wom.position) <= epsilon;

					return canInteract ? e : null;

				})

				// finally, lift the action to turn it to a tactic:
				. lift();

		return interact;

	}
	*/

	/**
	 * A tactic that would drive the agent to trave to the given entity. The implementation of
	 * this depends on the SUT, e.g. whether it already has path-finding, or else whether it
	 * would provide some kind of navigation map, etc.
	 */
	public static Tactic navigateToEntity(String entityId) { 
		throw new UnsupportedOperationException();
	}

	/**
	 * A tactic to explore the SUT's virtual world.
	 */
	public static Tactic explore() { 
		throw new UnsupportedOperationException();
	}

}

package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.MCPolicyGradient
import eu.iv4xr.framework.model.rl.algorithms.QActorCritic
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.LinearStateValueFunction
import eu.iv4xr.framework.model.rl.policies.QCountBasedICMModule
import eu.iv4xr.framework.model.rl.policies.QFromMerged
import eu.iv4xr.framework.model.rl.policies.SoftmaxPolicy

class MCPolicyGradientSolver(private val episodes: Int = 10, private val gamma: Float = 0.99f, private val conf: ICMQConf) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val policy = SoftmaxPolicy(actionRepeatingFactory, configuration.mdp, 0.2)

        val alg = MCPolicyGradient(policy, icm, LinearStateValueFunction(factory, 0.01), 0.1, episodes, gamma.toDouble(), configuration.random)
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                "Policy gradient MC - ${conf.toString().replace("_", " ").toLowerCase()}",
                episodes,
                gamma,
                null, null
        )
    }
}

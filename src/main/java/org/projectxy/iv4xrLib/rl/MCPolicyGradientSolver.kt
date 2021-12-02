package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.MCPolicyGradient
import eu.iv4xr.framework.model.rl.algorithms.QActorCritic
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.QCountBasedICMModule
import eu.iv4xr.framework.model.rl.policies.QFromMerged
import eu.iv4xr.framework.model.rl.policies.SoftmaxPolicy

class MCPolicyGradientSolver(private val episodes: Int = 10, private val gamma: Float = 0.99f, private val conf: ICMQConf) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val policy = SoftmaxPolicy(actionRepeatingFactory, configuration.mdp, 0.003)

        val alg = MCPolicyGradient(policy, episodes, gamma.toDouble(), configuration.random)
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                "Policy gradient MC - ${conf.toString().replace("_", " ").toLowerCase()}",
                episodes,
                gamma,
                null, null
        )
    }
}

package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.QActorCritic
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.QCountBasedICMModule
import eu.iv4xr.framework.model.rl.policies.QFromMerged
import eu.iv4xr.framework.model.rl.policies.SoftmaxPolicy

class PolicyGradientSolver(private val episodes: Int = 10, private val gamma: Float = 0.9f, private val conf: ICMQConf) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val qFunction = QFromMerged(actionRepeatingFactory, 0.1)
        val policy = SoftmaxPolicy(actionRepeatingFactory, configuration.mdp, 4.0)
        val alg = QActorCritic(policy, qFunction, icm, configuration.random, gamma.toDouble(), episodes, 1.0)
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                "Policy gradient ICM - ${conf.toString().replace("_", " ").toLowerCase()}",
                episodes,
                gamma,
                null, null
        )
    }
}

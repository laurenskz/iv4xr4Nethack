package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.ExploreAndConnect
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.MCSampleGreedyPolicy
import eu.iv4xr.framework.model.rl.policies.QCountBasedICMModule
import eu.iv4xr.framework.model.rl.policies.QFromMerged

enum class ICMQConf(val initialQ: Double, val countFun: (Double) -> Double) {
    POSITIVE_REWARDS(0.0, { 1.0 / (it + 1) }),
    POSITIVE_REWARDS_OPTIMISTIC(1.0, { 1.0 / (it + 1) }),
    NEGATIVE_REWARDS(0.0, { 1.0 / (it + 1) - 1.0 })
}

class CountBasedICMSolver(private val episodes: Int = 10, private val gamma: Float = 0.9f, private val conf: ICMQConf, val maxSteps: Int = 10000, val epsilon: Double) : NethackSolver {

    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val qFunction = QFromMerged(actionRepeatingFactory, 1.0)
        val exploreFun = QFromMerged(actionRepeatingFactory, 1.0, conf.initialQ)
        val alg = ExploreAndConnect(icm, configuration.random,
                qFunction, exploreFun, gamma, 0.999f, 2, episodes,
                MCSampleGreedyPolicy(qFunction, configuration.mdp, episodes, gamma, episodes, configuration.random), maxSteps, epsilon
        )
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                "Count based ICM - ${conf.toString().replace("_", " ").toLowerCase()}",
                episodes,
                gamma,
                null, null
        )
    }
}
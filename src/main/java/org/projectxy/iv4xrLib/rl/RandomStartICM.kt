package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.ExploreAndLearn
import eu.iv4xr.framework.model.rl.algorithms.RandomStartICM
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.QCountBasedICMModule
import eu.iv4xr.framework.model.rl.policies.QFromMerged

class RandomStartICMSolver(private val episodes: Int = 10, private val gamma: Float = 0.9f, private val conf: ICMQConf, val maxSteps: Int = 10000, val epsilon: Double) : NethackSolver {
    override val name: String
        get() = "Random start"

    override fun train(configuration: NethackSolveConfiguration, episodes: List<Int>, callback: (NethackSolverCallback) -> Unit): NethackSolveOutput {
        return train(configuration)
    }

    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val qFunction = QFromMerged(actionRepeatingFactory, 1.0)
        val exploreFun = QFromMerged(actionRepeatingFactory, 1.0, conf.initialQ)
        val goalDiscoverer = RandomStartICM(
                configuration.mdp, icm, exploreFun, gamma, configuration.random, maxSteps, epsilon
        )
        val alg = ExploreAndLearn(configuration.random, qFunction, goalDiscoverer, 0.999f, 5)
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                name,
                episodes,
                gamma,
                epsilon, null
        )
    }
}

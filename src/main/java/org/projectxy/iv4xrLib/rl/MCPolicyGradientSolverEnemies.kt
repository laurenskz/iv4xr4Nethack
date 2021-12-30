package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.MCPolicyGradient
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.*

class MCPolicyGradientSolverEnemies(private val episodes: Int = 10, private val gamma: Float = 0.99f, private val conf: ICMQConf) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        return train(configuration, listOf(episodes)) {}
    }

    override val name: String
        get() = "MC ICM"

    override fun train(configuration: NethackSolveConfiguration, episodes: List<Int>, callback: (NethackSolverCallback) -> Unit): NethackSolveOutput {
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)

        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val icm = QCountBasedICMModule<StateWithGoalProgress<NethackModelState>, NethackModelAction>(QFromMerged(actionRepeatingFactory, 1.0), conf.countFun)
        val policy = SoftmaxPolicy(actionRepeatingFactory, configuration.mdp, 0.2)

        val valueFunction = LinearStateValueFunction(factory, 0.005)
        val visitFunction = LinearStateValueFunction(factory, 1.0)
        var total = 0
        episodes.forEach {
            val alg = MCPolicyGradient(policy, icm, valueFunction, visitFunction, 0.1, it, gamma.toDouble(), configuration.random)
            configuration.agent.trainWith(alg)
            callback(NethackSolverCallback(
                    ValueFunctionWithoutProgress(valueFunction),
                    ValueFunctionWithoutProgress(visitFunction),
                    total + it
            ))
            total += it
        }
        return NethackSolveOutput(
                name,
                episodes.sum(),
                gamma,
                null, null
        )
    }
}

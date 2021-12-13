package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.StateWithGoalProgress
import eu.iv4xr.framework.model.rl.algorithms.*
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.NoOpStateEncoder
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.*

class ConvSolver(val gamma: Float, val episodes: Int) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {

        val factory = NethackModelState.tensorFactory(configuration.state.getConf())
        val qFunction = TFQFunction(
                NoOpStateEncoder(factory), configuration.mdp,
        ) {
            QModelDefBuilder(Sequential(
                    convLayer(3, 3, 32, "conv1"),
//                    maxPoolLayer(2, 2, 2),
//                    convLayer(3, 3, 64, "conv2"),
                    flatten()
            ), factory.shape, it, 0.1f)
        }
        val alg = DeepQLearning(qFunction, configuration.random, gamma, 16, 10000, 0.3)
        configuration.agent.trainWith(alg)
        return NethackSolveOutput(
                "Convolutional",
                episodes,
                gamma,
                null, null
        )
    }
}
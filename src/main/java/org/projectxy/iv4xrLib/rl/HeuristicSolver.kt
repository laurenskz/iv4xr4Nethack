package org.projectxy.iv4xrLib.rl

import eu.iv4xr.framework.model.rl.Heuristic
import eu.iv4xr.framework.model.rl.algorithms.DeepSARSA
import eu.iv4xr.framework.model.rl.algorithms.OffPolicyQLearning
import eu.iv4xr.framework.model.rl.approximation.ActionRepeatingFactory
import eu.iv4xr.framework.model.rl.approximation.stateWithGoalProgressFactory
import eu.iv4xr.framework.model.rl.policies.GreedyPolicy
import eu.iv4xr.framework.model.rl.policies.QFromMerged
import eu.iv4xr.framework.spatial.Vec3
import kotlin.random.Random

class HeuristicSolver(val episodes: Int, val gamma: Float, val algName: String, val epsilon: Double? = null, val n: Int? = null) : NethackSolver {
    override fun train(configuration: NethackSolveConfiguration): NethackSolveOutput {
        val stairs = configuration.mdp.initialState().support().first().state.stairs
        configuration.agent.mdp.heuristics.add(object : Heuristic<NethackModelState, NethackModelAction> {
            override fun reward(state: NethackModelState, action: NethackModelAction, statePrime: NethackModelState): Double {
                val d = Vec3.dist(stairs, state.position)
                val dp = Vec3.dist(stairs, statePrime.position)
                return (d - dp).toDouble()
            }
        })
        val factory = stateWithGoalProgressFactory(NethackModelState.factoryFrom(configuration.state.getConf()), 1)
        val actionRepeatingFactory = ActionRepeatingFactory(factory, configuration.mdp.allPossibleActions().toList())
        val qFunction = QFromMerged(actionRepeatingFactory, 0.1)
        val name = "Heuristic - $algName"
        if (algName == "Q-learning") {
            val alg = OffPolicyQLearning(qFunction, 0.9f, configuration.mdp, configuration.random)
            alg.trainEPolicy(episodes)
            configuration.agent.policy = GreedyPolicy(qFunction, configuration.mdp)
            return NethackSolveOutput(name, episodes, gamma, epsilon, n)
        } else {
            epsilon ?: error("Sarsa requires epsilon")
            n ?: error("Sarsa requires n")
            val alg = DeepSARSA(qFunction, epsilon, configuration.random, gamma, episodes, n)
            configuration.agent.trainWith(alg)
            return NethackSolveOutput(name, episodes, gamma, epsilon, n)
        }
    }
}
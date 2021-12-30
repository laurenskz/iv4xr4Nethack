package org.projectxy.iv4xrLib.rl.study

import A.B.NethackConfiguration
import org.projectxy.iv4xrLib.rl.*
import kotlin.time.ExperimentalTime

val confs2 = listOf(
        NethackSolveInput(
                "Simple",
                16,
                NethackConfiguration().also {
                    it.rows = 25
                    it.columns = 25
                    it.roomCount = 7
                    it.seed = 16
                    it.minMobs = 15
                },
                movementActions,
                PerformanceMode
        )
)

val solvers2 = listOf(
//        RandomStartICMSolver(10, 0.8f, ICMQConf.POSITIVE_REWARDS, epsilon = 0.2)
        MCPolicyGradientSolverEnemies(1000, 0.999f, ICMQConf.POSITIVE_REWARDS)
//        ConvSolver(0.9f, 80000)
//        CountBasedICMSolver(10, 0.999f, ICMQConf.NEGATIVE_REWARDS, 10000, 0.0),
//        CountBasedICMSolver(10, 0.8f, ICMQConf.NEGATIVE_REWARDS, 10000, 0.0),
//        CountBasedICMSolver(10, 0.999f, ICMQConf.POSITIVE_REWARDS, 10000, 0.2),
//        CountBasedICMSolver(10, 0.8f, ICMQConf.POSITIVE_REWARDS, 10000, 0.2),
//        CountBasedICMSolver(10, 0.999f, ICMQConf.POSITIVE_REWARDS_OPTIMISTIC, 10000, 0.2),
//        CountBasedICMSolver(10, 0.8f, ICMQConf.POSITIVE_REWARDS_OPTIMISTIC, 10000, 0.2),
//        HeuristicSolver(1000, 0.999f, "SARSA", 0.2, 4),
//        HeuristicSolver(1000, 0.999f, "Q-learning")
)

@ExperimentalTime
fun main() {
    runExperiment(solvers2, confs2, 5000)
}
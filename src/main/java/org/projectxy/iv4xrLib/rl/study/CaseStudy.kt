package org.projectxy.iv4xrLib.rl.study

import A.B.NethackConfiguration
import eu.iv4xr.framework.mainConcepts.WorldModel
import eu.iv4xr.framework.model.rl.RLAgent
import eu.iv4xr.framework.model.rl.valuefunctions.Valuefunction
import eu.iv4xr.framework.spatial.Vec3
import eu.iv4xr.framework.utils.cons
import nl.uu.cs.aplib.AplibEDSL
import org.projectxy.iv4xrLib.MyAgentState
import org.projectxy.iv4xrLib.MyNethackEnv
import org.projectxy.iv4xrLib.NethackWrapper
import org.projectxy.iv4xrLib.NethackWrapper.Interact.AimWithBow
import org.projectxy.iv4xrLib.NethackWrapper.Interact.PickupItem
import org.projectxy.iv4xrLib.NethackWrapper.Movement.*
import org.projectxy.iv4xrLib.Utils
import org.projectxy.iv4xrLib.rl.*
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

val movementActions = listOf(Move(DOWN), Move(UP), Move(LEFT), Move(RIGHT))
val itemActions = listOf(
        Move(DOWN), Move(UP), Move(LEFT), Move(RIGHT), Move(DONOTHING),
        Action(PickupItem),
        UseItem(0), UseItem(1), UseItem(2),

        )
val actionMap = mapOf(
        "Movements" to movementActions,
        "Movements and items" to itemActions
)

private val confs = listOf(
        NethackSolveInput(
                "Simple",
                16,
                NethackConfiguration().also {
                    it.rows = 25
                    it.columns = 25
                    it.roomCount = 7
                    it.seed = 16
                },
                movementActions,
                PerformanceMode
        ),
//        NethackSolveInput(
//                "Simple",
//                16,
//                NethackConfiguration().also {
//                    it.rows = 25
//                    it.columns = 25
//                    it.roomCount = 7
//                    it.seed = 16
//                },
//                movementActions,
//                ValuedMode(listOf(10, 1000, 1000))
//        ),
        NethackSolveInput(
                "Large",
                16,
                NethackConfiguration().also {
                    it.seed = 3;
                    it.rows = 50;
                    it.columns = 90;
                    it.roomCount = 15;
                    it.minMobs = 0;
                    it.maxMobs = 1;
                },
                movementActions,
                PerformanceMode
        ),
        NethackSolveInput(
                "Large-15hp",
                16,
                NethackConfiguration().also {
                    it.seed = 3;
                    it.rows = 50;
                    it.columns = 90;
                    it.roomCount = 15;
                    it.minMobs = 0;
                    it.maxMobs = 1;
                    it.initialHealth = 15;
                },
                movementActions,
                ValuedMode(listOf(1000, 1000, 1000, 1000, 1000))
        ),
        NethackSolveInput(
                "Large-10hp",
                16,
                NethackConfiguration().also {
                    it.seed = 3;
                    it.rows = 50;
                    it.columns = 90;
                    it.roomCount = 15;
                    it.minMobs = 0;
                    it.maxMobs = 1;
                    it.initialHealth = 10;
                },
                movementActions,
                ValuedMode(listOf(1000, 1000, 1000, 1000, 1000))
        )
)

fun NethackConfiguration.header() = mapOf(
        "Rows" to rows.toString(),
        "Columns" to columns.toString(),
        "Room count" to roomCount.toString(),
        "Seed" to seed.toString(),
        "Mobs" to (minMobs > 0).toString(),
)

fun NethackSolveInput.header() = mapOf(
        "Name" to name,
        "Seed" to seed.toString(),
        "Enabled actions" to actionMap.filter { it.value == enabledActions }.keys.first()

) + nethackConfiguration.header()

@ExperimentalTime
fun CaseStudyRow.header() = mapOf(
        "Configuration" to input.name,
        "Success" to success.toString(),
        "Algorithm" to this.output.name,
        "Gamma" to this.output.gamma.toString(),
        "Duration" to duration.toString(TimeUnit.MILLISECONDS)
)


val solvers = listOf(
//        RandomStartICMSolver(10, 0.8f, ICMQConf.POSITIVE_REWARDS, epsilon = 0.2)
        MCPolicyGradientSolver(100000, 0.999f, ICMQConf.POSITIVE_REWARDS)
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


data class CaseStudyRow @ExperimentalTime constructor(
        val input: NethackSolveInput,
        val output: NethackSolveOutput,
        val resultState: NethackModelState,
        val success: Boolean,
        val duration: Duration
)

fun toRow(strings: List<String>) = strings.joinToString(" & ") + "\\\\"

fun <T> toTable(t: List<T>, extractor: (T) -> Map<String, String>): String {
    val first = extractor(t.first())
    val keys = first.keys.toList()
    val header = toRow(keys)
    val rows = t.map {
        val extracted = extractor(it)
        val strings = keys.map { extracted[it] ?: error("Key not found") }
        toRow(strings)
    }
    val body = (header cons ("\\hline" cons rows)).joinToString("\n")
    return "\\begin{tabular}{|${keys.map { "c" }.joinToString("|")}|}\n\\hline\n" + body + "\n\\hline\n\\end{tabular}"
}

fun <T> toTable(t: List<T>, extractor: (T) -> Map<String, String>, fileName: String) {
    FileOutputStream(fileName).writer().use {
        it.write(
                toTable(t, extractor)
        )
    }
}

@ExperimentalTime
fun main() {
    runExperiment(solvers, confs,30)
}

@ExperimentalTime
fun runExperiment(solvers: Iterable<NethackSolver>, confs: List<NethackSolveInput>, pause: Long) {
    confs.filter { it.mode is ValuedMode }.forEach { input ->
        solvers.forEach { solver ->
            evaluateEpisodeSolver(input, solver)
        }
    }

    toTable(confs, NethackSolveInput::header, "casestudy2confs.tex")
    val results = confs.filter { it.mode == PerformanceMode }.flatMap { conf ->
        solvers.map { solver ->
            evaluateSolver(conf, solver, pause)
        }
    }
    toTable(results, CaseStudyRow::header, "casestudy2results.tex")
}

fun evaluateEpisodeSolver(input: NethackSolveInput, solver: NethackSolver) {
    if (input.mode !is ValuedMode) return
    val wrapper = NethackWrapper()
    val configuration = createConfiguration(input, wrapper)
    val valueVisualizer = NethackVisualizer(configuration.state.getConf(), configuration.agent.mdp.initialState().sample(Random).state, Functions.linear)
    val visitVisualizer = NethackVisualizer(configuration.state.getConf(), configuration.agent.mdp.initialState().sample(Random).state, Functions.onOff)
    solver.train(configuration, input.mode.episodes) {
        input.name
        visitVisualizer.visualize(it.visitFunction).writeTo("${input.name}/${solver.name}-visited-${it.episodes}.png")
        valueVisualizer.visualize(it.valueFunction).writeTo("${input.name}/${solver.name}-values-${it.episodes}.png")
    }
}

@ExperimentalTime
fun evaluateSolver(input: NethackSolveInput, solver: NethackSolver, sleepInterval: Long): CaseStudyRow {
    val wrapper = NethackWrapper()
    val configuration = createConfiguration(input, wrapper)
    val out = measureTimedValue {
        solver.train(configuration)
    }
    var step = 0
    while (configuration.agent.goal.status.inProgress() && step++ < 1000) {
        Thread.sleep(sleepInterval)
        configuration.agent.update()
    }
    val resultState = configuration.agent.currentState()
    val succes = configuration.agent.goal.status.success()
    wrapper.closeNethack()
    return CaseStudyRow(input, out.value, resultState, succes, out.duration)
}

private fun createConfiguration(input: NethackSolveInput, wrapper: NethackWrapper): NethackSolveConfiguration {
    val random = Random(input.seed)
    wrapper.launchNethack(input.nethackConfiguration)
    val state = MyAgentState().setEnvironment(MyNethackEnv(wrapper))
    val agent = RLAgent(NethackModel(state.getConf()), random)
    agent.attachState(state)

    val goal = AplibEDSL.goal("goal").toSolve { st: Any ->
        if (st is NethackModelState)
            Utils.sameTile(st.position, st.stairs)
        else
            Utils.sameTile((st as WorldModel).position, st.getElement("Stairs").position)
    }.lift()
    goal.maxbudget(5.0)
    agent.setGoal(goal)
    val configuration = NethackSolveConfiguration(input.nethackConfiguration, random, agent.mdp, state, agent)
    return configuration
}
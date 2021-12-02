package org.projectxy.iv4xrLib.rl

import A.B.NethackConfiguration
import eu.iv4xr.framework.model.rl.RLAgent
import eu.iv4xr.framework.model.rl.approximation.NDArrayBuffer
import eu.iv4xr.framework.model.rl.policies.prettyString
import nl.uu.cs.aplib.AplibEDSL
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.projectxy.iv4xrLib.MyAgentState
import org.projectxy.iv4xrLib.MyNethackEnv
import org.projectxy.iv4xrLib.NethackWrapper
import org.tensorflow.ndarray.NdArrays
import org.tensorflow.ndarray.Shape
import kotlin.random.Random

internal class NethackModelTest {

    @Test
    fun test() {
        val wrapper = NethackWrapper()
        val random = Random(22)
        wrapper.launchNethack(NethackConfiguration().also {
            it.rows = 25
            it.columns = 25
            it.roomCount = 7
            it.seed = 16
        })
        val state = MyAgentState().setEnvironment(MyNethackEnv(wrapper))
        val conf = state.getConf()
        val factory = NethackModelState.tensorFactory(conf)
        val agent = RLAgent(NethackModel(state.getConf()), random)
        agent.attachState(state)
        agent.setGoal(AplibEDSL.goal("bab").lift())
        val init = agent.mdp.initialState().sample(Random)
        val of = Shape.of(3, 25, 25)
        val ndArray = NdArrays.ofFloats(of)
        factory.setFrom(init.state, NDArrayBuffer(ndArray, longArrayOf()), longArrayOf())
        println(ndArray.prettyString())
        Thread.sleep(100000)
        wrapper.closeNethack()
    }
}
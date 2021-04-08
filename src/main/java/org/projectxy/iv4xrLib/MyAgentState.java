package org.projectxy.iv4xrLib;

import java.util.*;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.W3DAgentState;
import eu.iv4xr.framework.mainConcepts.W3DEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nl.uu.cs.aplib.agents.State;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

public class MyAgentState extends State {

    public WorldModel previousWom = null;
    public WorldModel wom;

    /**
     * If not null, this contains a path that the agent intent to follow towards a
     * destination. The last element in the list is the destination. The first
     * element in the list is the next tile the agent should move to. After moving
     * to that tile, it is removed from the list. (so, this assumes that that first
     * element is indeed a neiboring tile of the tile where the agent currently is).
     * 
     * The tiles are encoded as integers, representing the indices of the tiles in
     * the navigation graph kept in the field simpleWorldNavigation.
     */
    public List<Integer> currentPathToFollow;

    public SimpleNavGraph simpleWorldNavigation;

    /**
     * Attaching MyEnv to this agent-state. Initializing the first World Model, and
     * initializing the navigation-graph. Assumption: the game is already launched.
     */
    @Override
    public MyAgentState setEnvironment(Environment env) {
        if (!(env instanceof MyEnv))
            throw new IllegalArgumentException();

        MyEnv env_ = (MyEnv) env;

        super.setEnvironment(env_);

        env_.startNewGame();

        this.wom = env_.nethackUnderTest.observe();
        this.simpleWorldNavigation = env_.nethackUnderTest.getNavigationGraph();

        return this;
    }

    @Override
    public void updateState() {
        super.updateState();
        previousWom = wom;
        MyEnv env_ = (MyEnv) this.env();
        this.wom = env_.nethackUnderTest.observe();
    };

}

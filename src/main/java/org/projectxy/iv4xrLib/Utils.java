package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.utils.Pair;

public class Utils {

    /**
     * Conver a 3D coordinate p to a discrete tile-world coordinate. Basically, p
     * will be converted to a pair of integers (p.x,p.y).
     */
    static Pair<Integer, Integer> toTileCoordinate(Vec3 p) {
        return new Pair((int) p.x, (int) p.y);
    }

}

package org.orecruncher.dsurround.runtime.sets;

public interface IEnvironmentState {

    boolean isInVillage();

    boolean isInside();

    boolean isUnderWater();

    boolean hasBlockNearby(String blockId, int range);
}

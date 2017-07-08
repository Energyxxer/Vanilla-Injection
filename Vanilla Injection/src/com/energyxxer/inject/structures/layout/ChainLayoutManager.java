package com.energyxxer.inject.structures.layout;

import com.energyxxer.inject.structures.CommandBlock;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;

/**
 * Interface for defining the layout of a command block chain.
 */
public interface ChainLayoutManager {
    /**
     * Changes the position and orientation of the command blocks in the given list to fit within
     * the specified dimensions.
     *
     * @param list A list of CommandBlock objects to modify, in the order they should be run.
     * @param start The starting point for the command blocks.
     * @param max The maximum size for the structure.
     *
     * @return The starting position for the next chain of command blocks.
     * */
    Vector3D arrange(ArrayList<CommandBlock> list, Vector3D start, Vector3D max);

    /**
     * Calculates the maximum total size of a structure, given the two sizes that define the two chain
     * sections of the structure. Does not take into consideration the padding necessary for the structure block
     * and the fetch section.
     *
     * @param impulseSize A vector representing the size of the impulse chain section.
     * @param repeatingSize A vector representing the size of the repeating chain section.
     *
     * @return The final maximum size.
     * */
    Vector3D mergeSizes(Vector3D impulseSize, Vector3D repeatingSize);
}

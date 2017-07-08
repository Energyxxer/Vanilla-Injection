package com.energyxxer.inject.structures.layout;

import com.energyxxer.inject.structures.CommandBlock;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;

import static com.energyxxer.inject.structures.CommandBlock.Orientation.DOWN;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.EAST;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.SOUTH;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.UP;

/**
 * Chain Layout Manager that arranges command blocks in XYZ order.
 */
public class XYZChainLayoutManager implements ChainLayoutManager {
    @Override
    public Vector3D arrange(ArrayList<CommandBlock> list, Vector3D start, Vector3D max) {
        Vector3D current = start;
        CommandBlock.Orientation orientation = EAST;

        CommandBlock.Orientation yOrientation = UP;
        CommandBlock.Orientation xOrientation = EAST;

        for(CommandBlock block : list) {
            block.setPos(new Vector3D(current));

            Vector3D next;

            while(true) {
                next = current.translated(orientation.vector);
                if(next.x < start.x || next.x >= start.x + max.x) {
                    orientation = yOrientation;
                    xOrientation = xOrientation.opposite;
                    continue;
                }
                if(next.y < start.y || next.y >= start.y + max.y) {
                    orientation = SOUTH;
                    yOrientation = yOrientation.opposite;
                    continue;
                }
                if(next.z >= start.z + max.z) throw new RuntimeException("Commands exceed maximum size given: " + max);
                break;
            }
            block.setOrientation(orientation);
            current = next;
            if(orientation == UP || orientation == DOWN || orientation == SOUTH) {
                orientation = xOrientation;
            }
        }
        return new Vector3D(0,0,start.x + max.z);
    }

    @Override
    public Vector3D mergeSizes(Vector3D impulseSize, Vector3D repeatingSize) {
        return new Vector3D(Math.max(impulseSize.x, repeatingSize.x), Math.max(impulseSize.y, repeatingSize.y), impulseSize.z + repeatingSize.z);
    }
}

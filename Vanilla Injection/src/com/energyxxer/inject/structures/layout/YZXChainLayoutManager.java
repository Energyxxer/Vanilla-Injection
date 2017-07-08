package com.energyxxer.inject.structures.layout;

import com.energyxxer.inject.structures.CommandBlock;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;

import static com.energyxxer.inject.structures.CommandBlock.Orientation.EAST;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.NORTH;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.SOUTH;
import static com.energyxxer.inject.structures.CommandBlock.Orientation.UP;

/**
 * Chain Layout Manager that arranges command blocks in YZX order.
 */
public class YZXChainLayoutManager implements ChainLayoutManager {
    @Override
    public Vector3D arrange(ArrayList<CommandBlock> list, Vector3D start, Vector3D max) {
        Vector3D current = start;
        CommandBlock.Orientation orientation = UP;

        CommandBlock.Orientation yOrientation = UP;
        CommandBlock.Orientation zOrientation = SOUTH;

        for(CommandBlock block : list) {
            block.setPos(new Vector3D(current));

            Vector3D next;

            while(true) {
                next = current.translated(orientation.vector);
                if(next.y < start.y || next.y >= start.y + max.y) {
                    orientation = zOrientation;
                    yOrientation = yOrientation.opposite;
                    continue;
                }
                if(next.z < start.z || next.z >= start.z + max.z) {
                    orientation = EAST;
                    zOrientation = zOrientation.opposite;
                    continue;
                }
                if(next.x >= start.x + max.x) throw new RuntimeException("Commands exceed maximum size given: " + max);
                break;
            }
            block.setOrientation(orientation);
            current = next;
            if(orientation == NORTH || orientation == SOUTH || orientation == EAST) {
                orientation = yOrientation;
            }
        }
        return new Vector3D(start.x + max.x,0,0);
    }

    @Override
    public Vector3D mergeSizes(Vector3D impulseSize, Vector3D repeatingSize) {
        return new Vector3D(impulseSize.x + repeatingSize.x, Math.max(impulseSize.y, repeatingSize.y), Math.max(impulseSize.z, repeatingSize.z));
    }
}

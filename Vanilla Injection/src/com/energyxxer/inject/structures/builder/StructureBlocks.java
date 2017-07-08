package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.IntTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.Tag;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;

/**
 * Class for maintaining all the blocks within a structure file.
 *
 * @see StructureBlockEntry
 * @see StructurePalette
 * @see StructureBuilder
 */
public class StructureBlocks {
    /**
     * A list of all block entries.
     * */
    private ArrayList<StructureBlockEntry> entries = new ArrayList<>();

    /**
     * Creates a StructureBlocks object.
     * */
    StructureBlocks() {}

    /**
     * Adds a <code>StructureBlockEntry</code> to this block master.
     *
     * @param newEntry The entry to add.
     *
     * @see StructureBlockEntry
     * */
    public void addEntry(StructureBlockEntry newEntry) {
        entries.add(newEntry);
    }
    /**
     * Adds a <code>StructureBlockEntry</code> to this block master.
     *
     * @param newEntry The entry to add.
     * @param priority Whether the entry should be added before all others.
     *                 This is used to manage order-specific blocks, such as command blocks.
     *
     * @see StructureBlockEntry
     * */
    public void addEntry(StructureBlockEntry newEntry, boolean priority) {
        if(priority) entries.add(0, newEntry);
        else entries.add(newEntry);
    }

    /**
     * Gets the minimum size required to fit all the blocks in this block master.
     *
     * @return The minimum size.
     * */
    public Vector3D getSize() {
        int x = 0;
        int y = 0;
        int z = 0;
        for(StructureBlockEntry entry : entries) {
            Vector3D pos = entry.getPos();
            if(pos.x > x) x = pos.x;
            if(pos.y > y) y = pos.y;
            if(pos.z > z) z = pos.z;
        }
        return new Vector3D(x+1,y+1,z+1);
    }

    /**
     * Creates a List Tag containing all the block entries.
     *
     * @return The JNBT list tag, named "blocks".
     * */
    public ListTag getTag() {

        ArrayList<Tag> tagList = new ArrayList<>();
        entries.forEach(e -> tagList.add(e.getTag()));

        return new ListTag("blocks", CompoundTag.class, tagList);
    }

    /**
     * Turns the minimum size of this structure into a ListTag containing the dimensions for this structure.
     *
     * @return The JNBT list tag, named "size"
     * */
    public ListTag getSizeTag() {
        ArrayList<Tag> tagList = new ArrayList<>();
        Vector3D size = this.getSize();
        tagList.add(new IntTag("", size.x));
        tagList.add(new IntTag("", size.y));
        tagList.add(new IntTag("", size.z));
        return new ListTag("size", IntTag.class, tagList);
    }
}

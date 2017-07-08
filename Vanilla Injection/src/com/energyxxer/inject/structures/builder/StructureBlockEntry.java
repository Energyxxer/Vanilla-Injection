package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.IntTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.Tag;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing a single block entry in a structure file.
 *
 * @see StructureBlocks
 * @see StructurePaletteEntry
 * @see StructureBuilder
 */
public class StructureBlockEntry {
    /**
     * The (J)NBT data associated with this block.
     * */
    private ArrayList<Tag> nbt = new ArrayList<>();
    /**
     * The position of this block relative to the structure's origin.
     * */
    private Vector3D pos;
    /**
     * This block's palette entry index.
     * */
    private int state;

    /**
     * Creates a structure block entry at the given position, with the given state.
     *
     * @param pos This block's position, relative to the structure's origin.
     * @param state This block's state. It's obtained after adding a palette entry to the StructurePalette.
     *
     * @see StructurePalette#addEntry(StructurePaletteEntry)
     * @see StructurePalette#getState(StructurePaletteEntry)
     * */
    public StructureBlockEntry(Vector3D pos, int state) {
        this.pos = pos;
        this.state = state;
    }

    /**
     * Creates a Compound NBT tag to represent the block entry.
     *
     * @return The JNBT compound tag.
     * */
    public CompoundTag getTag() {
        HashMap<String, Tag> map = new HashMap<>();

        if(nbt.size() > 0) {
            HashMap<String, Tag> nbtMap = new HashMap<>();
            nbt.forEach(t -> nbtMap.put(t.getName(),t));
            map.put("nbt", new CompoundTag("nbt", nbtMap));
        }

        ArrayList<Tag> posList = new ArrayList<>();
        posList.add(new IntTag("", pos.x));
        posList.add(new IntTag("", pos.y));
        posList.add(new IntTag("", pos.z));
        map.put("pos", new ListTag("pos", IntTag.class, posList));

        map.put("state", new IntTag("state", state));

        return new CompoundTag("", map);
    }

    /**
     * Adds a single NBT tag to this block's NBT.
     *
     * @param tag The NBT tag to add.
     *
     * @see Tag
     * */
    public void putNBT(Tag tag) {
        nbt.add(tag);
    }

    /**
     * Returns the list of all NBT tags in this block.
     *
     * @return This block's NBT.
     * */
    public ArrayList<Tag> getNbt() {
        return nbt;
    }

    /**
     * Sets the list of NBT tags to the given list.
     *
     * @param nbt The new list of tags.
     * */
    public void setNbt(ArrayList<Tag> nbt) {
        this.nbt = nbt;
    }

    /**
     * Gets this block's position.
     *
     * @return This block's position.
     * */
    public Vector3D getPos() {
        return pos;
    }

    /**
     * Sets the position of this block.
     *
     * @param pos This block's new position.
     * */
    public void setPos(Vector3D pos) {
        this.pos = pos;
    }

    /**
     * Gets this block's palette state.
     *
     * @return This block's palette state.
     * */
    public int getState() {
        return state;
    }

    /**
     * Sets this block's state.
     *
     * @param state This block's new state.
     * */
    public void setState(int state) {
        this.state = state;
    }
}

package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.DoubleTag;
import com.energyxxer.inject.jnbt.IntTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.Tag;
import com.energyxxer.inject.utils.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing a single block entry in a structure file.
 *
 * @see StructureEntities
 * @see StructureBuilder
 */
public class StructureEntityEntry {
    /**
     * This entity's NBT data.
     * */
    private ArrayList<Tag> nbt = new ArrayList<>();
    /**
     * This entity's position, relative to the structure's origin.
     * */
    private Vector3D.Double pos;

    /**
     * Creates a structure entity entry at the given double-precision position.
     *
     * @param pos This entity's position.
     * */
    public StructureEntityEntry(Vector3D.Double pos) {
        this.pos = pos;
    }

    /**
     * Creates a Compound NBT tag to represent the entity entry.
     *
     * @return The JNBT compound tag.
     * */
    public CompoundTag getTag() {
        HashMap<String, Tag> map = new HashMap<>();

        HashMap<String, Tag> nbtMap = new HashMap<>();
        nbt.forEach(t -> nbtMap.put(t.getName(),t));
        map.put("nbt", new CompoundTag("nbt", nbtMap));

        ArrayList<Tag> blockPosList = new ArrayList<>();
        blockPosList.add(new IntTag("", (int) Math.floor(pos.x)));
        blockPosList.add(new IntTag("", (int) Math.floor(pos.y)));
        blockPosList.add(new IntTag("", (int) Math.floor(pos.z)));
        map.put("blockPos", new ListTag("blockPos", IntTag.class, blockPosList));

        ArrayList<Tag> posList = new ArrayList<>();
        posList.add(new DoubleTag("", pos.x));
        posList.add(new DoubleTag("", pos.y));
        posList.add(new DoubleTag("", pos.z));
        map.put("pos", new ListTag("pos", DoubleTag.class, posList));

        return new CompoundTag("", map);
    }

    /**
     * Adds a single NBT tag to this entity's NBT.
     *
     * @param tag The NBT tag to add.
     *
     * @see Tag
     * */
    public void putNBT(Tag tag) {
        nbt.add(tag);
    }

    /**
     * Returns the list of all NBT tags in this entity.
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
     * Gets this entity's position.
     *
     * @return This entity's position.
     * */
    public Vector3D.Double getPos() {
        return pos;
    }

    /**
     * Sets the position of this entity.
     *
     * @param pos This entity's new position.
     * */
    public void setPos(Vector3D.Double pos) {
        this.pos = pos;
    }
}

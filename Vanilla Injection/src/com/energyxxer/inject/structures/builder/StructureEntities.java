package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.Tag;

import java.util.ArrayList;

/**
 * Class for maintaining all the entities within a structure file.
 *
 * @see StructureEntityEntry
 * @see StructureBuilder
 */
public class StructureEntities {

    /**
     * A list of all entity entries.
     * */
    private ArrayList<StructureEntityEntry> entries = new ArrayList<>();

    /**
     * Creates a StructureEntities object.
     * */
    StructureEntities() {}

    /**
     * Adds a <code>StructureEntityEntry</code> to this entity master.
     *
     * @param newEntry The entry to add.
     *
     * @see StructureEntityEntry
     * */
    public void addEntry(StructureEntityEntry newEntry) {
        entries.add(newEntry);
    }
    /**
     * Adds a <code>StructureEntityEntry</code> to this entity master.
     *
     * @param newEntry The entry to add.
     * @param priority Whether the entry should be added before all others.
     *
     * @see StructureEntityEntry
     * */
    public void addEntry(StructureEntityEntry newEntry, boolean priority) {
        if(priority) entries.add(0, newEntry);
        else entries.add(newEntry);
    }

    /**
     * Creates a List Tag containing all the entity entries.
     *
     * @return The JNBT list tag, named "entities".
     * */
    public ListTag getTag() {

        ArrayList<Tag> tagList = new ArrayList<>();
        entries.forEach(e -> tagList.add(e.getTag()));

        return new ListTag("entities", CompoundTag.class, tagList);
    }
}

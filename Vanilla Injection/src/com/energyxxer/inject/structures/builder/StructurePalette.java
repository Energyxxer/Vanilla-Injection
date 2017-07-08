package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.ListTag;
import com.energyxxer.inject.jnbt.Tag;

import java.util.ArrayList;

/**
 * Class for maintaining the block palette within a structure file.
 *
 * @see StructurePaletteEntry
 * @see StructureBlocks
 * @see StructureBuilder
 */
public class StructurePalette {
    /**
     * A list of all palette entries.
     * */
    private ArrayList<StructurePaletteEntry> entries = new ArrayList<>();

    /**
     * Creates a StructurePalette object.
     * */
    StructurePalette() {}

    /**
     * Adds a palette entry to this palette, if it doesn't already exist.
     *
     * @param newEntry The new entry to register.
     *
     * @return The entry's index or "state". Use this to create a <code>StructureBlockEntry</code>.
     *
     * @see StructureBlockEntry
     * */
    public int addEntry(StructurePaletteEntry newEntry) {
        for(StructurePaletteEntry entry : entries) {
            if(newEntry.equals(entry)) return getState(entry);
        }
        entries.add(newEntry);
        return getState(newEntry);
    }

    /**
     * Gets the given entry's index in this palette.
     *
     * @param entry The entry to get the state for.
     *
     * @return The index of the given entry in this palette, if exists. If it isn't present, returns -1.
     * */
    public int getState(StructurePaletteEntry entry) {
        return entries.indexOf(entry);
    }

    /**
     * Creates a List Tag containing all the palette entries.
     *
     * @return The JNBT list tag, named "palette".
     * */
    public ListTag getTag() {
        ArrayList<Tag> tagList = new ArrayList<>();
        entries.forEach(e -> tagList.add(e.getTag()));

        return new ListTag("palette", CompoundTag.class, tagList);
    }
}

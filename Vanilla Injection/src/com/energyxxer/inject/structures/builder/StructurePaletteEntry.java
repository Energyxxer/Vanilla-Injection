package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.StringTag;
import com.energyxxer.inject.jnbt.Tag;

import java.util.HashMap;

/**
 * Class representing a single block palette entry in a structure file.
 *
 * @see StructurePalette
 * @see StructureBlockEntry
 * @see StructureBuilder
 */
public class StructurePaletteEntry {
    /**
     * The ID of the block. Used in the Name tag in the entry.
     * */
    private String name;
    /**
     * A Map of properties that make up the blockstate.
     * */
    private HashMap<String, String> properties = new HashMap<>();

    /**
     * Creates a StructurePaletteEntry with the given block ID.
     *
     * @param name The block ID / name of the block. Note that it should include the <code>minecraft:</code> namespace.
     * */
    public StructurePaletteEntry(String name) {
        this.name = name;
    }

    /**
     * Puts a blockstate property into the property map.
     *
     * @param key The name of the property.
     * @param value The value of the property.
     * */
    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Gets the block ID of the palette entry.
     *
     * @return The block ID.
     * */
    public String getName() {
        return name;
    }

    /**
     * Sets the block ID of the palette entry.
     *
     * @param name The new block ID. Note that it should include the <code>minecraft:</code> namespace
     * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creates a Compound NBT tag to represent the palette entry.
     *
     * @return The JNBT compound tag.
     * */
    public CompoundTag getTag() {
        HashMap<String, Tag> map = new HashMap<>();
        map.put("Name", new StringTag("Name", this.name));

        if(properties.size() > 0) {
            HashMap<String, Tag> propertiesMap = new HashMap<>();
            for(String key : properties.keySet()) {
                propertiesMap.put(key, new StringTag(key, properties.get(key)));
            }
            CompoundTag propertiesTag = new CompoundTag("Properties", propertiesMap);
            map.put("Properties", propertiesTag);
        }

        return new CompoundTag("", map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StructurePaletteEntry that = (StructurePaletteEntry) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }
}

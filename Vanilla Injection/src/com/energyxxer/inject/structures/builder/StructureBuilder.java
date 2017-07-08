package com.energyxxer.inject.structures.builder;

import com.energyxxer.inject.jnbt.CompoundTag;
import com.energyxxer.inject.jnbt.IntTag;
import com.energyxxer.inject.jnbt.NBTOutputStream;
import com.energyxxer.inject.jnbt.StringTag;
import com.energyxxer.inject.jnbt.Tag;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Utility class for building structures.
 */
public class StructureBuilder {

    /**
     * This builder's palette object. Used to register palette entries.
     * */
    public final StructurePalette palette = new StructurePalette();
    /**
     * This builder's blocks object. Used to register block entries.
     * */
    public final StructureBlocks blocks = new StructureBlocks();
    /**
     * This builder's entities object. Used to register entities.
     * */
    public final StructureEntities entities = new StructureEntities();

    /**
     * This builder's output file path.
     * */
    public final String filePath;

    /**
     * Creates a Structure Builder that outputs to the given file path.
     *
     * @param filePath The path of the file at which to output.
     * */
    public StructureBuilder(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Takes all the given information and creates a structure file at the specified file path.
     *
     * @throws IOException If something goes wrong with the output.
     * */
    public void build() throws IOException {

        FileOutputStream fos = new FileOutputStream(filePath);
        NBTOutputStream nbtOs = new NBTOutputStream(fos);

        HashMap<String, Tag> rootMap = new HashMap<>();
        rootMap.put("blocks", blocks.getTag());
        rootMap.put("entities", entities.getTag());
        rootMap.put("palette", palette.getTag());
        rootMap.put("size", blocks.getSizeTag());
        rootMap.put("author", new StringTag("author", ""));
        rootMap.put("DataVersion", new IntTag("DataVersion",922));

        CompoundTag rootTag = new CompoundTag("", rootMap);

        nbtOs.writeTag(rootTag);

        nbtOs.close();
        fos.close();
    }
}

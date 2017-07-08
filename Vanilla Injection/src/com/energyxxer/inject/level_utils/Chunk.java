package com.energyxxer.inject.level_utils;

import com.energyxxer.inject.level_utils.block.Block;
import com.energyxxer.inject.nbt.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing a single chunk of a world.
 *
 * @see <a href="http://minecraft.gamepedia.com/Chunk_format">Chunk format in the Minecraft Wiki</a>
 */
public class Chunk {
    private static final int MIN_HEIGHT = 0;
    private static final int MAX_HEIGHT = 255;
    /**
     * X position of the region the chunk is contained in.
     * */
    public final int regionX;
    /**
     * Z position of the region the chunk is contained in.
     * */
    public final int regionZ;

    /**
     * X position of the chunk.
     * */
    public final int xPos;
    /**
     * Z position of the chunk.
     * */
    public final int zPos;
    /**
     * Tick when the chunk was last saved.
     * */
    public final long lastUpdate;

    /**
     * This chunk's <code>BiomeMap</code> object. Contains information about the chunk's biomes.
     * */
    public final BiomeMap biomeMap;
    /**
     * This chunk's <code>TerrainData</code> object. Contains information about the blocks contained in the chunk.
     * */
    public final TerrainData terrainData;

    /**
     * The cumulative number of ticks players have been in the chunk.
     * */
    public final long inhabitedTime;

    /**
     * Each TAG_Compound in this list defines a block entity in the chunk.
     * */
    public final List<Tag.Compound> tileEntities = new ArrayList<>();
    /**
     * Each TAG_Compound in this list defines an entity in the chunk.
     * */
    public final List<Tag.Compound> entities = new ArrayList<>();

    /**
     * Version of the chunk's NBT structure.
     * */
    public final int dataVersion;

    /**
     * The chunk's entire TAG_Compound. Use to get data not abstracted by the chunk's constructor.
     * */
    public final Tag.Compound raw;

    /**
     * Takes a Compound tag, reads some of its most relevant data and saves it in the object.
     * Also takes the position of the region this chunk is in for later optimization by the level reader.
     *
     * @param raw The compound tag representing the chunk.
     * @param regionX The X position of the region the chunk is in.
     * @param regionZ The Z position of the region the chunk is in.
     * */
    Chunk(Tag.Compound raw, int regionX, int regionZ) {
        this.raw = raw;
        this.regionX = regionX;
        this.regionZ = regionZ;

        int xPos = 0;
        int zPos = 0;
        long lastUpdate = -1;
        BiomeMap biomeMap = null;
        TerrainData terrainData = null;
        long inhabitedTime = -1;
        int dataVersion = -1;

        for(Tag t0 : raw) {
            if(TagUtils.match(t0, "DataVersion", Tag.Int.class)) {
                dataVersion = ((Tag.Int) t0).v;
            } else if(TagUtils.match(t0, "Level", Tag.Compound.class)) {
                //Start abstracting chunk data;

                for(Tag t : (Tag.Compound) t0) {
                    if(TagUtils.match(t, "xPos", Tag.Int.class)) xPos = ((Tag.Int) t).v;
                    else if(TagUtils.match(t, "zPos", Tag.Int.class)) zPos = ((Tag.Int) t).v;
                    else if(TagUtils.match(t, "LastUpdate", Tag.Long.class)) lastUpdate = ((Tag.Long) t).v;
                    else if(TagUtils.match(t, "Biomes", Tag.ByteArray.class)) biomeMap = new BiomeMap((Tag.ByteArray) t);
                    else if(TagUtils.match(t, "Sections", Tag.List.class)) terrainData = new TerrainData((Tag.List) t);
                    else if(TagUtils.match(t, "InhabitedTime", Tag.Long.class)) inhabitedTime = ((Tag.Long) t).v;
                    else if(TagUtils.match(t, "TileEntities", Tag.List.class)) {
                        for(Tag te : (Tag.List) t) {
                            tileEntities.add((Tag.Compound) te);
                        }
                    }
                    else if(TagUtils.match(t, "Entities", Tag.List.class)) {
                        for(Tag te : (Tag.List) t) {
                            entities.add((Tag.Compound) te);
                        }
                    }
                }
            }
        }

        this.xPos = xPos;
        this.zPos = zPos;
        this.lastUpdate = lastUpdate;
        this.biomeMap = biomeMap;
        this.terrainData = terrainData;
        this.inhabitedTime = inhabitedTime;
        this.dataVersion = dataVersion;
    }

    /**
     * Creates an empty chunk object in the given region. Used for when the chunk to be
     * read has not yet been saved into disk.
     *
     * @param regionX The X position of the region the chunk is in.
     * @param regionZ The Z position of the region the chunk is in.
     * */
    Chunk(int regionX, int regionZ) {
        this.raw = null;
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.xPos = regionX << 5;
        this.zPos = regionZ << 5;
        this.lastUpdate = 0;
        this.biomeMap = new BiomeMap(new Tag.ByteArray("",new byte[16*16]));
        this.terrainData = new TerrainData();
        this.inhabitedTime = 0;
        this.dataVersion = -1;
    }

    @Override
    public String toString() {
        return String.format("Chunk[%d, %d]", xPos, zPos);
    }

    /**
     * Class tasked to read information about a chunk's block map.
     * */
    public static class TerrainData {

        /**
         * A list of all cubic sections of the chunk.
         * */
        Section[] sections = new Section[16];

        /**
         * Whether the chunk hasn't been saved to disk.
         * */
        private final boolean missing;

        /**
         * Creates a <code>TerrainData</code> object from the given TAG_List.
         *
         * @param raw The <code>Sections</code> TAG_List in the chunk's root tag.
         * */
        TerrainData(Tag.List raw) {
            this.missing = false;
            for(Tag t : raw) {
                Section sect = new Section((Tag.Compound) t);
                sections[sect.yIndex] = sect;
            }
        }

        /**
         * Creates an empty/corrupted/missing <code>TerrainData</code> object.
         * */
        TerrainData() {
            this.missing = true;
        }

        /**
         * Gets the block at the given position.
         * Note that the coordinates are relative to the chunk, not to the world.
         *
         * If the chunk is missing, it will return air.
         *
         * @param x The x coordinate in the chunk's coordinate space.
         * @param y The y coordinate in the chunk's coordinate space.
         * @param z The z coordinate in the chunk's coordinate space.
         * */
        public Block getBlockAt(int x, int y, int z) {
            if(missing) return Block.get(0,0);
            Section sect = sections[y/16];
            if(sect == null) return Block.get(0,0);
            if(y < Chunk.MIN_HEIGHT || y > Chunk.MAX_HEIGHT) return Block.get(0,0);
            Block block = sect.getBlockAt(x, y % 16, z);
            return (block != null) ? block : Block.get(0,0);
        }

        @Override
        public String toString() {
            return "TerrainData{" +
                    "sections=" + Arrays.toString(sections) +
                    '}';
        }

        /**
         * Class representing a cubic section of a chunk's terrain data.
         * */
        private static class Section {
            /**
             * The y index of the section. Not to be confused with the y coordinate.
             * Equal to the y coordinate mod 16.
             * */
            int yIndex;
            /**
             * The y coordinate of this section's bottom block.
             * Equal to the y index * 16.
             * */
            int yPos;

            /**
             * The TAG_Compound that makes up this section.
             * */
            Tag.Compound raw;

            /**
             * Whether this section's blocks have been loaded into the object.
             * */
            boolean populated = false;
            /**
             * Three-dimensional array containing all the blocks in the section.
             * */
            Block[][][] blocks = new Block[16][16][16];

            /**
             * Creates a <code>Section</code> from the given compound TAG_Compound.
             *
             * @param raw A TAG_Compound within the <code>Sections</code> tag.
             * */
            Section(Tag.Compound raw) {
                this.raw = raw;
                for(Tag t : raw) {
                    if (TagUtils.match(t, "Y", Tag.Byte.class)) this.yIndex = ((Tag.Byte) t).v;
                }
                this.yPos = yIndex * 16;
            }

            /**
             * Loads all this section's blocks into memory once needed.
             * */
            private void populateBlocks() {
                if(populated) return;
                populated = true;
                byte[] blocks = null;
                byte[] add = null;
                byte[] data = null;
                for(Tag t : raw) {
                    if(TagUtils.match(t, "Blocks", Tag.ByteArray.class)) blocks = ((Tag.ByteArray) t).v;
                    else if(TagUtils.match(t, "Add", Tag.ByteArray.class)) add = ((Tag.ByteArray) t).v;
                    else if(TagUtils.match(t, "Data", Tag.ByteArray.class)) data = ((Tag.ByteArray) t).v;
                }

                if(blocks == null) return;

                int blockIndex = 0;
                for(int y = 0; y < 16; y++) {
                    for(int z = 0; z < 16; z++) {
                        for(int x = 0; x < 16; x++) {
                            int blockId = blocks[blockIndex] & 255;
                            if(add != null) blockId += add[blockIndex] << 8;
                            int blockData = 0;
                            if(data != null) {
                                if((x & 1) == 0) blockData = (data[blockIndex/2] & 255) & 15;
                                else blockData = ((data[blockIndex/2] & 255) & 240) >> 4;
                            }
                            this.blocks[x][y][z] = Block.get(blockId, blockData);
                            blockIndex++;
                        }
                    }
                }
            }

            /**
             * Gets the block at the given coordinate. Note that the coordinates are relative to the section's origin,
             * as in, the coordinates in all three axes must be between 0 and 15, inclusive.
             *
             * @param x The x coordinate in the section's coordinate space.
             * @param y The y coordinate in the section's coordinate space.
             * @param z The z coordinate in the section's coordinate space.
             *
             * @return The block object at the given position.
             * */
            Block getBlockAt(int x, int y, int z) {
                populateBlocks();
                return blocks[x][y][z];
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder("@Y:" + yIndex + ": ");
                for(int x = 0; x < 16; x++) {
                    for(int y = 0; y < 16; y++) {
                        for(int z = 0; z < 16; z++) {
                            sb.append("\n    (");
                            sb.append(x);
                            sb.append(',');
                            sb.append(y);
                            sb.append(',');
                            sb.append(z);
                            sb.append("): ");
                            sb.append(blocks[x][y][z]);
                        }
                    }
                }
                return sb.toString();
            }
        }
    }

    /**
     * Class tasked to read information about a chunk's biomes in each column.
     * */
    public static class BiomeMap {
        /**
         * A flat array containing the biomes in this chunk in ZX order.
         * */
        private Chunk.Biome[] flatMap = new Chunk.Biome[16*16];

        /**
         * The TAG_Byte_Array containing the raw biome info.
         * */
        private Tag.ByteArray raw;

        /**
         * Whether the chunk'ss biomes have been loaded into the object.
         * */
        private boolean populated = false;

        /**
         * Creates a <code>BiomeMap</code> from the given TAG_Byte_Array.
         *
         * @param raw The <code>Biomes</code> TAG_Byte_Array within the chunk's root tag.
         * */
        BiomeMap(Tag.ByteArray raw) {
            this.raw = raw;
        }

        /**
         * Loads all this chunk's biomes into memory.
         * */
        private void populateMap() {
            if(populated) return;
            populated = true;
            byte[] allBytes = raw.v;

            int i = 0;
            for(byte id : allBytes) {
                Biome biome = Biome.getBiomeById(id & 255);
                flatMap[i] = biome;
                i++;
            }
        }

        /**
         * Gets the biome at the given column. Note that the coordinates are relative to the chunk's origin,
         * as in, the coordinates in both axes must be between 0 and 15, inclusive.
         *
         * @param x The x coordinate in the chunk's coordinate space.
         * @param z The z coordinate in the chunk's coordinate space.
         *
         * @return The biome object at the given position.
         * */
        public Biome getBiomeForColumn(int x, int z) {
            populateMap();
            return flatMap[z*16 + x];
        }

        @Override
        public String toString() {
            return Arrays.toString(flatMap);
        }
    }

    /**
     * Enum containing all biomes as of Minecraft 17w06a
     * */
    public enum Biome {
        OCEAN,
        PLAINS,
        DESERT,
        EXTREME_HILLS,
        FOREST,
        TAIGA,
        SWAMPLAND,
        RIVER,
        HELL,
        THE_END,
        FROZEN_OCEAN,
        FROZEN_RIVER,
        ICE_PLAINS,
        ICE_MOUNTAINS,
        MUSHROOM_ISLAND,
        MUSHROOM_ISLAND_SHORE,
        BEACH,
        DESERT_HILLS,
        FOREST_HILLS,
        TAIGA_HILLS,
        EXTREME_HILLS_EDGE,
        JUNGLE,
        JUNGLE_HILLS,
        JUNGLE_EDGE,
        DEEP_OCEAN,
        STONE_BEACH,
        COLD_BEACH,
        BIRCH_FOREST,
        BIRCH_FOREST_HILLS,
        ROOFED_FOREST,
        COLD_TAIGA,
        COLD_TAIGA_HILLS,
        MEGA_TAIGA,
        MEGA_TAIGA_HILLS,
        EXTREME_HILLS_PLUS,
        SAVANNA,
        SAVANNA_PLATEAU,
        MESA,
        MESA_PLATEAU_F,
        MESA_PLATEAU,
        THE_VOID(127),
        PLAINS_M(128),
        SUNFLOWER_PLAINS(129),
        DESERT_M(130),
        EXTREME_HILLS_M(131),
        FLOWER_FOREST(132),
        TAIGA_M(133),
        SWAMPLAND_M(134),
        ICE_PLAINS_SPIKES(140),
        JUNGLE_M(149),
        JUNGLE_EDGE_M(151),
        BIRCH_FOREST_M(155),
        BIRCH_FOREST_HILLS_M(156),
        ROOFED_FOREST_M(157),
        COLD_TAIGA_M(158),
        MEGA_SPRUCE_TAIGA(160),
        REDWOOD_TAIGA_HILLS_M(161),
        EXTREME_HILLS_PLUS_M(162),
        SAVANNA_M(163),
        SAVANNA_PLATEAU_M(164),
        MESA_BRYCE(165),
        MESA_PLATEAU_F_M(166),
        MESA_PLATEAU_M(167);

        /**
         * The numerical ID of the biome as stored in memory.
         * */
        public final int id;
        /**
         * The biome's human-readable name. Obtained from the enum value's name.
         * */
        public final String name;

        /**
         * Creates a biome.
         * Numerical id defaults to the enum value's ordinal.
         * Human-readable name is taken from the enum value's name.
         * */
        Biome() {
            this.id = this.ordinal();
            this.name = getDisplayName(this.name());
        }

        /**
         * Creates a biome with the given numerical ID.
         * Human-readable name is taken from the enum value's name.
         *
         * @param id The numerical ID for the biome.
         * */
        Biome(int id) {
            this.id = id;
            this.name = getDisplayName(this.name());
        }

        /**
         * Turns an enum value name into a human-readable display name.
         * Additionally, turns all "_PLUS" into "+".
         *
         * @param enumValueName The name of the enum value.
         * */
        private static String getDisplayName(String enumValueName) {
            StringBuilder sb = new StringBuilder();
            boolean capitalize = true;
            for (byte b : enumValueName.getBytes()) {
                char c = (char) b;

                if(c == '_') {
                    sb.append(' ');
                    capitalize = true;
                    continue;
                }
                sb.append((capitalize) ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalize = false;
            }
            return sb.toString().replace(" Plus","+");
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * Gets the biome by the numerical biome ID.
         *
         * @param id The numerical biome ID to get the object for.
         *
         * @return The biome object representing this numerical biome ID. <code>null</code> if it doesn't exist.
         * */
        public static Biome getBiomeById(int id) {
            Biome[] values = values();

            if(id >= 0 && id < values.length) {
                Biome guess = values[id];
                if(guess.id == id) return guess;
            }

            for(Biome b : values) {
                if(b.id == id) return b;
            }
            return null;
        }
    }
}
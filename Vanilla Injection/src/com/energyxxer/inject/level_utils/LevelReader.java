package com.energyxxer.inject.level_utils;

import com.energyxxer.inject.InjectionMaster;
import com.energyxxer.inject.level_utils.block.Block;
import com.energyxxer.inject.nbt.Tag;
import com.energyxxer.inject.utils.Vector3D;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.InflaterInputStream;

/**
 * Class for reading a level's chunks.
 */
public class LevelReader {
    /**
     * The <code>InjectionMaster</code> associated with this level reader.
     * */
    private final InjectionMaster master;

    /**
     * Creates a <code>LevelReader</code> associated to the given master.
     *
     * @param master The master to attach this <code>LevelReader</code> to.
     * */
    public LevelReader(InjectionMaster master) {
        this.master = master;
    }

    /**
     * Map containing all the chunks previously read, per dimension.
     * */
    private HashMap<Integer, ArrayList<Chunk>> chunkMemory = new HashMap<>();

    /**
     * Reads a chunk at the position specified in the overworld.
     *
     * Note that <code>chunkX</code> and <code>chunkZ</code> are not world coordinates: instead, they're the chunk
     * coordinates. In Java, transforming world coordinates to chunk coordinates would be:
     * <br>
     * <pre>
     * int chunkX = (int) Math.floor(x / 16f);
     * int chunkZ = (int) Math.floor(z / 16f);</pre>
     *
     * @param chunkX The chunk's X position.
     * @param chunkZ The chunk's Z position.
     *
     * @return The chunk object at the given position in the overworld.
     * */
    public Chunk readChunk(int chunkX, int chunkZ) {
        return this.readChunk(chunkX, chunkZ, 0);
    }

    /**
     * Reads a chunk at the position specified in the given dimension.
     *
     * Note that <code>chunkX</code> and <code>chunkZ</code> are not world coordinates: instead, they're the chunk
     * coordinates. In Java, transforming world coordinates to chunk coordinates would be:
     * <br>
     * <pre>
     * int chunkX = (int) Math.floor(x / 16f);
     * int chunkZ = (int) Math.floor(z / 16f);</pre>
     *
     * @param chunkX The chunk's X position.
     * @param chunkZ The chunk's Z position.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The chunk object at the position in the specified dimension.
     * */
    public Chunk readChunk(int chunkX, int chunkZ, int dim) {
        //Check in memory
        if(chunkMemory.containsKey(dim)) {
            ArrayList<Chunk> chunksForDim = chunkMemory.get(dim);
            for(Chunk c : chunksForDim) {
                if(c.xPos == chunkX && c.zPos == chunkZ) {
                    return c;
                }
            }
        }

        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;

        int inRegionX = chunkX % 32;
        int inRegionZ = chunkZ % 32;
        if(inRegionX < 0) inRegionX += 32;
        if(inRegionZ < 0) inRegionZ += 32;

        File rf = new File(this.getPathForDimension(dim)+File.separator+"r."+regionX+'.'+regionZ+".mca");

        master.scheduleChunkRefresh();

        try(RandomAccessFile region = new RandomAccessFile(rf,"r")) {
            long seek = ((inRegionX%32) + (inRegionZ%32)*32);
            region.seek(seek*4);

            //Location Table
            final byte[] locationEntry = new byte[4];
            int offset;
            region.readFully(locationEntry);
            try(DataInputStream dis = new DataInputStream(new ByteArrayInputStream(new byte[]{0, locationEntry[0], locationEntry[1], locationEntry[2]}))) {
                offset = dis.readInt()*4096;
            }
            int size = locationEntry[3]*4096;

            //Back in getChunk
            if(offset > 0 && size > 0) {
                region.seek(offset);

                int length = region.readInt();
                byte compression = region.readByte();

                byte[] chunkBytes = new byte[length-1];
                region.readFully(chunkBytes);

                try(InputStream is = new InflaterInputStream(new ByteArrayInputStream(chunkBytes))) {
                    Chunk chunk = new Chunk((Tag.Compound) Tag.deserialize(is), regionX, regionZ);

                    if(chunkMemory.containsKey(dim)) {
                        ArrayList<Chunk> chunksForDim = chunkMemory.get(dim);
                        boolean refresh = false;
                        for(Chunk c : chunksForDim) {
                            if(c.xPos == chunkX && c.zPos == chunkZ) {
                                if(chunk.lastUpdate == c.lastUpdate) {
                                    return c;
                                } else {
                                    refresh = true;
                                    break;
                                }
                            }
                        }
                        if(refresh) chunksForDim.removeIf(c1 -> c1.regionX == regionX && c1.regionZ == regionZ);
                    }

                    chunkMemory.putIfAbsent(dim,new ArrayList<>());
                    chunkMemory.get(dim).add(chunk);
                    return chunk;
                }
            }


        } catch(IOException x) {
            x.printStackTrace();
        }
        return new Chunk(regionX, regionZ);
    }

    /**
     * Gets the block at the specified position in the overworld.
     * Note that this loads the chunk into memory.
     *
     * @param pos The point in a world coordinate space.
     *
     * @return The block object at the specified position in the overworld.
     * */
    public Block getBlockAtPos(Vector3D pos) {
        return getBlockAtPos(pos.x, pos.y, pos.z);
    }

    /**
     * Gets the block at the position in the specified dimension.
     * Note that this loads the chunk into memory.
     *
     * @param pos The point in a world coordinate space.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The block object at the position in the dimension specified.
     * */
    public Block getBlockAtPos(Vector3D pos, int dim) {
        return getBlockAtPos(pos.x, pos.y, pos.z, dim);
    }

    /**
     * Gets the block at the specified position in the overworld.
     * Note that this loads the chunk into memory.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     *
     * @return The block object at the specified position in the overworld.
     * */
    public Block getBlockAtPos(int x, int y, int z) {
        return getBlockAtPos(x, y, z, 0);
    }

    /**
     * Gets the block at the position in the specified dimension.
     * Note that this loads the chunk into memory.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The block object at the position in the dimension specified.
     * */
    public Block getBlockAtPos(int x, int y, int z, int dim) {
        Chunk chunk = getChunkAtPos(x, z, dim);

        Vector3D pos = getInChunkPos(x, y, z);

        return chunk.terrainData.getBlockAt(pos.x, pos.y, pos.z);
    }

    /**
     * Gets the biome at the position in the overworld.
     * Note that this loads the chunk into memory.
     *
     * @param pos The point in a world coordinate space.
     *
     * @return The biome at the position in the overworld.
     * */
    public Chunk.Biome getBiomeAtPos(Vector3D pos) {
        return getBiomeAtPos(pos.x, pos.z);
    }

    /**
     * Gets the biome at the position in the specified dimension.
     * Note that this loads the chunk into memory.
     *
     * @param pos The point in a world coordinate space.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The biome at the position in the dimension specified.
     * */
    public Chunk.Biome getBiomeAtPos(Vector3D pos, int dim) {
        return getBiomeAtPos(pos.x, pos.z, dim);
    }

    /**
     * Gets the biome at the position in the overworld.
     * Note that this loads the chunk into memory.
     *
     * @param x The x coordinate.
     * @param z The z coordinate.
     *
     * @return The biome at the position in the overworld.
     * */

    public Chunk.Biome getBiomeAtPos(int x, int z) {
        return getBiomeAtPos(x, z, 0);
    }

    /**
     * Gets the biome at the position in the specified dimension.
     * Note that this loads the chunk into memory.
     *
     * @param x The x coordinate.
     * @param z The z coordinate.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The biome at the position in the dimension specified.
     * */
    public Chunk.Biome getBiomeAtPos(int x, int z, int dim) {
        Chunk chunk = getChunkAtPos(x, z, dim);

        Vector3D pos = getInChunkPos(x, 0, z);

        return chunk.biomeMap.getBiomeForColumn(pos.x, pos.z);
    }

    /**
     * Gets the chunk at the specified world position in the overworld.
     *
     * @param pos The point in a world coordinate space.
     *
     * @return The chunk at the position specified in the overworld.
     * */
    public Chunk getChunkAtPos(Vector3D pos) {
        return getChunkAtPos(pos.x, pos.z);
    }

    /**
     * Gets the chunk at the world position in the dimension specified.
     *
     * @param pos The point in a world coordinate space.
     *
     * @return The chunk at the position in the dimension specified.
     * */
    public Chunk getChunkAtPos(Vector3D pos, int dim) {
        return getChunkAtPos(pos.x, pos.z, dim);
    }

    /**
     * Gets the chunk at the specified world position in the overworld.
     *
     * @param x The x coordinate.
     * @param z The z coordinate.
     *
     * @return The chunk at the position specified in the overworld.
     * */
    public Chunk getChunkAtPos(int x, int z) {
        return getChunkAtPos(x, z, 0);
    }

    /**
     * Gets the chunk at the world position in the dimension specified.
     *
     * @param x The x coordinate.
     * @param z The z coordinate.
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The chunk at the position in the dimension specified.
     * */
    public Chunk getChunkAtPos(int x, int z, int dim) {
        int chunkX = (int) Math.floor(x / 16f);
        int chunkZ = (int) Math.floor(z / 16f);

        return master.reader.readChunk(chunkX, chunkZ, dim);
    }

    /**
     * Returns the folder name for the given dimension number.
     *
     * @param dim The dimension number (Nether -1, Overworld 0, End 1).
     *
     * @return The name of the folder containing region information about the given dimension.
     * */
    private String getPathForDimension(int dim) {
        String folder;
        switch(dim) {
            case -1: folder = "DIM-1"; break;
            case 0: folder = "region"; break;
            case 1: folder = "DIM1"; break;
            default: throw new IllegalArgumentException("Invalid dimension index " + dim);
        }
        return master.getWorldDirectory().getAbsolutePath() + File.separator + folder;
    }

    /**
     * Converts coordinates in a world coordinate space to a chunk coordinate space.
     *
     * @param pos The point in a world coordinate space.
     *
     * @return A point containing the position specified in a chunk coordinate space.
     * */
    public static Vector3D getInChunkPos(Vector3D pos) {
        return getInChunkPos(pos.x, pos.y, pos.z);
    }

    /**
     * Converts coordinates in a world coordinate space to a chunk coordinate space.
     *
     * @param x The world x coordinate.
     * @param y The world y coordinate.
     * @param z The world z coordinate.
     *
     * @return A point containing the position specified in a chunk coordinate space.
     * */
    public static Vector3D getInChunkPos(int x, int y, int z) {
        double inChunkX = x % 16;
        double inChunkZ = z % 16;

        if(inChunkX < 0) inChunkX = Math.ceil(inChunkX + 16);
        if(inChunkZ < 0) inChunkZ = Math.ceil(inChunkZ + 16);

        return new Vector3D((int) inChunkX, y, (int) inChunkZ);
    }

    /**
     * Clears this level reader's chunk memory for chunks to be read from file again.
     * */
    public void clearChunkMemory() {
        chunkMemory.clear();
    }
}

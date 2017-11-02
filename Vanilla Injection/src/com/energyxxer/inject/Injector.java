package com.energyxxer.inject;

import static com.energyxxer.inject.structures.StructureBlock.Mode.LOAD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import com.energyxxer.inject.listeners.SuccessEvent;
import com.energyxxer.inject.listeners.SuccessListener;
import com.energyxxer.inject.structures.CommandBlock;
import com.energyxxer.inject.structures.CommandBlockMinecart;
import com.energyxxer.inject.structures.StructureBlock;
import com.energyxxer.inject.structures.layout.ChainLayoutManager;
import com.energyxxer.inject.structures.layout.YZXChainLayoutManager;
import com.energyxxer.inject.utils.Vector3D;
import com.evilco.mc.nbt.stream.NbtOutputStream;
import com.evilco.mc.nbt.tag.TagCompound;

import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * Class containing functions related to converting lists of commands
 * to structure files injected into an InjectionMaster's world.
 */
public class Injector {
    /**
     * The InjectionMaster associated with this Injector.
     * */
    private InjectionMaster master;

    /**
     * The ID of the next structure file to create.
     * */
    private int structureID = 0;

    /**
     * List containing all the commands to be run once injected.
     * */
    private ArrayList<CommandBlock> commandBuffer = new ArrayList<>();
    /**
     * List containing all the commands to be run every tick before the next injection.
     * */
    private ArrayList<CommandBlock> repeatingCommandBuffer = new ArrayList<>();

    /**
     * List containing all the commands to run and enable output for once injected.
     * */
    private ArrayList<AbstractCommand> fetchCommandBuffer = new ArrayList<>();

    /**
     * Path to the injection folder. Usually /structures/injection/, relative to the master's world directory.
     * */
    private final String injectFolderPath;
    /**
     * Path to the injection data file for this injector. Stores information such as the ID for the next structure
     * to be able to re-run the injector on a previously injected world in case the injector program is closed.
     * */
    private final File injectDataFile;

    /**
     * ID of the last structure file loaded by the game. Usually a multiple of ten, as every ten structures, the
     * injector will insert a command to make sure the in-game injector is still running. -1 if the injector has
     * just been created.
     * */
    private int lastLoadedId = -1;

    /**
     * Number of times a structure file has been created while this injector has been running.
     * */
    private int filesCreated = 0;

    /**
     * ID of the next fetch command to use an unnamed SuccessListener.
     *
     * @see Injector#insertFetchCommand(String, SuccessListener)
     * */
    private int nextSuccessListenerId = 0;

    /**
     * The maximum size for the impulse command section of the structure files created by this injector.
     * */
    private Vector3D impulseSize = new Vector3D(8,5,5);

    /**
     * The maximum size for the repeating command section of the structure files created by this injector.
     * */
    private Vector3D repeatingSize = new Vector3D(2,5,5);

    /**
     * The preferred layout manager for this injector.
     * */
    private ChainLayoutManager layoutManager = new YZXChainLayoutManager();

    /**
     * Creates an injector for the given master.
     *
     * @param master The InjectionMaster associated with this injector.
     * */
    Injector(InjectionMaster master) {
        this.master = master;

        injectFolderPath = master.getWorldDirectory().getAbsolutePath() + File.separator + "structures" + File.separator + "inject" + File.separator;
        String injectDataPath = injectFolderPath + master.prefix + "_data.txt";
        injectDataFile = new File(injectDataPath);

        File injectFolderFile = new File(injectFolderPath);
        if(!injectFolderFile.exists()) {
            boolean success = injectFolderFile.mkdirs();
            if(!success) System.out.println("[WARNING] " + injectFolderPath + " couldn't be created.");
        }

        try {
            if(!injectDataFile.exists()) {
                boolean success = injectDataFile.createNewFile();
                if(!success) System.out.println("[WARNING] " + injectDataPath + " couldn't be created.");
            } else {
                byte[] encoded = Files.readAllBytes(injectDataFile.toPath());
                String contents = new String(encoded, "UTF-8");
                String[] lines = contents.split("\n");
                for(String line : lines) {
                    if(line.startsWith("l")) {
                        String raw = line.substring(1);
                        if(raw.endsWith("\r")) raw = raw.substring(0, raw.length()-1);
                        this.structureID = Integer.parseInt(raw);
                    }
                }
            }
        } catch(IOException x) {
            x.printStackTrace();
        }

        master.addLogListener(l -> {
            String confirmation = l.getReturnValueFor("$" + master.prefix,true);

            if(confirmation == null) return;
            String checkpoint = confirmation.substring(0,confirmation.indexOf(":"));

            lastLoadedId = Integer.parseInt(checkpoint);

            if(master.paused && lastLoadedId >= structureID-1) master.resume();

            int i = lastLoadedId-5;
            File f;
            while((f = new File(injectFolderPath + master.prefix + i-- + ".nbt")).exists()) {
                if(master.isVerbose()) System.out.println(InjectionMaster.TIME_FORMAT.format(new Date()) + " [Injector] Deleting structure '" + f.getName() + "'.");
                boolean success = f.delete();
                if(!success) System.out.println("[WARNING] " + f + " couldn't be deleted.");
            }
        });
    }

    /**
     * Queues the given command to be run once.
     *
     * @param command The command to run.
     * */
    public void insertImpulseCommand(String command) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((commandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.IMPULSE), command);
            commandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be run once. The command block spawned will have the given name, and
     * its TrackOutput enabled.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block.
     * */
    public void insertImpulseCommand(String command, String name) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((commandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.IMPULSE), command);
            cb.setTrackOutput(true);
            cb.setName(name);
            commandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be continuously run.
     *
     * @param command The command to run.
     * */
    public void insertRepeatingCommand(String command) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((repeatingCommandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.REPEATING), command);
            repeatingCommandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be continuously run. The command block spawned will have the given name, and
     * its TrackOutput enabled.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block.
     * */
    public void insertRepeatingCommand(String command, String name) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((repeatingCommandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.REPEATING), command);
            cb.setTrackOutput(true);
            cb.setName(name);
            repeatingCommandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be run once, set to conditional.
     *
     * @param command The command to run.
     * */
    public void insertConditionalImpulseCommand(String command) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((commandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.IMPULSE), command);
            cb.setConditional(true);
            commandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be run once, set to conditional. The command block spawned will have the given name, and
     * its TrackOutput enabled.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block.
     * */
    public void insertConditionalImpulseCommand(String command, String name) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((commandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.IMPULSE), command);
            cb.setTrackOutput(true);
            cb.setName(name);
            cb.setConditional(true);
            commandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be continuously run, set to conditional.
     *
     * @param command The command to run.
     * */
    public void insertConditionalRepeatingCommand(String command) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((repeatingCommandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.REPEATING), command);
            cb.setConditional(true);
            repeatingCommandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be continuously run, set to conditional. The command block spawned will have the given name, and
     * its TrackOutput enabled.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block.
     * */
    public void insertConditionalRepeatingCommand(String command, String name) {
        if(!master.paused) {
            CommandBlock cb = new CommandBlock(((repeatingCommandBuffer.size() > 0) ? CommandBlock.Type.CHAIN : CommandBlock.Type.REPEATING), command);
            cb.setTrackOutput(true);
            cb.setName(name);
            cb.setConditional(true);
            repeatingCommandBuffer.add(cb);
        }
    }

    /**
     * Queues the given command to be run once. *All* its outputs will be printed into the log, allowing to see
     * all the outputs for commands that run for multiple entities, such as /tp or /execute.
     *
     * @param command The command to run.
     * */
    public void insertFetchCommand(String command) {
        if(!master.paused) fetchCommandBuffer.add(new AbstractCommand(command));
    }

    /**
     * Queues the given command to be run once. *All* its outputs will be printed into the log, allowing to see
     * all the outputs for commands that run for multiple entities, such as /tp or /execute.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block minecart.
     * */
    public void insertFetchCommand(String command, String name) {
        if(!master.paused) fetchCommandBuffer.add(new AbstractCommand(command, name));
    }

    /**
     * Queues the given command to be run once. *All* its outputs will be printed into the log, allowing to see
     * all the outputs for commands that run for multiple entities, such as /tp or /execute.
     *
     * @param command The command to run.
     * @param listener A success listener to be invoked when the command has printed its output to the log.
     * */
    public void insertFetchCommand(String command, SuccessListener listener) {
        if(!master.paused) {
            fetchCommandBuffer.add(new AbstractCommand(command, "$fetchCommand" + nextSuccessListenerId));
            master.addSuccessListener("$fetchCommand" + nextSuccessListenerId++, new SuccessListener() {
                @Override
                public void onSuccess(SuccessEvent e) {
                    listener.onSuccess(e);
                }

                @Override
                public boolean doOnce() {
                    return true;
                }
            });
        }
    }

    /**
     * Queues the given command to be run once. *All* its outputs will be printed into the log, allowing to see
     * all the outputs for commands that run for multiple entities, such as /tp or /execute.
     *
     * @param command The command to run.
     * @param name The CustomName for this command block minecart.
     * @param listener A success listener to be invoked when the command has printed its output to the log.
     * */
    public void insertFetchCommand(String command, String name, SuccessListener listener) {
        if(!master.paused) {
            fetchCommandBuffer.add(new AbstractCommand(command, name));
            master.addSuccessListener(name, new SuccessListener() {
                @Override
                public void onSuccess(SuccessEvent e) {
                    listener.onSuccess(e);
                }

                @Override
                public boolean doOnce() {
                    return true;
                }
            });
        }
    }

    /**
     * Creates a structure file containing all the buffered commands, if any.
     * */
    void flush() {
        if(commandBuffer.size() == 0 && repeatingCommandBuffer.size() == 0 && fetchCommandBuffer.size() == 0) return;

        Structure structure = new Structure(922, "Vanilla-Injection");
        structure.setBackground(new SimpleBlockState("minecraft:air"));
        structure.addBlock(new StructureBlock(new Coordinate3I(), LOAD, "inject/" + master.prefix + (structureID + 1)));

        if(structureID % 10 == 0) {
            insertFetchCommand("gamerule logAdminCommands true","$" + master.prefix + structureID);
            if(lastLoadedId < structureID - 10 && (lastLoadedId > -1 || filesCreated > 10)) {
                master.pause();
            }
        }

        Vector3D startPoint = new Vector3D(2,0,0);

        startPoint = layoutManager.arrange(commandBuffer, new Vector3D(startPoint), new Vector3D(this.impulseSize));
        layoutManager.arrange(repeatingCommandBuffer, new Vector3D(startPoint), new Vector3D(this.repeatingSize));

        structure.addBlocks(repeatingCommandBuffer);
        structure.addBlocks(commandBuffer);

        structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0,0,2)));
        SimpleBlock activatorRail = new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0,1,2));
        activatorRail.putProperty("powered","true");
        activatorRail.putProperty("shape","north_south");
        structure.addBlock(activatorRail);

        if(fetchCommandBuffer.size() > 0) {
            fetchCommandBuffer.add(0,new AbstractCommand("gamerule logAdminCommands true"));
            fetchCommandBuffer.add(new AbstractCommand("gamerule logAdminCommands false"));
            fetchCommandBuffer.add(new AbstractCommand("kill @e[type=commandblock_minecart,r=1,tag=injector_fetch" + structureID + "]"));
        }
        for(AbstractCommand command : fetchCommandBuffer) {
            CommandBlockMinecart minecart = new CommandBlockMinecart(command.command, command.name, new Vector3D(0,1,2));
            minecart.addTag("injector_fetch" + structureID);
            structure.addEntity(minecart);
        }

        TagCompound nbt = structure.toNbt();
        try (
            FileOutputStream fos = new FileOutputStream(injectFolderPath + master.prefix + structureID + ".nbt");
            GZIPOutputStream zos = new GZIPOutputStream(fos);
            NbtOutputStream nos = new NbtOutputStream(zos);
            PrintWriter pw = new PrintWriter(injectDataFile);) {
          nos.write(nbt);
          pw.println("l" + (structureID + 1));
            structureID++;
            filesCreated++;
        } catch(IOException x) {
            x.printStackTrace();
        }

        commandBuffer.clear();
        repeatingCommandBuffer.clear();
        fetchCommandBuffer.clear();
    }

    /**
     * Gets the maximum size of the impulse command block section given for this injector's structure files.
     *
     * @return A Vector3D where its x, y and z represent the maximum sizes in their respective axes.
     * */
    public Vector3D getImpulseSize() {
        return impulseSize;
    }

    /**
     * Sets the maximum size of the impulse command block section given for this injector's structure files.
     *
     * @param size A Vector3D containing the maximum sizes in their respective axes.
     * */
    public void setImpulseSize(Vector3D size) {
        this.impulseSize = new Vector3D(size);
    }

    /**
     * Gets the maximum size of the repeating command block section given for this injector's structure files.
     *
     * @return A Vector3D where its x, y and z represent the maximum sizes in their respective axes.
     * */
    public Vector3D getRepeatingSize() {
        return repeatingSize;
    }

    /**
     * Sets the maximum size of the repeating command block section given for this injector's structure files.
     *
     * @param size A Vector3D containing the maximum sizes in their respective axes.
     * */
    public void setRepeatingSize(Vector3D size) {
        this.repeatingSize = new Vector3D(size);
    }

    /**
     * Sets the layout manager for this injector to the one specified.
     *
     * @param layoutManager The new layout manager.
     * */
    public void setLayoutManager(ChainLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
}

/**
 * Class containing basic information about a command block.
 * */
class AbstractCommand {
    /**
     * The command.
     * */
    String command;
    /**
     * The name of this command block. May be null.
     * */
    String name = null;

    /**
     * Creates an AbstractCommand with the given command.
     *
     * @param command The command.
     * */
    AbstractCommand(String command) {
        this.command = command;
    }

    /**
     * Creates an AbstractCommand with the given command and name.
     *
     * @param command The command.
     * @param name The CustomName for this command's invoker.
     * */
    AbstractCommand(String command, String name) {
        this(command);
        this.name = name;
    }

    @Override
    public String toString() {
        return ((name != null) ? name : '@') + ": " + command;
    }
}

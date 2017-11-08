package com.energyxxer.inject.v2;

import static com.energyxxer.inject.structures.StructureBlock.Mode.LOAD;
import static com.energyxxer.inject.v2.CommandBlock.Type.CHAIN;
import static com.energyxxer.inject.v2.CommandBlock.Type.REPEAT;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.adrodoc55.minecraft.coordinate.Direction3.DOWN;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.energyxxer.inject.structures.StructureBlock;
import com.google.common.collect.Iterators;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * @author Adrodoc55
 */
@ThreadSafe
class InjectionBuffer {
  private final Logger logger = LogManager.getLogger();

  /**
   * The function to convert the {@code structureId} parameter of {@link #createStructure(int)} into
   * a {@link Structure} name.
   */
  private final IntFunction<String> getStructureName;

  /**
   * This flag indicates whether or not the next call to {@link #createStructure(int)} should
   * generate commands that if successful are written to the {@link MinecraftLogObserver#logFile log
   * file}. This is usually set to {@code true} when listening for the output of some commands.
   * <p>
   * <b>Implementation Notes</b><br>
   * This field is not {@code volatile} because it is only read while holding the exclusive write
   * lock and only written when holding the read lock of {@link #logAdminCommandsLock}.
   */
  private boolean logAdminCommands;
  /**
   * This {@link ReadWriteLock} is used to prevent concurrent adding of commands that require admin
   * command logging while {@link #createStructure(int) creating a structure}.<br>
   * Adding such commands is considered to be the read operation, because multiple commands may be
   * added concurrently.<br>
   * Creating a {@link Structure} is considered to be the write operation, because while creating
   * you may not add such a command or you might end up with one of the following scenarios:
   * <ul>
   * <li>If such a command is first added during {@link #createStructure(int)} then it might be
   * added to the {@link Structure} even though the preparation to enable logging was not
   * added.</li>
   * <li>If such a command is added during {@link #createStructure(int)} then the flag
   * {@link #logAdminCommands} might be set to {@code false} by {@link #createStructure(int)} even
   * though the command might not have been added to a {@link Structure} yet.</li>
   * </ul>
   */
  private final ReadWriteLock logAdminCommandsLock = new ReentrantReadWriteLock();

  private final Collection<Command> commands = new ConcurrentLinkedQueue<>();

  public InjectionBuffer(IntFunction<String> getStructureName) {
    this.getStructureName = checkNotNull(getStructureName, "getStructureName == null!");
  }

  private String getStructureName(int structureId) {
    return getStructureName.apply(structureId);
  }

  public void addCommand(Command command) throws IllegalStateException {
    commands.add(command);
  }

  public void addFetchCommand(Command command) throws IllegalStateException {
    logAdminCommandsLock.readLock().lock();
    try {
      addCommand(command);
      logAdminCommands = true;
    } finally {
      logAdminCommandsLock.readLock().unlock();
    }
  }

  private boolean isEmpty() {
    return commands.isEmpty();
  }

  /**
   * Create a new {@link Structure} from the contents of {@code this} {@link InjectionBuffer} and
   * clear {@code this} buffer.
   *
   * @param structureId the ID of the {@link Structure} to generate.
   * @return the generated {@link Structure} or {@code null} if {@code this} {@link InjectionBuffer}
   *         was already empty.
   */
  public @Nullable Structure createStructure(int structureId) {
    // WriteLock to prevent concurrent adding of commands that require admin command logging
    // AND synchronize the structure creation itself to prevent empty structures
    logAdminCommandsLock.writeLock().lock();
    boolean logAdminCommands = this.logAdminCommands; // This copy is used after releasing the lock
    Structure structure;
    try {
      if (isEmpty()) {
        logger.trace("Skipping creation of structure {} due to empty buffer", structureId);
        return null;
      }
      structure = new Structure(922, "Vanilla-Injection");
      if (logAdminCommands) {
        structure.addEntity(newCommandBlockMinecart(structureId, "gamerule logAdminCommands true"));
      }
      Iterators.consumingIterator(commands.iterator()).forEachRemaining(command -> {
        structure.addEntity(newCommandBlockMinecart(structureId, command));
      });
      this.logAdminCommands = false;
    } finally {
      // commandBuffer no longer contains comands that require admin command loggig, releasing lock
      logAdminCommandsLock.writeLock().unlock();
    }
    if (logAdminCommands) {
      structure.addEntity(newCommandBlockMinecart(structureId, "gamerule logAdminCommands false"));
    }
    structure.addEntity(newCommandBlockMinecart(structureId,
        "kill @e[type=commandblock_minecart,dy=0,tag=" + getStructureName(structureId) + "]"));

    structure.addBlock(
        new StructureBlock(new Coordinate3I(0, 0, 0), LOAD, getStructureName(structureId + 1)));
    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0, 2, 0)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0, 3, 0)));

    structure.addBlock(new CommandBlock("setblock ~ ~-1 ~ stone", new Coordinate3I(0, 1, 1), DOWN,
        CHAIN, false, true));
    structure.addBlock(new CommandBlock("setblock ~ ~-2 ~ redstone_block",
        new Coordinate3I(0, 2, 1), DOWN, REPEAT, false, false));

    structure.setBackground(new SimpleBlockState("minecraft:air"));
    return structure;
  }

  private static final Coordinate3D MINECART_POS = new Coordinate3D(0.5, 3.0625, 0.5);

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, String command) {
    return newCommandBlockMinecart(structureId, new Command(command));
  }

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, Command command) {
    CommandBlockMinecart result = new CommandBlockMinecart(command, MINECART_POS);
    result.addTag(getStructureName(structureId));
    return result;
  }
}

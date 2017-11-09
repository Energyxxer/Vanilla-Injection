package com.energyxxer.inject.v2;

import static com.energyxxer.inject.structures.StructureBlock.Mode.LOAD;
import static com.energyxxer.inject.v2.CommandBlock.Type.CHAIN;
import static com.energyxxer.inject.v2.CommandBlock.Type.IMPULSE;
import static com.energyxxer.inject.v2.CommandBlock.Type.REPEAT;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static de.adrodoc55.minecraft.coordinate.Direction3.DOWN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.energyxxer.inject.structures.StructureBlock;
import com.energyxxer.inject.v2.CommandBlock.Type;
import com.google.common.collect.Iterables;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Coordinate3I;
import de.adrodoc55.minecraft.coordinate.Direction3;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * @author Adrodoc55
 */
@ThreadSafe
class InjectionBuffer {
  private static final Logger LOGGER = LogManager.getLogger();

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

  private final Collection<Command> minecartCommands = new ConcurrentLinkedQueue<>();
  private final Collection<Command> impulseCommands = new ConcurrentLinkedQueue<>();

  public InjectionBuffer(IntFunction<String> getStructureName) {
    this.getStructureName = checkNotNull(getStructureName, "getStructureName == null!");
  }

  private String getStructureName(int structureId) {
    return getStructureName.apply(structureId);
  }

  public void addMinecartCommand(Command command) throws IllegalStateException {
    minecartCommands.add(command);
  }

  public void addMinecartFetchCommand(Command command) throws IllegalStateException {
    logAdminCommandsLock.readLock().lock();
    try {
      addMinecartCommand(command);
      logAdminCommands = true;
    } finally {
      logAdminCommandsLock.readLock().unlock();
    }
  }

  public void addImpulseCommand(Command command) throws IllegalStateException {
    impulseCommands.add(command);
  }

  public void addImpulseFetchCommand(Command command) throws IllegalStateException {
    logAdminCommandsLock.readLock().lock();
    try {
      addImpulseCommand(command);
      logAdminCommands = true;
    } finally {
      logAdminCommandsLock.readLock().unlock();
    }
  }

  private boolean isEmpty() {
    return minecartCommands.isEmpty() && impulseCommands.isEmpty();
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
    List<Command> minecartCommands;
    List<Command> impulseCommands;
    try {
      if (isEmpty()) {
        LOGGER.trace("Skipping creation of structure {} due to empty buffer", structureId);
        return null;
      }
      this.logAdminCommands = false;
      minecartCommands = new ArrayList<>(this.minecartCommands.size());
      for (Command command : Iterables.consumingIterable(this.minecartCommands)) {
        minecartCommands.add(command);
      }
      impulseCommands = new ArrayList<>(this.impulseCommands.size());
      for (Command command : Iterables.consumingIterable(this.impulseCommands)) {
        impulseCommands.add(command);
      }
    } finally {
      // this buffer no longer contains fetch commands, releasing lock
      logAdminCommandsLock.writeLock().unlock();
    }
    LOGGER.debug("Creating structure from commands {}", concat(minecartCommands, impulseCommands));
    Structure structure = new Structure(922, "Vanilla-Injection");
    structure.setBackground(new SimpleBlockState("minecraft:air"));
    String nextStructureName = getStructureName(structureId + 1);
    structure.addBlock(new StructureBlock(new Coordinate3I(0, 0, 0), LOAD, nextStructureName));
    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Coordinate3I(0, 2, 0)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Coordinate3I(0, 3, 0)));

    structure.addBlock(new CommandBlock("setblock ~ ~-1 ~ stone", new Coordinate3I(0, 1, 1), DOWN,
        CHAIN, false, true));
    structure.addBlock(new CommandBlock("setblock ~ ~-2 ~ redstone_block",
        new Coordinate3I(0, 2, 1), DOWN, REPEAT, false, false));

    if (logAdminCommands) {
      minecartCommands.add(0, new Command("gamerule logAdminCommands true"));
      minecartCommands.add(new Command("gamerule logAdminCommands false"));
      impulseCommands.add(0, new Command("gamerule logAdminCommands true"));
      impulseCommands.add(new Command("gamerule logAdminCommands false"));
    }

    int x = 2, y = 0, z = 0;
    for (Command command : impulseCommands) {
      Coordinate3I coordinate = new Coordinate3I(x, y, z);
      Direction3 direction = Direction3.SOUTH;
      Type type = z == 0 ? IMPULSE : CHAIN;
      boolean conditional = false;
      boolean auto = true;
      structure.addBlock(new CommandBlock(command, coordinate, direction, type, conditional, auto));
      z++;
    }

    minecartCommands.add(new Command(
        "kill @e[type=commandblock_minecart,dy=0,tag=" + getStructureName(structureId) + "]"));
    for (Command command : minecartCommands) {
      structure.addEntity(newCommandBlockMinecart(structureId, command));
    }
    return structure;
  }

  private static final Coordinate3D MINECART_POS = new Coordinate3D(0.5, 3.0625, 0.5);

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, Command command) {
    CommandBlockMinecart result = new CommandBlockMinecart(command, MINECART_POS);
    result.addTag(getStructureName(structureId));
    return result;
  }
}

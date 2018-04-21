package com.energyxxer.inject;

import static com.energyxxer.inject.structure.CommandBlock.Type.CHAIN;
import static com.energyxxer.inject.structure.CommandBlock.Type.IMPULSE;
import static com.energyxxer.inject.structure.CommandBlock.Type.REPEAT;
import static com.energyxxer.inject.structure.StructureBlock.Mode.LOAD;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.adrodoc55.common.concurrent.Locks.runLocked;
import static de.adrodoc55.minecraft.coordinate.Axis3.X;
import static de.adrodoc55.minecraft.coordinate.Direction3.DOWN;
import static de.adrodoc55.minecraft.coordinate.Vec3I.max;

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

import com.energyxxer.inject.structure.Command;
import com.energyxxer.inject.structure.CommandBlock;
import com.energyxxer.inject.structure.CommandBlock.Type;
import com.energyxxer.inject.structure.CommandBlockMinecart;
import com.energyxxer.inject.structure.StructureBlock;
import com.energyxxer.log.MinecraftLogObserver;
import com.google.common.collect.Iterables;

import de.adrodoc55.minecraft.coordinate.Vec3D;
import de.adrodoc55.minecraft.coordinate.Vec3I;
import de.adrodoc55.minecraft.placement.CommandBlockFactory;
import de.adrodoc55.minecraft.placement.CommandBlockPlacer;
import de.adrodoc55.minecraft.placement.NotEnoughSpaceException;
import de.adrodoc55.minecraft.structure.SimpleBlock;
import de.adrodoc55.minecraft.structure.SimpleBlockState;
import de.adrodoc55.minecraft.structure.Structure;

/**
 * @author Adrodoc55
 */
@ThreadSafe
public class InjectionBuffer {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * The function to convert the {@code structureId} parameter of {@link #createStructure(int)} into
   * a {@link Structure} name.
   */
  private final IntFunction<String> getStructureName;

  /**
   * The maximal size for the impulse command section of the structure files.
   */
  private Vec3I impulseSize = new Vec3I(8, 5, 5);

  /**
   * The maximal size for the repeat command section of the structure files.
   */
  private Vec3I repeatSize = new Vec3I(2, 5, 5);

  /**
   * The {@link Command}s that should be injected as {@link CommandBlockMinecart}s.
   */
  private final Collection<Command> minecartCommands = new ConcurrentLinkedQueue<>();
  /**
   * The {@link Command}s that should be injected as {@link Type#IMPULSE} {@link CommandBlock}s.
   */
  private final Collection<Command> impulseCommands = new ConcurrentLinkedQueue<>();
  /**
   * The {@link Command}s that should be injected as {@link Type#REPEAT} {@link CommandBlock}s.
   */
  private final Collection<Command> repeatCommands = new ConcurrentLinkedQueue<>();

  /**
   * This flag indicates whether or not the next call to {@link #createStructure(int)} should
   * generate {@link #minecartCommands} that if successful are written to the
   * {@link MinecraftLogObserver#logFile log file}. This is usually set to {@code true} when
   * listening for the output of some commands.
   * <p>
   * <b>Implementation Notes</b><br>
   * This field is not {@code volatile} because it is only read while holding the exclusive write
   * lock and only written when holding the read lock of {@link #logAdminCommandsLock}.
   */
  private boolean logMinecartCommands;
  /**
   * This flag indicates whether or not the next call to {@link #createStructure(int)} should
   * generate {@link #impulseCommands} that if successful are written to the
   * {@link MinecraftLogObserver#logFile log file}. This is usually set to {@code true} when
   * listening for the output of some commands.
   * <p>
   * <b>Implementation Notes</b><br>
   * This field is not {@code volatile} because it is only read while holding the exclusive write
   * lock and only written when holding the read lock of {@link #logAdminCommandsLock}.
   */
  private boolean logImpulseCommands;
  /**
   * This flag indicates whether or not the next call to {@link #createStructure(int)} should
   * generate {@link #repeatCommands} that if successful are written to the
   * {@link MinecraftLogObserver#logFile log file}. This is usually set to {@code true} when
   * listening for the output of some commands.
   * <p>
   * <b>Implementation Notes</b><br>
   * This field is not {@code volatile} because it is only read while holding the exclusive write
   * lock and only written when holding the read lock of {@link #logAdminCommandsLock}.
   */
  private boolean logRepeatCommands;
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
   * <li>If such a command is added during {@link #createStructure(int)} then the flags
   * {@link #logMinecartCommands}, {@link #logImpulseCommands} or {@link #logRepeatCommands} might
   * be set to {@code false} by {@link #createStructure(int)} even though the command might not have
   * been added to a {@link Structure} yet.</li>
   * </ul>
   */
  private final ReadWriteLock logAdminCommandsLock = new ReentrantReadWriteLock();

  /**
   * The minimal size of the next structure needed to clear all blocks of the previous structure.
   */
  private @Nullable Vec3I minSize;

  public InjectionBuffer(IntFunction<String> getStructureName) {
    this.getStructureName = checkNotNull(getStructureName, "getStructureName == null!");
  }

  private String getStructureName(int structureId) {
    return getStructureName.apply(structureId);
  }

  /**
   * @return the value of {@link #impulseSize}
   */
  public Vec3I getImpulseSize() {
    return impulseSize;
  }

  /**
   * @param impulseSize the new value of {@link #impulseSize}
   */
  public void setImpulseSize(Vec3I impulseSize) {
    this.impulseSize = checkNotNull(impulseSize, "impulseSize == null!");
  }

  /**
   * @return the value of {@link #repeatSize}
   */
  public Vec3I getRepeatSize() {
    return repeatSize;
  }

  /**
   * @param repeatSize the new value of {@link #repeatSize}
   */
  public void setRepeatSize(Vec3I repeatSize) {
    this.repeatSize = checkNotNull(repeatSize, "repeatSize == null!");
  }

  /**
   * @author Adrodoc55
   */
  public enum InjectionType {
    MINECART {
      @Override
      protected Collection<Command> getCommandCollection(InjectionBuffer buffer) {
        return buffer.minecartCommands;
      }

      @Override
      protected void setLogCommands(InjectionBuffer buffer, boolean logCommands) {
        buffer.logMinecartCommands = logCommands;
      }
    }, //
    IMPULSE {
      @Override
      protected Collection<Command> getCommandCollection(InjectionBuffer buffer) {
        return buffer.impulseCommands;
      }

      @Override
      protected void setLogCommands(InjectionBuffer buffer, boolean logCommands) {
        buffer.logImpulseCommands = logCommands;
      }
    }, //
    REPEAT {
      @Override
      protected Collection<Command> getCommandCollection(InjectionBuffer buffer) {
        return buffer.repeatCommands;
      }

      @Override
      protected void setLogCommands(InjectionBuffer buffer, boolean logCommands) {
        buffer.logRepeatCommands = logCommands;
      }
    }, //
    ;
    protected abstract Collection<Command> getCommandCollection(InjectionBuffer buffer);

    protected abstract void setLogCommands(InjectionBuffer buffer, boolean logCommands);
  }

  public void addCommand(InjectionType type, Command command) {
    checkNotNull(type, "type == null!");
    Collection<Command> commandCollection = type.getCommandCollection(this);
    commandCollection.add(command);
  }

  public void addCommands(InjectionType type, Collection<? extends Command> commands) {
    checkNotNull(type, "type == null!");
    Collection<Command> commandCollection = type.getCommandCollection(this);
    commandCollection.addAll(commands);
  }

  public void addFetchCommand(InjectionType type, Command command) {
    runLocked(logAdminCommandsLock.readLock(), () -> {
      addCommand(type, command);
      type.setLogCommands(this, true);
    });
  }

  public void addFetchCommands(InjectionType type, Collection<? extends Command> commands) {
    runLocked(logAdminCommandsLock.readLock(), () -> {
      addCommands(type, commands);
      type.setLogCommands(this, true);
    });
  }

  private boolean isEmpty() {
    return minecartCommands.isEmpty() && impulseCommands.isEmpty() && repeatCommands.isEmpty();
  }

  /**
   * Create a new {@link Structure} from the contents of {@code this} {@link InjectionBuffer} and
   * clear {@code this} buffer.
   *
   * @param structureId the ID of the {@link Structure} to generate.
   * @return the generated {@link Structure} or {@code null} if {@code this} {@link InjectionBuffer}
   *         was already empty.
   */
  public synchronized @Nullable Structure createStructure(int structureId) {
    if (isEmpty()) {
      LOGGER.trace("Skipping creation of structure {} due to empty buffer", structureId);
      return null;
    }
    // WriteLock to prevent concurrent adding of commands that require admin command logging
    logAdminCommandsLock.writeLock().lock();
    List<Command> minecartCommands;
    List<Command> impulseCommands;
    List<Command> repeatCommands;
    try {
      minecartCommands = new ArrayList<>(this.minecartCommands.size());
      for (Command command : Iterables.consumingIterable(this.minecartCommands)) {
        minecartCommands.add(command);
      }
      if (logMinecartCommands) {
        minecartCommands.add(0, new Command("gamerule logAdminCommands true"));
        minecartCommands.add(new Command("gamerule logAdminCommands false"));
        this.logMinecartCommands = false;
      }
      impulseCommands = new ArrayList<>(this.impulseCommands.size());
      for (Command command : Iterables.consumingIterable(this.impulseCommands)) {
        impulseCommands.add(command);
      }
      if (logImpulseCommands) {
        impulseCommands.add(0, new Command("gamerule logAdminCommands true"));
        impulseCommands.add(new Command("gamerule logAdminCommands false"));
        this.logImpulseCommands = false;
      }
      repeatCommands = new ArrayList<>(this.repeatCommands.size());
      for (Command command : Iterables.consumingIterable(this.repeatCommands)) {
        repeatCommands.add(command);
      }
      if (logRepeatCommands) {
        repeatCommands.add(0, new Command("gamerule logAdminCommands true"));
        repeatCommands.add(new Command("gamerule logAdminCommands false"));
        this.logRepeatCommands = false;
      }
    } finally {
      // this buffer no longer contains fetch commands, releasing lock
      logAdminCommandsLock.writeLock().unlock();
    }

    LOGGER.debug("Creating structure");
    LOGGER.debug("minecartCommands {}", minecartCommands);
    LOGGER.debug("impulseCommands {}", impulseCommands);
    LOGGER.debug("repeatCommands {}", repeatCommands);

    Structure structure = new Structure(922, "Vanilla-Injection");
    structure.setBackground(new SimpleBlockState("minecraft:air"));
    String nextStructureName = getStructureName(structureId + 1);
    structure.addBlock(new StructureBlock(new Vec3I(0, 0, 0), LOAD, nextStructureName));
    structure.addBlock(new SimpleBlock("minecraft:redstone_block", new Vec3I(0, 2, 0)));
    structure.addBlock(new SimpleBlock("minecraft:activator_rail", new Vec3I(0, 3, 0)));

    structure.addBlock(
        new CommandBlock("setblock ~ ~-1 ~ stone", new Vec3I(0, 1, 1), DOWN, CHAIN, false, true));
    structure.addBlock(new CommandBlock("setblock ~ ~-2 ~ redstone_block", new Vec3I(0, 2, 1), DOWN,
        REPEAT, false, false));

    if (!minecartCommands.isEmpty()) {
      minecartCommands.add(new Command(
          "kill @e[type=commandblock_minecart,dy=0,tag=" + getStructureName(structureId) + "]"));
    }
    for (Command command : minecartCommands) {
      structure.addEntity(newCommandBlockMinecart(structureId, command));
    }

    Vec3I impulseStart = new Vec3I(2, 0, 0);
    structure.addBlocks(createCommandBlocks(IMPULSE, impulseStart, impulseSize, impulseCommands));

    Vec3I repeatStart = impulseStart.plus(impulseSize.x, X);
    structure.addBlocks(createCommandBlocks(REPEAT, repeatStart, repeatSize, repeatCommands));

    // Each structure has to be big enough to clear all blocks of the previous structure
    Vec3I calcSize = structure.calcSize();
    if (minSize != null) {
      structure.setExplicitSize(max(minSize, calcSize));
    }
    minSize = calcSize;

    return structure;
  }

  private static final Vec3D MINECART_POS = new Vec3D(0.5, 3.0625, 0.5);

  private CommandBlockMinecart newCommandBlockMinecart(int structureId, Command command) {
    CommandBlockMinecart result = new CommandBlockMinecart(command, MINECART_POS);
    result.addTag(getStructureName(structureId));
    return result;
  }

  private Collection<CommandBlock> createCommandBlocks(Type type, Vec3I start, Vec3I size,
      List<Command> commands) {
    Vec3I max = start.plus(size);
    CommandBlockFactory<Command, CommandBlock> factory = newCommandBlockFactory(type);
    try {
      return CommandBlockPlacer.place(commands, start, max, factory);
    } catch (NotEnoughSpaceException ex) {
      throw new BufferOverflowException(ex);
    }
  }

  private CommandBlockFactory<Command, CommandBlock> newCommandBlockFactory(Type initialType) {
    return (indexInChain, command, coordinate, direction) -> {
      command = command != null ? command : new Command("");
      Type type = indexInChain == 0 ? initialType : CHAIN;
      boolean conditional = command.isConditional();
      boolean auto = true;
      return new CommandBlock(command, coordinate, direction, type, conditional, auto);
    };
  }
}

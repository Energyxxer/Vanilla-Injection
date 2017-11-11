package de.adrodoc55.minecraft.placement;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.adrodoc55.minecraft.coordinate.Direction3.SOUTH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import de.adrodoc55.minecraft.coordinate.Vec3I;
import de.adrodoc55.minecraft.coordinate.Direction3;

/**
 * @author Adrodoc55
 * @see #place(List, List)
 */
class ChainPlacer<C extends Command> {
  /**
   * Places {@link CommandBlock}s along a specified {@code curve} and make sure that a
   * {@link Command#isConditional() conditional} {@link Command} is never located at a turn in the
   * {@code curve}. This is achieved by inserting empty command blocks somewhere before an illegal
   * conditional command to "push" it past the turn. Such an empty command block is sometimes
   * referred to as NOP (for no operation).
   *
   * @param chain the {@link Command}s to place
   * @param curve the curve to place the {@link CommandBlock}s along
   * @return the placed {@link CommandBlock}s
   * @throws NotEnoughSpaceException if not all {@link Command}s of {@code chain} could be placed
   *         before the {@code curve} ended
   */
  public static <C extends Command> List<CommandBlock<C>> place(List<? extends C> chain,
      List<Vec3I> curve) throws NotEnoughSpaceException {
    return new ChainPlacer<C>(chain, curve).place();
  }

  /**
   * The {@link Direction3} to use when placing only a single command block.
   */
  private static final Direction3 DEFAULT_DIRECTION = SOUTH;

  private final List<? extends C> chain;
  private final List<? extends Vec3I> curve;

  public ChainPlacer(List<? extends C> chain, List<? extends Vec3I> curve) {
    this.chain = checkNotNull(chain, "chain == null!");
    this.curve = checkNotNull(curve, "curve == null!");
    commandIterator = chain.listIterator();
    coordinateIterator = curve.listIterator();
  }

  private ListIterator<? extends C> commandIterator;
  private ListIterator<? extends Vec3I> coordinateIterator;

  private static class Backup<C extends Command> {
    int commandIndex;
    int coordinateIndex;
    List<CommandBlock<C>> result;

    public Backup(int commandIndex, int coordinateIndex,
        Collection<? extends CommandBlock<C>> result) {
      this.commandIndex = commandIndex;
      this.coordinateIndex = coordinateIndex;
      this.result = new ArrayList<>(result);
    }
  }

  private @Nullable Backup<C> backup;

  /**
   * The {@link Direction3} of the command block that was last placed or {@code null} if no command
   * was placed yet. If a previous command block exists then it points to
   * {@link #currentCoordinate}.
   */
  private @Nullable Direction3 previousDirection;
  /**
   * The {@link Vec3I} of the command block that is about to be placed.
   */
  private Vec3I currentCoordinate;
  /**
   * The {@link Direction3} of the command block that is about to be placed. If the current command
   * block is the last command block then {@link #currentDirection} equals
   * {@link #previousDirection}, otherwise it points to the {@link Vec3I} of the next command
   * block.
   */
  private Direction3 currentDirection;

  public List<CommandBlock<C>> place() throws NotEnoughSpaceException {
    backup = null;
    setCoordinateIndex(0);
    List<CommandBlock<C>> result = new ArrayList<>();
    while (commandIterator.hasNext()) {
      C command = commandIterator.next();
      if (canPlaceCommand(command)) {
        if (canPlaceNoOperationInsteadOf(command)) {
          createBackup(result);
        }
        place(result, command);
      } else {
        result = restoreBackup();
        placeNoOperation(result);
        createBackup(result);
      }
    }
    return result;
  }

  private boolean canPlaceCommand(Command command) {
    return !command.isConditional() || previousDirection == null
        || previousDirection == currentDirection;
  }

  private boolean canPlaceNoOperationInsteadOf(C command) {
    return !command.isConditional();
  }

  private void placeNoOperation(List<CommandBlock<C>> result) throws NotEnoughSpaceException {
    place(result, null);
  }

  private void place(List<CommandBlock<C>> result, @Nullable C command)
      throws NotEnoughSpaceException {
    result.add(new CommandBlock<>(command, currentCoordinate, currentDirection));
    incrementCoordinateIndex();
  }

  private void createBackup(Collection<? extends CommandBlock<C>> result) {
    backup =
        new Backup<>(commandIterator.previousIndex(), coordinateIterator.previousIndex(), result);
  }

  private List<CommandBlock<C>> restoreBackup() throws NotEnoughSpaceException {
    if (backup == null) {
      throw new NotEnoughSpaceException();
    }
    commandIterator = chain.listIterator(backup.commandIndex);
    setCoordinateIndex(backup.coordinateIndex);
    return backup.result;
  }

  private void setCoordinateIndex(int coordinateIndex) {
    coordinateIterator = curve.listIterator(coordinateIndex);
    currentCoordinate = coordinateIterator.next();
    if (coordinateIndex > 0) {
      Vec3I previousCoordinate = curve.get(coordinateIndex - 1);
      previousDirection = Direction3.valueOf(currentCoordinate.minus(previousCoordinate));
    } else {
      previousDirection = null;
    }
    updateCurrentDirection();
  }

  private void incrementCoordinateIndex() throws NotEnoughSpaceException {
    if (!coordinateIterator.hasNext()) {
      throw new NotEnoughSpaceException();
    }
    currentCoordinate = coordinateIterator.next();
    previousDirection = currentDirection;
    updateCurrentDirection();
  }

  private void updateCurrentDirection() {
    if (coordinateIterator.hasNext()) {
      coordinateIterator.next(); // Peek nextCoordinate
      Vec3I nextCoordinate = coordinateIterator.previous();
      currentDirection = Direction3.valueOf(nextCoordinate.minus(currentCoordinate));
    } else if (previousDirection != null) {
      currentDirection = previousDirection;
    } else {
      previousDirection = DEFAULT_DIRECTION;
    }
  }
}

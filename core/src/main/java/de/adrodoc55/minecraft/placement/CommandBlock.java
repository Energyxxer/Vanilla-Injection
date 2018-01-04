package de.adrodoc55.minecraft.placement;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import de.adrodoc55.minecraft.coordinate.Direction3;
import de.adrodoc55.minecraft.coordinate.Vec3I;

/**
 * The intermediate result of {@link ChainPlacer#place(List, List)} which is later converted in
 * {@link CommandBlockPlacer#transformResult(CommandBlockFactory, List)}.
 *
 * @author Adrodoc55
 */
@Immutable
class CommandBlock<C extends Command> {
  /**
   * The {@link Command} or {@code null} if this command block represents a NOP (no operation).
   */
  private final @Nullable C command;
  private final Vec3I coordinate;
  private final Direction3 direction;

  public CommandBlock(@Nullable C command, Vec3I coordinate, Direction3 direction) {
    this.command = command;
    this.coordinate = checkNotNull(coordinate, "coordinate == null!");
    this.direction = checkNotNull(direction, "direction == null!");
  }

  /**
   * @return the value of {@link #command}
   */
  public C getCommand() {
    return command;
  }

  /**
   * @return the value of {@link #coordinate}
   */
  public Vec3I getCoordinate() {
    return coordinate;
  }

  /**
   * @return the value of {@link #direction}
   */
  public Direction3 getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return "CommandBlock [command=" + command + ", coordinate=" + coordinate + ", direction="
        + direction + "]";
  }
}

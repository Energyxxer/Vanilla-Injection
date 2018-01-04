package de.adrodoc55.minecraft.placement;

import java.util.List;

import javax.annotation.Nullable;

import de.adrodoc55.minecraft.coordinate.Vec3I;
import de.adrodoc55.minecraft.coordinate.Direction3;
import de.adrodoc55.minecraft.coordinate.Orientation3;

/**
 * A factory for command blocks used in
 * {@link CommandBlockPlacer#place(List, Vec3I, Vec3I, Orientation3, CommandBlockFactory)}.
 *
 * @param <C> the type of {@link Command}
 * @param <CB> the type of command block
 * @author Adrodoc55
 */
public interface CommandBlockFactory<C extends Command, CB> {
  /**
   * Create an instance of {@code <CB>} from the specified parameters.
   *
   * @param indexInChain zero based index of the command block in the chain, this can for instance
   *        be used to make the first command block repeating.
   * @param command the {@link Command} or {@code null} if the command block should be empty
   * @param coordinate the {@link Vec3I}
   * @param direction the {@link Direction3}
   * @return an instance of {@code <CB>}
   */
  CB create(int indexInChain, @Nullable C command, Vec3I coordinate, Direction3 direction);
}

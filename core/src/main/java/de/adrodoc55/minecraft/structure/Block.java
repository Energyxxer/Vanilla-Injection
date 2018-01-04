package de.adrodoc55.minecraft.structure;

import de.adrodoc55.minecraft.coordinate.Vec3I;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Block">block</a> that can be added to a
 * {@link Structure}.
 *
 * @author Adrodoc55
 */
public interface Block extends BlockState {
  /**
   * The relative coordinate of this {@link Block} within a {@link Structure}.
   *
   * @return relative coordinate
   */
  Vec3I getCoordinate();
}

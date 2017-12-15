package de.adrodoc55.minecraft.placement;

/**
 * A Minecraft <a href="https://minecraft.gamepedia.com/Commands">command</a> that can be placed by
 * {@link CommandBlockPlacer}.
 *
 * @author Adrodoc55
 */
public interface Command {
  boolean isConditional();
}

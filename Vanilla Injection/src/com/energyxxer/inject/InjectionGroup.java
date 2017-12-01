package com.energyxxer.inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.energyxxer.inject.InjectionBuffer.InjectionType;
import com.energyxxer.inject.structure.Command;
import com.energyxxer.log.SuccessEvent;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import de.adrodoc55.minecraft.structure.Structure;

/**
 * An {@link InjectionGroup} is used to inject multiple {@link Command}s in one atomic action to
 * ensure that they are injected in one {@link Structure}.
 *
 * @author Adrodoc55
 * @see InjectionConnection#inject(InjectionType, InjectionGroup)
 */
public class InjectionGroup {
  private final List<Command> commands = new ArrayList<>();
  private final ListMultimap<String, Consumer<SuccessEvent>> listeners = ArrayListMultimap.create();

  public List<Command> getCommands() {
    return Collections.unmodifiableList(commands);
  }

  public ListMultimap<String, Consumer<SuccessEvent>> getListeners() {
    return Multimaps.unmodifiableListMultimap(listeners);
  }

  public boolean containsFetchCommands() {
    return !listeners.isEmpty();
  }

  public void add(String command) {
    add(new Command(command));
  }

  public void add(Command command) {
    commands.add(command);
  }

  public void add(String command, Consumer<SuccessEvent> listener) {
    String name = UUID.randomUUID().toString();
    add(new Command(name, command), listener);
  }

  public void add(Command command, Consumer<SuccessEvent> listener) {
    commands.add(command);
    listeners.put(command.getName(), listener);
  }
}

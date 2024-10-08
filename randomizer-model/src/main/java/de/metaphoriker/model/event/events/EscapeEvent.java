package de.metaphoriker.model.event.events;

import de.metaphoriker.model.event.Event;
import java.awt.event.KeyEvent;

public class EscapeEvent extends Event {

  @Override
  public void execute() {

    int key = KeyEvent.VK_ESCAPE;

    robot.keyPress(key);
    robot.keyRelease(key);
  }

  @Override
  public String description() {
    return "Presses Escape to troll";
  }
}

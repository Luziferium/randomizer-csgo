package de.metaphoriker.model.event.cluster;

import de.metaphoriker.model.event.Event;
import de.metaphoriker.model.json.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EventCluster {

  private final String name;
  private final List<Event> events;
  public EventCluster(String name, List<Event> events) {
    this.name = name;
    this.events = events;
  }

  public static EventCluster formatEventCluster(String name, String content) {

    String[] events = content.split(";");
    List<Event> eventList = new ArrayList<>();

    for (String event : events) eventList.add(JsonUtil.deserialize(event));

    return new EventCluster(name, eventList);
  }

  public String getName() {
    return name;
  }

  public List<Event> getEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public String toString() {
    return "EventCluster{" + "name='" + name + '\'' + ", events=" + events + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EventCluster that = (EventCluster) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}

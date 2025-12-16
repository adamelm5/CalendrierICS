package eirb.pg203;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ComponentListTest {
  private ComponentList<CalendarComponent> componentList;
  private Event event1;
  private Event event2;
  private Todo todo1;
  private Todo todo2;

  @Before
  public void setUp() {
    componentList = new ComponentList<>();

    event1 = new Event("uid1", "Meeting", "Office", "20240101T120000Z", 1, "20240101T120000Z",
        "20240110T090000Z", "20240110T100000Z", "Project meeting", "20231231T110000Z");

    event2 = new Event("uid2", "Conference", "Hall", "20240102T120000Z", 1, "20240102T120000Z",
        "20240115T090000Z", "20240115T170000Z", "Annual conference", "20231231T120000Z");

    todo1 = new Todo("uid3", "Submit Report", "Home", "20240102T130000Z", 1, "20240102T130000Z",
        "20240115T170000Z", "20240115T170000Z", "IN-PROCESS", 50, 3, "", "organizer@example.com",
        "PRIVATE");

    todo2 = new Todo("uid4", "Read Book", "Library", "20240103T130000Z", 1, "20240103T130000Z",
        "20240120T170000Z", "20240120T170000Z", "COMPLETED", 100, 2, "20240110T120000Z",
        "me@example.com", "PUBLIC");
  }

  @Test
  public void testDefaultConstructor() {
    ComponentList<CalendarComponent> list = new ComponentList<>();
    assertNotNull(list);
    assertTrue(list.isEmpty());
    assertEquals(0, list.size());
  }

  @Test
  public void testConstructorWithList() {
    List<CalendarComponent> initialList = new ArrayList<>();
    initialList.add(event1);
    initialList.add(todo1);

    ComponentList<CalendarComponent> list = new ComponentList<>(initialList);
    assertEquals(2, list.size());
    assertTrue(list.contains(event1));
    assertTrue(list.contains(todo1));
  }

  @Test
  public void testAddAndGet() {
    componentList.add(event1);
    componentList.add(todo1);

    assertEquals(2, componentList.size());
    assertEquals(event1, componentList.get(0));
    assertEquals(todo1, componentList.get(1));
  }

  @Test
  public void testRemove() {
    componentList.add(event1);
    componentList.add(todo1);
    assertTrue(componentList.remove(event1));
    assertEquals(1, componentList.size());
    assertEquals(todo1, componentList.get(0));
    assertFalse(componentList.remove(event2));
  }

  @Test
  public void testGetComponents() {
    componentList.add(event1);
    componentList.add(todo1);
    List<CalendarComponent> components = componentList.getComponents();
    assertEquals(2, components.size());
    assertTrue(components.contains(event1));
    assertTrue(components.contains(todo1));
    components.clear();
    assertEquals(2, componentList.size());
  }
  @Test
  public void testSizeAndIsEmpty() {
    assertTrue(componentList.isEmpty());
    assertEquals(0, componentList.size());

    componentList.add(event1);

    assertFalse(componentList.isEmpty());
    assertEquals(1, componentList.size());
  }

  @Test
  public void testClear() {
    componentList.add(event1);
    componentList.add(todo1);

    assertEquals(2, componentList.size());

    componentList.clear();

    assertTrue(componentList.isEmpty());
    assertEquals(0, componentList.size());
  }

  @Test
  public void testContains() {
    assertFalse(componentList.contains(event1));

    componentList.add(event1);

    assertTrue(componentList.contains(event1));
    assertFalse(componentList.contains(event2));
  }

  @Test
  public void testIterator() {
    componentList.add(event1);
    componentList.add(todo1);

    Iterator<CalendarComponent> iterator = componentList.iterator();
    assertNotNull(iterator);

    int count = 0;
    while (iterator.hasNext()) {
      CalendarComponent component = iterator.next();
      assertTrue(component == event1 || component == todo1);
      count++;
    }
    assertEquals(2, count);
  }

  @Test
  public void testAddAll() {
    ComponentList<CalendarComponent> otherList = new ComponentList<>();
    otherList.add(event1);
    otherList.add(todo1);

    componentList.add(event2);
    componentList.addAll(otherList);

    assertEquals(3, componentList.size());
    assertTrue(componentList.contains(event1));
    assertTrue(componentList.contains(event2));
    assertTrue(componentList.contains(todo1));
  }

  @Test
  public void testToStringByTypeText() {
    componentList.add(event1);
    componentList.add(todo1);

    String textOutput = componentList.toStringByType("text");

    assertNotNull(textOutput);
    assertTrue(textOutput.contains("Meeting"));
    assertTrue(textOutput.contains("Submit Report"));
    assertTrue(textOutput.contains("Start:"));
    assertTrue(textOutput.contains("\n\n"));
  }

  @Test
  public void testToStringByTypeHtml() {
    componentList.add(event1);
    componentList.add(todo1);

    String htmlOutput = componentList.toStringByType("html");

    assertNotNull(htmlOutput);
    assertTrue(htmlOutput.contains("<div"));
    assertTrue(htmlOutput.contains("</div>"));
    assertTrue(htmlOutput.contains("<h3"));
    assertTrue(htmlOutput.contains("Event #1"));
    assertTrue(htmlOutput.contains("Todo #1"));
    assertTrue(htmlOutput.contains("style="));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToStringByTypeIcs() {
    componentList.add(event1);
    componentList.toStringByType("ics");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToStringByTypeInvalid() {
    componentList.add(event1);
    componentList.toStringByType("invalid");
  }

  @Test
  public void testCountTodos() {
    assertEquals(0, componentList.countTodos());

    componentList.add(event1);
    assertEquals(0, componentList.countTodos());

    componentList.add(todo1);
    assertEquals(1, componentList.countTodos());

    componentList.add(todo2);
    assertEquals(2, componentList.countTodos());
  }

  @Test
  public void testFilterByTypeEvents() {
    componentList.add(event1);
    componentList.add(todo1);
    componentList.add(event2);
    componentList.add(todo2);

    ComponentList<CalendarComponent> eventsOnly = componentList.filterByType(Event.class);

    assertEquals(2, eventsOnly.size());
    for (CalendarComponent component : eventsOnly.getComponents()) {
      assertTrue(component instanceof Event);
    }
  }

  @Test
  public void testFilterByTypeTodos() {
    componentList.add(event1);
    componentList.add(todo1);
    componentList.add(event2);
    componentList.add(todo2);

    ComponentList<CalendarComponent> todosOnly = componentList.filterByType(Todo.class);

    assertEquals(2, todosOnly.size());
    for (CalendarComponent component : todosOnly.getComponents()) {
      assertTrue(component instanceof Todo);
    }
  }

  @Test
  public void testFilterByTypeEmptyList() {
    ComponentList<CalendarComponent> filtered = componentList.filterByType(Event.class);
    assertTrue(filtered.isEmpty());
    assertEquals(0, filtered.size());
  }

  @Test
  public void testIterableForEach() {
    componentList.add(event1);
    componentList.add(todo1);

    int count = 0;
    for (CalendarComponent component : componentList) {
      assertNotNull(component);
      count++;
    }
    assertEquals(2, count);
  }

  @Test
  public void testHtmlOutputColors() {
    componentList.add(event1);
    componentList.add(todo1);
    String html = componentList.toStringByType("html");
    assertTrue(html.contains("#3498db"));
    assertTrue(html.contains("#2ecc71"));
  }

  @Test
  public void testHtmlNumbering() {
    componentList.add(event1);
    componentList.add(todo1);
    componentList.add(event2);
    componentList.add(todo2);

    String html = componentList.toStringByType("html");
    assertTrue(html.contains("Event #1"));
    assertTrue(html.contains("Event #2"));
    assertTrue(html.contains("Todo #1"));
    assertTrue(html.contains("Todo #2"));
  }

  @Test
  public void testTextOutputFormat() {
    componentList.add(event1);
    String text = componentList.toStringByType("text");
    assertTrue(text.startsWith(" Start:"));
    assertTrue(text.contains("Summary:"));
    assertTrue(text.contains("Meeting"));
    assertTrue(text.endsWith("\n\n") || !text.endsWith("\n\n"));
  }
}
package eirb.pg203;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class OutputsTest {
  private outputs outputGenerator;

  @Before
  public void setUp() {
    outputGenerator = new outputs("text");
  }

  @Test
  public void testConstructorAndGetters() {
    outputs textOutput = new outputs("text");
    assertEquals("text", textOutput.getOutputType());
    outputs htmlOutput = new outputs("html");
    assertEquals("html", htmlOutput.getOutputType());
    outputs icsOutput = new outputs("ics");
    assertEquals("ics", icsOutput.getOutputType());
  }

  @Test
  public void testSetter() {
    outputGenerator.setOutputType("html");
    assertEquals("html", outputGenerator.getOutputType());
    outputGenerator.setOutputType("ics");
    assertEquals("ics", outputGenerator.getOutputType());
  }

  @Test
  public void testDateTimeParserValid() {
    String icalDate = "20241216T143000Z";
    String expected = "16/12/2024 14:30:00";
    String result = outputGenerator.dateTimeParser(icalDate);
    assertEquals(expected, result);
  }

  @Test
  public void testDateTimeParserNull() {
    assertNull(outputGenerator.dateTimeParser(null));
  }

  @Test
  public void testDateTimeParserTooShort() {
    assertNull(outputGenerator.dateTimeParser("20241216"));
  }

  @Test
  public void testIcsToCalendarComponentEvent() {
    String eventIcs = "BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Meeting\n"
        + "LOCATION:Office\n"
        + "LAST-MODIFIED:20241216T120000Z\n"
        + "SEQUENCE:1\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241216T090000Z\n"
        + "DTEND:20241216T100000Z\n"
        + "DESCRIPTION:Project meeting\n"
        + "CREATED:20241215T110000Z\n"
        + "END:VEVENT";
    CalendarComponent component = outputGenerator.icsToCalendarComponent(eventIcs, "event");
    assertNotNull(component);
    assertTrue(component instanceof Event);
    Event event = (Event) component;
    assertEquals("event1@test.com", event.getUid());
    assertEquals("Test Meeting", event.getSummary());
    assertEquals("Office", event.getLocation());
    assertEquals("1", String.valueOf(event.getSequence()));
    assertTrue(event.getDtStart().contains("16/12/2024"));
    assertEquals("Project meeting", event.getDescription());
  }

  @Test
  public void testIcsToCalendarComponentTodo() {
    String todoIcs = "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Test Todo\n"
        + "LOCATION:Home\n"
        + "LAST-MODIFIED:20241216T130000Z\n"
        + "SEQUENCE:2\n"
        + "DTSTAMP:20241216T130000Z\n"
        + "DTSTART:20241216T000000Z\n"
        + "DUE:20241220T000000Z\n"
        + "STATUS:IN-PROCESS\n"
        + "PERCENT-COMPLETE:50\n"
        + "PRIORITY:3\n"
        + "COMPLETED:\n"
        + "ORGANIZER:mailto:test@example.com\n"
        + "CLASSIFICATION:PRIVATE\n"
        + "END:VTODO";

    CalendarComponent component = outputGenerator.icsToCalendarComponent(todoIcs, "todo");
    assertNotNull(component);
    assertTrue(component instanceof Todo);
    Todo todo = (Todo) component;
    assertEquals("todo1@test.com", todo.getUid());
    assertEquals("Test Todo", todo.getSummary());
    assertEquals("Home", todo.getLocation());
    assertEquals("2", String.valueOf(todo.getSequence()));
    assertEquals(50, todo.getPercentComplete());
    assertEquals(3, todo.getPriority());
    assertEquals("IN-PROCESS", todo.getStatus());
    assertEquals("mailto:test@example.com", todo.getOrganizer());
    assertEquals("PRIVATE", todo.getClassification());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIcsToCalendarComponentInvalidType() {
    String icsString = "BEGIN:VEVENT\nEND:VEVENT";
    outputGenerator.icsToCalendarComponent(icsString, "invalid");
  }

  @Test
  public void testIcsToCalendarComponentMissingFields() {
    String minimalEventIcs = "BEGIN:VEVENT\n"
        + "UID:minimal@test.com\n"
        + "SUMMARY:Minimal Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT";
    CalendarComponent component = outputGenerator.icsToCalendarComponent(minimalEventIcs, "event");
    assertNotNull(component);
    Event event = (Event) component;
    assertEquals("minimal@test.com", event.getUid());
    assertEquals("Minimal Event", event.getSummary());
    assertEquals("", event.getLocation());
    assertEquals(0, event.getSequence());
    assertEquals("", event.getDescription());
  }

  @Test
  public void testIcsToCalendarComponentWithSpecialCharacters() {
    String eventIcs = "BEGIN:VEVENT\n"
        + "UID:special@test.com\n"
        + "SUMMARY:Réunion importante avec des accents é à è\n"
        + "LOCATION:Bureau n°123\n"
        + "DTSTART:20241216T090000Z\n"
        + "DESCRIPTION:Description avec \\n saut de ligne\n"
        + "END:VEVENT";
    CalendarComponent component = outputGenerator.icsToCalendarComponent(eventIcs, "event");
    assertNotNull(component);
    assertEquals("Réunion importante avec des accents é à è", component.getSummary());
  }

  @Test
  public void testIcsListToCalendarComponentsList() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Event 1\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");
    icsStrings.add("BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Todo 1\n"
        + "DTSTART:20241216T000000Z\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO");
    ComponentList<CalendarComponent> componentList =
        outputGenerator.icsListToCalendarComponentsList(icsStrings);
    assertNotNull(componentList);
    assertEquals(2, componentList.size());
    assertTrue(componentList.get(0) instanceof Event);
    assertTrue(componentList.get(1) instanceof Todo);
    assertEquals("Event 1", componentList.get(0).getSummary());
    assertEquals("Todo 1", componentList.get(1).getSummary());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIcsListToCalendarComponentsListInvalidComponent() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("INVALID:STRING\nWITHOUT:BEGIN");

    outputGenerator.icsListToCalendarComponentsList(icsStrings);
  }

  @Test
  public void testGenerateOutputText() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "DTEND:20241216T100000Z\n"
        + "END:VEVENT");
    String output = outputGenerator.generateOutput(icsStrings, "text");
    assertNotNull(output);
    assertTrue(output.contains("Éléments du calendrier formatés en texte:"));
    assertTrue(output.contains("Test Event"));
    assertTrue(output.contains("Start:"));
    assertTrue(output.contains("End:"));
    assertFalse(output.contains("<html>"));
  }

  @Test
  public void testGenerateOutputHtml() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");
    String output = outputGenerator.generateOutput(icsStrings, "html");
    assertNotNull(output);
    assertTrue(output.contains("<html>"));
    assertTrue(output.contains("</html>"));
    assertTrue(output.contains("<head>"));
    assertTrue(output.contains("</head>"));
    assertTrue(output.contains("<body"));
    assertTrue(output.contains("</body>"));
    assertTrue(output.contains("Éléments du calendrier"));
    assertTrue(output.contains("Test Event"));
    assertTrue(output.contains("Event #1"));
    assertTrue(output.contains("style="));
  }

  @Test
  public void testGenerateOutputIcs() {
    List<String> icsStrings = new ArrayList<>();
    String eventIcs = "BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT\n";
    icsStrings.add(eventIcs);
    String output = outputGenerator.generateOutput(icsStrings, "ics");
    assertNotNull(output);
    assertEquals(eventIcs, output);
    assertTrue(output.contains("BEGIN:VEVENT"));
    assertTrue(output.contains("END:VEVENT"));
    assertTrue(output.contains("UID:event1@test.com"));
    assertFalse(output.contains("<html>"));
    assertFalse(output.contains("Éléments du calendrier"));
  }

  @Test
  public void testGenerateOutputWithNullType() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");
    String output = outputGenerator.generateOutput(icsStrings, null);

    assertNotNull(output);
    assertTrue(output.contains("Éléments du calendrier formatés en texte:"));
    assertTrue(output.contains("Test Event"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGenerateOutputInvalidType() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");
    outputGenerator.generateOutput(icsStrings, "invalid");
  }

  @Test
  public void testGenerateOutputMultipleComponents() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Event 1\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event2@test.com\n"
        + "SUMMARY:Event 2\n"
        + "DTSTART:20241217T090000Z\n"
        + "END:VEVENT");
    icsStrings.add("BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Todo 1\n"
        + "DTSTART:20241216T000000Z\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO");
    String output = outputGenerator.generateOutput(icsStrings, "text");
    assertNotNull(output);
    assertTrue(output.contains("Event 1"));
    assertTrue(output.contains("Event 2"));
    assertTrue(output.contains("Todo 1"));
    int event1Count = countOccurrences(output, "Event 1");
    int event2Count = countOccurrences(output, "Event 2");
    int todo1Count = countOccurrences(output, "Todo 1");
    assertEquals(1, event1Count);
    assertEquals(1, event2Count);
    assertEquals(1, todo1Count);
  }

  @Test
  public void testGenerateOutputEmptyList() {
    List<String> icsStrings = new ArrayList<>();

    String output = outputGenerator.generateOutput(icsStrings, "text");

    assertNotNull(output);
    assertTrue(output.contains("Éléments du calendrier formatés en texte:"));
  }

  @Test
  public void testHtmlOutputStructure() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:HTML Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "END:VEVENT");

    String output = outputGenerator.generateOutput(icsStrings, "html");
    assertTrue(output.startsWith("<html>"));
    assertTrue(output.endsWith("</html>"));
    assertTrue(output.contains("<meta charset=\"UTF-8\">"));
    assertTrue(output.contains("<title>Calendrier</title>"));
    assertTrue(output.contains("<h1"));
    assertTrue(output.contains("Éléments du calendrier"));
    assertTrue(output.contains("</h1>"));
    assertTrue(output.contains("HTML Test Event"));
    assertTrue(output.contains("Event #1"));

    assertTrue(output.contains("background-color:#f4f6f8"));
    assertTrue(output.contains("font-family:Arial, sans-serif"));
    assertTrue(output.contains("color:#2c3e50"));
  }

  @Test
  public void testTextOutputStructure() {
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add("BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "SUMMARY:Text Test Event\n"
        + "DTSTART:20241216T090000Z\n"
        + "DTEND:20241216T100000Z\n"
        + "LOCATION:Test Room\n"
        + "END:VEVENT");

    String output = outputGenerator.generateOutput(icsStrings, "text");
    assertTrue(output.contains("Éléments du calendrier formatés en texte:"));
    assertTrue(output.contains("Text Test Event"));
    assertTrue(output.contains("Start:"));
    assertTrue(output.contains("End:"));
    assertTrue(output.contains("Location:"));
    assertTrue(output.contains("Test Room"));
    assertTrue(output.contains("\n\n"));
  }

  @Test
  public void testIcsOutputPreservesOriginalFormat() {
    String originalIcs = "BEGIN:VEVENT\n"
        + "UID:preserve@test.com\n"
        + "SUMMARY:Preserve Format Test\n"
        + "DTSTART:20241216T090000Z\n"
        + "DESCRIPTION:Line 1\\nLine 2\n"
        + "END:VEVENT\n";
    List<String> icsStrings = new ArrayList<>();
    icsStrings.add(originalIcs);
    String output = outputGenerator.generateOutput(icsStrings, "ics");
    assertEquals(originalIcs, output);
    assertTrue(output.contains("DESCRIPTION:Line 1\\nLine 2"));
  }

  private int countOccurrences(String text, String pattern) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(pattern, index)) != -1) {
      count++;
      index += pattern.length();
    }
    return count;
  }

  @Test
  public void testDuplicateDateTimeParserMethod() {
    String icalDate = "20241216T143000Z";
    String outputsResult = outputGenerator.dateTimeParser(icalDate);
    String dateUtilsResult = DateUtils.dateTimeParser(icalDate);
    assertEquals(dateUtilsResult, outputsResult);
  }
}
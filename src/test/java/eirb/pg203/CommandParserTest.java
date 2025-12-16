import static org.junit.Assert.*;

import org.junit.Test;

public class CommandParserTest {
  @Test
  public void testBasicEventParsing() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-text"};
    parser.parseCommandLine(args);

    assertEquals("calendar.ics", parser.getSourceFile());
    assertEquals("events", parser.getEventType());
    assertEquals("text", parser.getOutputType());
    assertEquals("output", parser.getOutputFile());
    assertEquals("today", parser.getDateTag());
    assertEquals("all", parser.getTodoStatus());
  }

  @Test
  public void testBasicTodoParsing() {
    CommandParser parser = new CommandParser();
    String[] args = {"tasks.ics", "todos", "-text"};
    parser.parseCommandLine(args);

    assertEquals("tasks.ics", parser.getSourceFile());
    assertEquals("todos", parser.getEventType());
    assertEquals("incomplete", parser.getTodoStatus());
  }

  @Test
  public void testOutputFileOption() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-o", "myoutput.txt", "-html"};
    parser.parseCommandLine(args);

    assertEquals("myoutput.txt", parser.getOutputFile());
    assertEquals("html", parser.getOutputType());
  }

  @Test
  public void testEventDateOptions() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-tomorrow", "-ics"};
    parser.parseCommandLine(args);

    assertEquals("tomorrow", parser.getDateTag());
    assertEquals("ics", parser.getOutputType());
  }

  @Test
  public void testFromToDates() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-from", "2024-01-01", "-to", "2024-01-31"};
    parser.parseCommandLine(args);

    assertEquals("from_to", parser.getDateTag());
    assertEquals("2024-01-01", parser.getFromDate());
    assertEquals("2024-01-31", parser.getToDate());
  }

  @Test
  public void testTodoStatusOptions() {
    CommandParser parser = new CommandParser();
    String[] args = {"tasks.ics", "todos", "-completed", "-text"};
    parser.parseCommandLine(args);

    assertEquals("completed", parser.getTodoStatus());
  }

  @Test
  public void testMissingArguments() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics"};
    parser.parseCommandLine(args);
  }

  @Test
  public void testInvalidEventType() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "invalid"};
    parser.parseCommandLine(args);
  }

  @Test
  public void testDateOptionWithTodos() {
    CommandParser parser = new CommandParser();
    String[] args = {"tasks.ics", "todos", "-today"};
    parser.parseCommandLine(args);
  }

  @Test
  public void testTodoOptionWithEvents() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-completed"};
    parser.parseCommandLine(args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleDateOptions() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-today", "-week"};
    parser.parseCommandLine(args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromWithoutDate() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-from"};
    parser.parseCommandLine(args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToWithoutDate() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-to"};
    parser.parseCommandLine(args);
  }

  @Test
  public void testTokenizeMethod() {
    CommandParser parser = new CommandParser();
    String commandLine = "clical calendar.ics events -today -text -o output.txt";
    java.util.List<String> tokens = parser.tokenize(commandLine);

    assertEquals(7, tokens.size());
    assertEquals("clical", tokens.get(0));
    assertEquals("calendar.ics", tokens.get(1));
    assertEquals("events", tokens.get(2));
    assertEquals("-today", tokens.get(3));
    assertEquals("-text", tokens.get(4));
    assertEquals("-o", tokens.get(5));
    assertEquals("output.txt", tokens.get(6));
  }

  @Test
  public void testPrintState() {
    CommandParser parser = new CommandParser();
    String[] args = {"calendar.ics", "events", "-week", "-html", "-o", "schedule.html"};
    parser.parseCommandLine(args);

    // Cette méthode imprime simplement, on vérifie qu'elle ne lance pas d'exception
    parser.printState();
  }

  @Test
  public void testSettersAndGetters() {
    CommandParser parser = new CommandParser();

    parser.setEventType("events");
    parser.setOutputType("ics");
    parser.setOutputFile("test.ics");
    parser.setSourceFile("input.ics");
    parser.setDateTag("tomorrow");
    parser.setFromDate("2024-01-01");
    parser.setToDate("2024-01-31");
    parser.setTodoStatus("incomplete");

    assertEquals("events", parser.getEventType());
    assertEquals("ics", parser.getOutputType());
    assertEquals("test.ics", parser.getOutputFile());
    assertEquals("input.ics", parser.getSourceFile());
    assertEquals("tomorrow", parser.getDateTag());
    assertEquals("2024-01-01", parser.getFromDate());
    assertEquals("2024-01-31", parser.getToDate());
    assertEquals("incomplete", parser.getTodoStatus());
  }

  @Test
  public void testConstructorDefaults() {
    CommandParser parser = new CommandParser();

    assertEquals("text", parser.getOutputType());
    assertEquals("output", parser.getOutputFile());
    assertEquals("today", parser.getDateTag());
    assertEquals("all", parser.getTodoStatus());
    assertNull(parser.getSourceFile());
    assertNull(parser.getEventType());
    assertNull(parser.getFromDate());
    assertNull(parser.getToDate());
  }
}
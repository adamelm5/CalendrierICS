package eirb.pg203;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FilterIcsEventsTest {
  private String testIcsFile;

  @Before
  public void setUp() throws IOException {
    testIcsFile = "test_calendar.ics";

    String icsContent = "BEGIN:VCALENDAR\n"
        + "VERSION:2.0\n"
        + "PRODID:-//Test//EN\n"
        + "\n"
        + "BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241216T090000Z\n"
        + "DTEND:20241216T100000Z\n"
        + "SUMMARY:Meeting Today\n"
        + "LOCATION:Office\n"
        + "END:VEVENT\n"
        + "\n"
        + "BEGIN:VEVENT\n"
        + "UID:event2@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241217T140000Z\n"
        + "DTEND:20241217T150000Z\n"
        + "SUMMARY:Meeting Tomorrow\n"
        + "LOCATION:Remote\n"
        + "END:VEVENT\n"
        + "\n"
        + "BEGIN:VEVENT\n"
        + "UID:event3@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241218T100000Z\n"
        + "DTEND:20241218T110000Z\n"
        + "SUMMARY:Mid Week Meeting\n"
        + "LOCATION:Conference Room\n"
        + "END:VEVENT\n"
        + "\n"
        + "BEGIN:VEVENT\n"
        + "UID:event4@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241222T160000Z\n"
        + "DTEND:20241222T170000Z\n"
        + "SUMMARY:End of Week Meeting\n"
        + "LOCATION:Home\n"
        + "END:VEVENT\n"
        + "\n"
        + "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20241216T000000Z\n"
        + "DUE:20241220T000000Z\n"
        + "SUMMARY:Complete Report\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO\n"
        + "\n"
        + "BEGIN:VEVENT\n"
        + "UID:event5@test.com\n"
        + "DTSTAMP:20241216T120000Z\n"
        + "DTSTART:20240101T000000Z\n"
        + "DTEND:20240101T010000Z\n"
        + "SUMMARY:New Year Event\n"
        + "LOCATION:Party\n"
        + "END:VEVENT\n"
        + "\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(testIcsFile)) {
      writer.write(icsContent);
    }
  }

  @Test
  public void testGetAttributeName() {
    assertEquals("SUMMARY", FilterIcsEvents.getAttributeName("SUMMARY:Meeting"));
    assertEquals("DTSTART", FilterIcsEvents.getAttributeName("DTSTART:20241216T090000Z"));
    assertEquals("BEGIN", FilterIcsEvents.getAttributeName("BEGIN:VEVENT"));
    assertEquals("LOCATION", FilterIcsEvents.getAttributeName("LOCATION:Office"));
  }

  @Test
  public void testGetAttributeValue() {
    assertEquals("Meeting", FilterIcsEvents.getAtrributeValue("SUMMARY:Meeting"));
    assertEquals("20241216T090000Z", FilterIcsEvents.getAtrributeValue("DTSTART:20241216T090000Z"));
    assertEquals("VEVENT", FilterIcsEvents.getAtrributeValue("BEGIN:VEVENT"));
    assertEquals("Office", FilterIcsEvents.getAtrributeValue("LOCATION:Office"));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testGetAttributeNameNoColon() {
    FilterIcsEvents.getAttributeName("INVALIDLINE");
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testGetAttributeValueNoColon() {
    FilterIcsEvents.getAtrributeValue("INVALIDLINE");
  }

  @Test
  public void testExtractTodayEventsICS() {
    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testIcsFile);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("BEGIN:VEVENT"));
    assertTrue(events.get(0).contains("Meeting Today"));
    assertTrue(events.get(0).contains("END:VEVENT"));
    assertFalse(events.get(0).contains("Meeting Tomorrow"));
  }

  @Test
  public void testExtractTodayEventsICSEmpty() {
    String date = "01/01/2023";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testIcsFile);

    assertTrue(events.isEmpty());
  }

  @Test
  public void testExtractTodayEventsICSWithTodos() {
    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testIcsFile);
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("VEVENT"));
    assertFalse(events.get(0).contains("VTODO"));
  }

  @Test
  public void testExtractTomorrowEventsICS() {
    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTomorrowEventsICS(date, testIcsFile);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("BEGIN:VEVENT"));
    assertTrue(events.get(0).contains("Meeting Tomorrow"));
    assertTrue(events.get(0).contains("END:VEVENT"));
    assertFalse(events.get(0).contains("Meeting Today"));
  }

  @Test
  public void testExtractWeekEventsICS() {
    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractWeekEventsICS(date, testIcsFile);

    assertEquals(4, events.size());

    int countToday = 0;
    int countTomorrow = 0;
    int countMidWeek = 0;
    int countEndWeek = 0;

    for (String event : events) {
      if (event.contains("Meeting Today"))
        countToday++;
      if (event.contains("Meeting Tomorrow"))
        countTomorrow++;
      if (event.contains("Mid Week Meeting"))
        countMidWeek++;
      if (event.contains("End of Week Meeting"))
        countEndWeek++;
    }

    assertEquals(1, countToday);
    assertEquals(1, countTomorrow);
    assertEquals(1, countMidWeek);
    assertEquals(1, countEndWeek);
  }

  @Test
  public void testExtractBetweenDatesEventsICS() {
    String fromDate = "17/12/2024";
    String toDate = "18/12/2024";
    List<String> events =
        FilterIcsEvents.extractBetweenDatesEventsICS(fromDate, toDate, testIcsFile);

    assertEquals(2, events.size());

    boolean hasTomorrow = false;
    boolean hasMidWeek = false;

    for (String event : events) {
      if (event.contains("Meeting Tomorrow"))
        hasTomorrow = true;
      if (event.contains("Mid Week Meeting"))
        hasMidWeek = true;
    }

    assertTrue(hasTomorrow);
    assertTrue(hasMidWeek);
    assertFalse(events.stream().anyMatch(e -> e.contains("Meeting Today")));
  }

  @Test
  public void testExtractBetweenDatesEventsICSSingleDay() {
    String fromDate = "16/12/2024";
    String toDate = "16/12/2024";
    List<String> events =
        FilterIcsEvents.extractBetweenDatesEventsICS(fromDate, toDate, testIcsFile);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Meeting Today"));
  }

  @Test
  public void testExtractBetweenDatesEventsICSEmptyRange() {
    String fromDate = "01/01/2023";
    String toDate = "01/01/2023";
    List<String> events =
        FilterIcsEvents.extractBetweenDatesEventsICS(fromDate, toDate, testIcsFile);

    assertTrue(events.isEmpty());
  }

  @Test
  public void testExtractEventsICSToday() {
    String todayDate = "16/12/2024";
    List<String> events =
        FilterIcsEvents.extractEventsICS(todayDate, "today", testIcsFile, null, null);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Meeting Today"));
  }

  @Test
  public void testExtractEventsICSTomorrow() {
    String todayDate = "16/12/2024";
    List<String> events =
        FilterIcsEvents.extractEventsICS(todayDate, "tomorrow", testIcsFile, null, null);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Meeting Tomorrow"));
  }

  @Test
  public void testExtractEventsICSWeek() {
    String todayDate = "16/12/2024";
    List<String> events =
        FilterIcsEvents.extractEventsICS(todayDate, "week", testIcsFile, null, null);

    assertEquals(4, events.size());
  }

  @Test
  public void testExtractEventsICSFromTo() {
    String todayDate = "16/12/2024";
    List<String> events = FilterIcsEvents.extractEventsICS(
        todayDate, "from_to", testIcsFile, "17/12/2024", "18/12/2024");

    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractEventsICSInvalidOption() {
    String todayDate = "16/12/2024";
    FilterIcsEvents.extractEventsICS(todayDate, "invalid", testIcsFile, null, null);
  }

  @Test
  public void testFileNotFound() {
    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, "nonexistent.ics");

    assertNotNull(events);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testEmptyFile() throws IOException {
    String emptyFile = "empty.ics";
    try (FileWriter writer = new FileWriter(emptyFile)) {
      writer.write("");
    }

    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, emptyFile);

    assertTrue(events.isEmpty());

    new File(emptyFile).delete();
  }

  @Test
  public void testEventWithoutDtStart() throws IOException {
    String testFile = "no_dtstart.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VEVENT\n"
        + "UID:no_dtstart@test.com\n"
        + "SUMMARY:Event without DTSTART\n"
        + "END:VEVENT\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write(icsContent);
    }

    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testFile);

    assertTrue(events.isEmpty());

    new File(testFile).delete();
  }

  @Test
  public void testMultipleEventsSameDay() throws IOException {
    String testFile = "multiple_events.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "DTSTART:20241216T090000Z\n"
        + "SUMMARY:Morning Meeting\n"
        + "END:VEVENT\n"
        + "BEGIN:VEVENT\n"
        + "UID:event2@test.com\n"
        + "DTSTART:20241216T140000Z\n"
        + "SUMMARY:Afternoon Meeting\n"
        + "END:VEVENT\n"
        + "BEGIN:VEVENT\n"
        + "UID:event3@test.com\n"
        + "DTSTART:20241217T090000Z\n"
        + "SUMMARY:Next Day Meeting\n"
        + "END:VEVENT\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write(icsContent);
    }

    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testFile);

    assertEquals(2, events.size());

    int morningCount = 0;
    int afternoonCount = 0;
    int nextDayCount = 0;

    for (String event : events) {
      if (event.contains("Morning Meeting"))
        morningCount++;
      if (event.contains("Afternoon Meeting"))
        afternoonCount++;
      if (event.contains("Next Day Meeting"))
        nextDayCount++;
    }

    assertEquals(1, morningCount);
    assertEquals(1, afternoonCount);
    assertEquals(0, nextDayCount);

    new File(testFile).delete();
  }

  @Test
  public void testEventWithMultilineDescription() throws IOException {
    String testFile = "multiline.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VEVENT\n"
        + "UID:multiline@test.com\n"
        + "DTSTART:20241216T090000Z\n"
        + "SUMMARY:Meeting with Description\n"
        + "DESCRIPTION:Line 1\\nLine 2\\nLine 3\n"
        + "END:VEVENT\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(testFile)) {
      writer.write(icsContent);
    }

    String date = "16/12/2024";
    List<String> events = FilterIcsEvents.extractTodayEventsICS(date, testFile);

    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("DESCRIPTION:Line 1\\nLine 2\\nLine 3"));

    new File(testFile).delete();
  }

  @org.junit.After
  public void tearDown() {
    File file = new File(testIcsFile);
    if (file.exists()) {
      file.delete();
    }
  }
}
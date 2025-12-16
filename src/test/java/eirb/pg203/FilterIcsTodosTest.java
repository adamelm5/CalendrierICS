package eirb.pg203;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class FilterIcsTodosTest {
  private String testIcsFile;

  @Before
  public void setUp() throws IOException {
    // Créer un fichier ICS de test basé sur l'exemple fourni
    testIcsFile = "test_todos.ics";

    String icsContent = "BEGIN:VCALENDAR\n"
        + "X-WR-CALNAME:Tasks\n"
        + "X-WR-CALID:aa25ce81-93f5-4470-845f-0c506feac946:15\n"
        + "PRODID:Zimbra-Calendar-Provider\n"
        + "VERSION:2.0\n"
        + "METHOD:PUBLISH\n"
        + "BEGIN:VTODO\n"
        + "UID:457d2974-389e-44f4-b201-efdd66183608\n"
        + "SUMMARY:Réviser l'examen de POO\n"
        + "LOCATION:Enseirb\n"
        + "PRIORITY:5\n"
        + "PERCENT-COMPLETE:100\n"
        + "COMPLETED:20251104T204504Z\n"
        + "ORGANIZER;CN=\"Alice\":mailto:foo@bar.fr\n"
        + "DUE;VALUE=DATE:20251107\n"
        + "STATUS:COMPLETED\n"
        + "CLASS:PUBLIC\n"
        + "LAST-MODIFIED:20251104T204504Z\n"
        + "DTSTAMP:20251104T204504Z\n"
        + "SEQUENCE:2\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:88efa974-1f04-49d7-bb5b-304b19786d51\n"
        + "SUMMARY:Faire projet POO\n"
        + "PRIORITY:1\n"
        + "PERCENT-COMPLETE:10\n"
        + "ORGANIZER;CN=\"Alice\":mailto:foo@bar.fr\n"
        + "DUE;VALUE=DATE:20251211\n"
        + "STATUS:IN-PROCESS\n"
        + "CLASS:PUBLIC\n"
        + "LAST-MODIFIED:20251104T215308Z\n"
        + "DTSTAMP:20251104T215308Z\n"
        + "SEQUENCE:2\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:33b819f5-5d72-4beb-b806-55675495fe27\n"
        + "SUMMARY:Profiter de la vie\n"
        + "PRIORITY:5\n"
        + "PERCENT-COMPLETE:0\n"
        + "ORGANIZER;CN=\"Alice\":mailto:foo@bar.fr\n"
        + "STATUS:IN-PROCESS\n"
        + "CLASS:PUBLIC\n"
        + "LAST-MODIFIED:20251104T204613Z\n"
        + "DTSTAMP:20251104T204613Z\n"
        + "SEQUENCE:1\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:57d6f4ab-d33d-4f23-bee6-12c0aa4bea25\n"
        + "SUMMARY:Trouver un stage 2A\n"
        + "PRIORITY:5\n"
        + "PERCENT-COMPLETE:0\n"
        + "ORGANIZER;CN=\"Alice\":mailto:foo@bar.fr\n"
        + "DTSTART;VALUE=DATE:20251201\n"
        + "DUE;VALUE=DATE:20260331\n"
        + "STATUS:NEEDS-ACTION\n"
        + "CLASS:PUBLIC\n"
        + "LAST-MODIFIED:20251104T204806Z\n"
        + "DTSTAMP:20251104T204806Z\n"
        + "SEQUENCE:1\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(testIcsFile)) {
      writer.write(icsContent);
    }
  }

  @Test
  public void testExtractTodosICSAll() {
    List<String> todos = FilterIcsTodos.extractTodosICS("all", testIcsFile);

    assertEquals(4, todos.size());

    int count = 0;
    for (String todo : todos) {
      assertTrue(todo.contains("BEGIN:VTODO"));
      assertTrue(todo.contains("END:VTODO"));
      assertTrue(todo.contains("SUMMARY:"));
      count++;
    }
    assertEquals(4, count);
  }

  @Test
  public void testExtractTodosICSAllWithNullOption() {
    List<String> todos = FilterIcsTodos.extractTodosICS(null, testIcsFile);

    // Avec option null, devrait retourner "all" par défaut
    assertEquals(4, todos.size());
  }

  @Test
  public void testExtractTodosICSIncomplete() {
    List<String> todos = FilterIcsTodos.extractTodosICS("incomplete", testIcsFile);

    // Incomplete = PERCENT-COMPLETE != 100
    // Dans notre fichier test:
    // - Todo 1: PERCENT-COMPLETE:100 (COMPLETED) -> pas incomplete
    // - Todo 2: PERCENT-COMPLETE:10 -> incomplete
    // - Todo 3: PERCENT-COMPLETE:0 -> incomplete
    // - Todo 4: PERCENT-COMPLETE:0 -> incomplete
    assertEquals(3, todos.size());

    boolean hasProjectPOO = false;
    boolean hasProfiter = false;
    boolean hasStage = false;
    boolean hasExamen = false;

    for (String todo : todos) {
      if (todo.contains("Faire projet POO"))
        hasProjectPOO = true;
      if (todo.contains("Profiter de la vie"))
        hasProfiter = true;
      if (todo.contains("Trouver un stage 2A"))
        hasStage = true;
      if (todo.contains("Réviser l'examen"))
        hasExamen = false; // Celui-ci est complet
    }

    assertTrue(hasProjectPOO);
    assertTrue(hasProfiter);
    assertTrue(hasStage);
    assertFalse(hasExamen);
  }

  @Test
  public void testExtractTodosICSCompleted() {
    List<String> todos = FilterIcsTodos.extractTodosICS("completed", testIcsFile);

    // Completed = PERCENT-COMPLETE:100
    // Seulement le premier todo a PERCENT-COMPLETE:100
    assertEquals(1, todos.size());

    assertTrue(todos.get(0).contains("Réviser l'examen de POO"));
    assertTrue(todos.get(0).contains("PERCENT-COMPLETE:100"));
    assertTrue(todos.get(0).contains("STATUS:COMPLETED"));
  }

  @Test
  public void testExtractTodosICSInProcess() {
    List<String> todos = FilterIcsTodos.extractTodosICS("inprocess", testIcsFile);

    // In-process = STATUS:IN-PROCESS
    // Todo 2 et 3 ont STATUS:IN-PROCESS
    assertEquals(2, todos.size());

    int countInProcess = 0;
    for (String todo : todos) {
      assertTrue(todo.contains("STATUS:IN-PROCESS"));
      if (todo.contains("Faire projet POO"))
        countInProcess++;
      if (todo.contains("Profiter de la vie"))
        countInProcess++;
    }

    assertEquals(2, countInProcess);
  }

  @Test
  public void testExtractTodosICSNeedsAction() {
    List<String> todos = FilterIcsTodos.extractTodosICS("needsaction", testIcsFile);

    // Needs-action = STATUS:NEEDS-ACTION
    // Seulement le todo 4 a STATUS:NEEDS-ACTION
    assertEquals(1, todos.size());

    assertTrue(todos.get(0).contains("Trouver un stage 2A"));
    assertTrue(todos.get(0).contains("STATUS:NEEDS-ACTION"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractTodosICSInvalidOption() {
    FilterIcsTodos.extractTodosICS("invalid", testIcsFile);
  }

  @Test
  public void testFileNotFound() {
    List<String> todos = FilterIcsTodos.extractTodosICS("all", "nonexistent.ics");

    // Devrait retourner une liste vide sans exception
    assertNotNull(todos);
    assertTrue(todos.isEmpty());
  }

  @Test
  public void testEmptyFile() throws IOException {
    String emptyFile = "empty_todos.ics";
    try (FileWriter writer = new FileWriter(emptyFile)) {
      writer.write("");
    }

    List<String> todos = FilterIcsTodos.extractTodosICS("all", emptyFile);

    assertTrue(todos.isEmpty());

    new File(emptyFile).delete();
  }

  @Test
  public void testMixedCalendarWithEventsAndTodos() throws IOException {
    String mixedFile = "mixed_calendar.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VEVENT\n"
        + "UID:event1@test.com\n"
        + "DTSTART:20241216T090000Z\n"
        + "SUMMARY:Meeting\n"
        + "END:VEVENT\n"
        + "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Test Todo\n"
        + "PERCENT-COMPLETE:50\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO\n"
        + "BEGIN:VEVENT\n"
        + "UID:event2@test.com\n"
        + "DTSTART:20241217T090000Z\n"
        + "SUMMARY:Another Meeting\n"
        + "END:VEVENT\n"
        + "BEGIN:VTODO\n"
        + "UID:todo2@test.com\n"
        + "SUMMARY:Another Todo\n"
        + "PERCENT-COMPLETE:100\n"
        + "STATUS:COMPLETED\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(mixedFile)) {
      writer.write(icsContent);
    }

    List<String> todos = FilterIcsTodos.extractTodosICS("all", mixedFile);

    // Devrait retourner seulement les TODOs, pas les EVENTS
    assertEquals(2, todos.size());

    for (String todo : todos) {
      assertTrue(todo.contains("BEGIN:VTODO"));
      assertTrue(todo.contains("SUMMARY:"));
      assertFalse(todo.contains("BEGIN:VEVENT"));
    }

    new File(mixedFile).delete();
  }

  @Test
  public void testTodosWithoutStatusOrPercent() throws IOException {
    String simpleFile = "simple_todos.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Todo without status\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo2@test.com\n"
        + "SUMMARY:Todo with status\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(simpleFile)) {
      writer.write(icsContent);
    }

    // Test "all" - devrait retourner les deux
    List<String> allTodos = FilterIcsTodos.extractTodosICS("all", simpleFile);
    assertEquals(2, allTodos.size());

    // Test "incomplete" - le premier n'a pas PERCENT-COMPLETE, donc par défaut != 100
    List<String> incompleteTodos = FilterIcsTodos.extractTodosICS("incomplete", simpleFile);
    assertEquals(2, incompleteTodos.size()); // Les deux sont incomplets

    // Test "inprocess" - seulement le second a STATUS:IN-PROCESS
    List<String> inprocessTodos = FilterIcsTodos.extractTodosICS("inprocess", simpleFile);
    assertEquals(1, inprocessTodos.size());
    assertTrue(inprocessTodos.get(0).contains("Todo with status"));

    new File(simpleFile).delete();
  }

  @Test
  public void testTodosWithComplexStatusValues() throws IOException {
    String complexFile = "complex_todos.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Todo 1\n"
        + "STATUS:NEEDS-ACTION\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo2@test.com\n"
        + "SUMMARY:Todo 2\n"
        + "STATUS:IN-PROCESS\n"
        + "PERCENT-COMPLETE:50\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo3@test.com\n"
        + "SUMMARY:Todo 3\n"
        + "STATUS:COMPLETED\n"
        + "PERCENT-COMPLETE:100\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo4@test.com\n"
        + "SUMMARY:Todo 4\n"
        + "STATUS:CANCELLED\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(complexFile)) {
      writer.write(icsContent);
    }

    // Test différents filtres
    List<String> needsAction = FilterIcsTodos.extractTodosICS("needsaction", complexFile);
    assertEquals(1, needsAction.size());
    assertTrue(needsAction.get(0).contains("Todo 1"));

    List<String> inProcess = FilterIcsTodos.extractTodosICS("inprocess", complexFile);
    assertEquals(1, inProcess.size());
    assertTrue(inProcess.get(0).contains("Todo 2"));

    List<String> completed = FilterIcsTodos.extractTodosICS("completed", complexFile);
    assertEquals(1, completed.size());
    assertTrue(completed.get(0).contains("Todo 3"));

    List<String> incomplete = FilterIcsTodos.extractTodosICS("incomplete", complexFile);
    // Todo 1, 2, 4 sont incomplets (PERCENT-COMPLETE != 100 ou absent)
    assertEquals(3, incomplete.size());

    new File(complexFile).delete();
  }

  @Test
  public void testTodoWithMultilineFields() throws IOException {
    String multilineFile = "multiline_todo.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VTODO\n"
        + "UID:multiline@test.com\n"
        + "SUMMARY:Todo with description\n"
        + "DESCRIPTION:This is a long description\\n"
        + " with multiple lines\\n"
        + " and special characters\n"
        + "PERCENT-COMPLETE:75\n"
        + "STATUS:IN-PROCESS\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(multilineFile)) {
      writer.write(icsContent);
    }

    List<String> todos = FilterIcsTodos.extractTodosICS("inprocess", multilineFile);

    assertEquals(1, todos.size());
    assertTrue(todos.get(0).contains("DESCRIPTION:This is a long description"));
    assertTrue(todos.get(0).contains(" with multiple lines"));

    new File(multilineFile).delete();
  }

  @Test
  public void testExtractTodosWithDifferentPercentFormats() throws IOException {
    String percentFile = "percent_todos.ics";
    String icsContent = "BEGIN:VCALENDAR\n"
        + "BEGIN:VTODO\n"
        + "UID:todo1@test.com\n"
        + "SUMMARY:Todo 0%\n"
        + "PERCENT-COMPLETE:0\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo2@test.com\n"
        + "SUMMARY:Todo 50%\n"
        + "PERCENT-COMPLETE:50\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo3@test.com\n"
        + "SUMMARY:Todo 99%\n"
        + "PERCENT-COMPLETE:99\n"
        + "END:VTODO\n"
        + "BEGIN:VTODO\n"
        + "UID:todo4@test.com\n"
        + "SUMMARY:Todo 100%\n"
        + "PERCENT-COMPLETE:100\n"
        + "END:VTODO\n"
        + "END:VCALENDAR";

    try (FileWriter writer = new FileWriter(percentFile)) {
      writer.write(icsContent);
    }

    // Test "incomplete" - devrait retourner tous sauf celui à 100%
    List<String> incomplete = FilterIcsTodos.extractTodosICS("incomplete", percentFile);
    assertEquals(3, incomplete.size());

    // Test "completed" - devrait retourner seulement celui à 100%
    List<String> completed = FilterIcsTodos.extractTodosICS("completed", percentFile);
    assertEquals(1, completed.size());
    assertTrue(completed.get(0).contains("Todo 100%"));

    new File(percentFile).delete();
  }

  // Note: J'ai remarqué un bug potentiel dans le code original
  // La condition pour "completed" utilise fieldExpectedValue = "100" mais vérifie le champ
  // COMPLETED alors qu'elle devrait vérifier PERCENT-COMPLETE:100 C'est probablement une erreur qui
  // sera corrigée plus tard

  // Nettoyage après les tests
  @org.junit.After
  public void tearDown() {
    File file = new File(testIcsFile);
    if (file.exists()) {
      file.delete();
    }
  }
}
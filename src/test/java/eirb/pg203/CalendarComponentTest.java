package eirb.pg203;

public class CalendarComponentTest {
  public static void main(String[] args) {
    System.out.println("=== Test de la classe abstraite CalendarComponent ===");

    // Test des classes concrètes
    testEvent();
    testTodo();

    System.out.println("\n=== Tous les tests passés avec succès ===");
  }

  private static void testEvent() {
    System.out.println("\n--- Test de la classe Event ---");

    // Création d'un événement
    Event event = new Event("uid-test-123", "Test Meeting", "Test Room", "20240101T120000Z", 1,
        "20240101T120000Z", "20240110T090000Z", "20240110T100000Z", "Description with \\n newline",
        "20231231T110000Z");

    // Test des getters de la classe parent
    assert "uid-test-123".equals(event.getUid()) : "getUid() échoué";
    assert "Test Meeting".equals(event.getSummary()) : "getSummary() échoué";
    assert "Test Room".equals(event.getLocation()) : "getLocation() échoué";
    assert "20240101T120000Z".equals(event.getLastModified()) : "getLastModified() échoué";
    assert event.getSequence() == 1 : "getSequence() échoué";
    assert "20240110T090000Z".equals(event.getDtStart()) : "getDtStart() échoué";

    // Test des getters spécifiques à Event
    assert "20240110T100000Z".equals(event.getDtEnd()) : "getDtEnd() échoué";
    assert "Description with \\n newline".equals(event.getDescription())
        : "getDescription() échoué";
    assert "20231231T110000Z".equals(event.getCreated()) : "getCreated() échoué";

    // Test de getType()
    assert "events".equals(event.getType()) : "getType() échoué pour Event";

    // Test de toString()
    String toStringResult = event.toString();
    assert toStringResult.contains("Start: 20240110T090000Z")
        : "toString() doit contenir la date de début";
    assert toStringResult.contains("Summary: Test Meeting") : ("toString() doit contenir le "
                                                               + "summary");

    System.out.println("toString() résultat:\n" + event);
    System.out.println("✓ Test Event réussi");
  }

  private static void testTodo() {
    System.out.println("\n--- Test de la classe Todo ---");

    // Création d'un todo
    Todo todo = new Todo("uid-todo-456", "Test Task", "Home Office", "20240102T130000Z", 2,
        "20240102T130000Z", "20240115T170000Z", "20240115T170000Z", "IN-PROCESS", 50, 3, "",
        "MAILTO:organizer@test.com", "PRIVATE");

    // Test des getters de la classe parent
    assert "uid-todo-456".equals(todo.getUid()) : "getUid() échoué pour Todo";
    assert "Test Task".equals(todo.getSummary()) : "getSummary() échoué pour Todo";
    assert 2 == todo.getSequence() : "getSequence() échoué pour Todo";

    // Test des getters spécifiques à Todo
    assert "20240115T170000Z".equals(todo.getDueDate()) : "getDueDate() échoué";
    assert "IN-PROCESS".equals(todo.getStatus()) : "getStatus() échoué";
    assert todo.getPercentComplete() == 50 : "getPercentComplete() échoué";
    assert todo.getPriority() == 3 : "getPriority() échoué";
    assert "".equals(todo.getCompleted()) : "getCompleted() échoué";
    assert "MAILTO:organizer@test.com".equals(todo.getOrganizer()) : "getOrganizer() échoué";
    assert "PRIVATE".equals(todo.getClassification()) : "getClassification() échoué";

    // Test de getType()
    assert "todos".equals(todo.getType()) : "getType() échoué pour Todo";

    // Test des setters
    todo.setPercentComplete(75);
    assert todo.getPercentComplete() == 75 : "setPercentComplete() échoué";

    todo.setPriority(1);
    assert todo.getPriority() == 1 : "setPriority() échoué";

    todo.setCompleted("20240116T100000Z");
    assert "20240116T100000Z".equals(todo.getCompleted()) : "setCompleted() échoué";

    // Test de toString()
    String toStringResult = todo.toString();
    assert toStringResult.contains("Summary: Test Task") : "toString() doit contenir le summary";
    assert toStringResult.contains("Status: IN-PROCESS") : "toString() doit contenir le status";
    assert toStringResult.contains("Percent Complete: 75%")
        : "toString() doit contenir le pourcentage";

    System.out.println("toString() résultat:\n" + todo);
    System.out.println("✓ Test Todo réussi");
  }
}
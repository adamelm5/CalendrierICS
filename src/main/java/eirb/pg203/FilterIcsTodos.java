import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FilterIcsTodos {
  /**
    TODO :
    - incomplete - all - completed - inprocess - needsaction
  */

  public static List<String> extractTodosICS(
      String option, String filePath) /*throws IllegalArgumentException*/ {
    if (option == null) {
      option = "all";
    }
    if (!option.equals("incomplete") && !option.equals("all") && !option.equals("completed")
        && !option.equals("inprocess") && !option.equals("needsaction")) {
      throw new IllegalArgumentException("Invalid option: " + option);
    }

    String fieldToCheck = null;
    String fieldExpectedValue = null;
    switch (option) {
      case "incomplete":
        fieldToCheck = "PERCENT-COMPLETE";
        fieldExpectedValue = "100"; // incomplete si différent de 100
        break;

      case "completed":
        fieldToCheck = "COMPLETED";
        fieldExpectedValue = "100"; // completed si égal à 100
        break;
      case "inprocess":
        fieldToCheck = "STATUS";
        fieldExpectedValue = "IN-PROCESS"; // inprocess si égal à IN-PROCESS
        break;
      case "needsaction":
        fieldToCheck = "STATUS"; // needsaction si égal à NEEDS-ACTION
        fieldExpectedValue = "NEEDS-ACTION";
        break;
      case "all":
        // pas de filtre
        break;
      default:
        // throw new IllegalArgumentException("Invalid option: " + option);
    }

    List<String> Todos = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      StringBuilder currentTodo = null;
      boolean isTargetTodo = false;

      while ((line = br.readLine()) != null) {
        if (line.startsWith("BEGIN:VTODO")) {
          currentTodo = new StringBuilder();
          currentTodo.append(line).append("\n");
          isTargetTodo = false;
        } else if ((line.startsWith("END:VTODO")) && currentTodo != null) {
          currentTodo.append(line).append("\n");
          if (isTargetTodo) {
            Todos.add(currentTodo.toString());
          }
          currentTodo = null;
        } else if (currentTodo != null) {
          currentTodo.append(line).append("\n");

          // vérifier le critère
          if (option.equals("all")) {
            isTargetTodo = true;
          } else if (line.startsWith(fieldToCheck)) {
            String value = FilterIcsEvents.getAtrributeValue(line);
            if (option.equals("incomplete") && !value.equals(fieldExpectedValue)) {
              isTargetTodo = true;
            } else if (option.equals("completed") || option.equals("inprocess")
                || option.equals("needsaction") && value.equals(fieldExpectedValue)) {
              isTargetTodo = true;
            }
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Erreur: " + e.getMessage());
    }
    return Todos;
  }

  public static void main(String[] args) {
    String todoFile = "zfiles/todos.ics";

    System.out.println("Test todos completed");
    List<String> todosCompleted = FilterIcsTodos.extractTodosICS("completed", todoFile);
    for (String todo : todosCompleted) {
      System.out.println(todo);
      System.out.println(" \n ");
    }

    System.out.println("Test todos incomplete");
    List<String> todosIncomplete = FilterIcsTodos.extractTodosICS("incomplete", todoFile);
    for (String todo : todosIncomplete) {
      System.out.println(todo);
      System.out.println(" \n ");
    }
  }
}
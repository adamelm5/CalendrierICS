package eirb.pg203;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// rq : cette classe se charge de les incoherences de commandes + dates ..
// fichier prochain de creation d'objets ne devrais pas faire ça, il sera logique et simple
public class CommandParser {
  // constantes pour les types d'événements
  public static final String EVENT_TYPE_EVENT = "events";
  public static final String EVENT_TYPE_TODO = "todos";

  // constantes pour les types de sortie
  public static final String OUTPUT_TEXT = "text";
  public static final String OUTPUT_ICS = "ics";
  public static final String OUTPUT_HTML = "html";

  // constantes pour les tags de date
  public static final String DATE_TODAY = "today";
  public static final String DATE_TOMORROW = "tomorrow";
  public static final String DATE_WEEK = "week";
  public static final String DATE_FROM_TO = "from_to";

  // attributs generaux
  private String eventType; // "events" ou "todos"
  private String outputType; // "text", "ics" ou "html"
  private String outputFile; // output par defaut, sinon le fichier inséré après -o
  private String sourceFile; // fichier source ICS

  // pour ics
  private String dateTag; // "today", "tomorrow", "week" ou "from_to" (par défaut)
  private String fromDate; // si dateTag == "from_to", l'un au moins ne devrait pas etre null
  private String toDate;

  // pour todo
  private String
      todoStatus; // "all" par défaut, "incomplete", "completed", "inprocess", "needsaction"

  // constructeur : valeurs par defaut
  public CommandParser() {
    this.outputType = OUTPUT_TEXT;
    this.outputFile = "output";
    this.sourceFile = null;
    this.dateTag = DATE_TODAY;
    this.fromDate = null;
    this.toDate = null;
    this.todoStatus =
        "all"; // a reviser cette initialisation, pour gerer probleme d'incoherence des parametres
  }

  // getters
  public String getEventType() {
    return eventType;
  }
  public String getOutputType() {
    return outputType;
  }
  public String getOutputFile() {
    return outputFile;
  }
  public String getSourceFile() {
    return sourceFile;
  }
  public String getDateTag() {
    return dateTag;
  }
  public String getFromDate() {
    return fromDate;
  }
  public String getToDate() {
    return toDate;
  }
  public String getTodoStatus() {
    return todoStatus;
  }

  // setters
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }
  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }
  public void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }
  public void setDateTag(String dateTag) {
    this.dateTag = dateTag;
  }
  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }
  public void setToDate(String toDate) {
    this.toDate = toDate;
  }
  public void setTodoStatus(String todoStatus) {
    this.todoStatus = todoStatus;
  }

  // fonction pour diviser la ligne de commande en tokens stockés dans une liste
  public List<String> tokenize(String commandLine) {
    List<String> tokens = new ArrayList<>();
    String[] parts = commandLine.split("\\s+"); // nv :\\s+ pour gérer plusieurs espaces
    for (String part : parts) {
      if (!part.trim().isEmpty()) {
        tokens.add(part.trim());
      }
    }
    return tokens;
  }

  // fonction principale pour parser les arguments
  public void parseCommandLine(String[] args) throws IllegalArgumentException {
    if (args.length < 2) {
      throw new IllegalArgumentException("Usage: clical <sourcefile> <events|todos> [options]");
    }

    // fichier source
    sourceFile = args[0];

    // type d'événement
    if (args[1].equalsIgnoreCase("events")) {
      eventType = EVENT_TYPE_EVENT;
      todoStatus = "all"; // par défaut pour events
    } else if (args[1].equalsIgnoreCase("todos")) {
      eventType = EVENT_TYPE_TODO;
      todoStatus = "incomplete"; // par défaut pour todos selon spécification
    } else {
      throw new IllegalArgumentException("Type must be 'events' or 'todos'");
    }

    int countDateOptions = 0;
    int fromToExists = 0;

    // parser les options restantes
    for (int i = 2; i < args.length; i++) {
      String arg = args[i];

      switch (arg) {
        // dates des events
        case "-today":
          countDateOptions++;
          if (eventType.equals(EVENT_TYPE_EVENT)) {
            dateTag = DATE_TODAY;
            fromDate = null;
            toDate = null;
          } else {
            throw new IllegalArgumentException("-today option is only valid for events");
          }
          break;

        case "-tomorrow":
          countDateOptions++;
          if (eventType.equals(EVENT_TYPE_EVENT)) {
            dateTag = DATE_TOMORROW;
            fromDate = null;
            toDate = null;
          } else {
            throw new IllegalArgumentException("-tomorrow option is only valid for events");
          }
          break;

        case "-week":
          countDateOptions++;
          if (eventType.equals(EVENT_TYPE_EVENT)) {
            dateTag = DATE_WEEK;
            fromDate = null;
            toDate = null;
          } else {
            throw new IllegalArgumentException("-week option is only valid for events");
          }
          break;

        case "-from":
          if (eventType.equals(EVENT_TYPE_EVENT)) {
            if (i + 1 < args.length) {
              dateTag = DATE_FROM_TO;
              fromDate = args[++i];
              fromToExists = 1;
            } else {
              throw new IllegalArgumentException("-from requires a date argument");
            }
          } else {
            throw new IllegalArgumentException("-from option is only valid for events");
          }
          break;

        case "-to":
          if (eventType.equals(EVENT_TYPE_EVENT)) {
            if (i + 1 < args.length) {
              dateTag = DATE_FROM_TO;
              toDate = args[++i];
              fromToExists = 1;
            } else {
              throw new IllegalArgumentException("-to requires a date argument");
            }
          } else {
            throw new IllegalArgumentException("-to option is only valid for events");
          }
          break;

        // status des todos
        case "-all":
          if (eventType.equals(EVENT_TYPE_TODO)) {
            todoStatus = "all";
          } else {
            throw new IllegalArgumentException("-all option is only valid for todos");
          }
          break;

        case "-incomplete":
          if (eventType.equals(EVENT_TYPE_TODO)) {
            todoStatus = "incomplete";
          } else {
            throw new IllegalArgumentException("-incomplete option is only valid for todos");
          }
          break;

        case "-completed":
          if (eventType.equals(EVENT_TYPE_TODO)) {
            todoStatus = "completed";
          } else {
            throw new IllegalArgumentException("-completed option is only valid for todos");
          }
          break;

        case "-inprocess":
          if (eventType.equals(EVENT_TYPE_TODO)) {
            todoStatus = "inprocess";
          } else {
            throw new IllegalArgumentException("-inprocess option is only valid for todos");
          }
          break;

        case "-needsaction":
          if (eventType.equals(EVENT_TYPE_TODO)) {
            todoStatus = "needsaction";
          } else {
            throw new IllegalArgumentException("-needsaction option is only valid for todos");
          }
          break;

        // options de format de sortie
        case "-text":
          outputType = OUTPUT_TEXT;
          break;

        case "-ics":
          outputType = OUTPUT_ICS;
          break;

        case "-html":
          outputType = OUTPUT_HTML;
          break;

        case "-o":
          if (i + 1 < args.length) {
            outputFile = args[++i];
          } else {
            throw new IllegalArgumentException("-o requires a filename argument");
          }
          break;

        default:
          throw new IllegalArgumentException("Unknown option: " + arg);
      }

      // vérifier s'il y a qu'une seule option de date
      if (countDateOptions > 1 || (countDateOptions == 1 && fromToExists == 1)) {
        throw new IllegalArgumentException("Multiple date options specified for events");
      }
    }
    validateOptions();
  }

  // validation finale des options
  private void validateOptions() throws IllegalArgumentException {
    // vérifier que si dateTag est "from_to", au moins une date est définie
    if (dateTag.equals(DATE_FROM_TO) && fromDate == null && toDate == null) {
      throw new IllegalArgumentException(
          "At least one of -from or -to must be specified with a date");
    }

    // vérifier la cohérence entre eventType et les options
    if (eventType.equals(EVENT_TYPE_EVENT)) {
      // pour events, vérifier qu'aucune option todo n'est utilisée
      if (!todoStatus.equals("all")) {
        throw new IllegalArgumentException("Todo status options are only valid for todos");
      }
    } else {
      // Pour todos, vérifier qu'aucune option de date events n'est utilisée
      if (!dateTag.equals(DATE_TODAY)) {
        throw new IllegalArgumentException("Date options are only valid for events");
      }
    }
  }

  // méthode utilitaire pour afficher l'état du parser (pour débogage)
  public void printState() {
    System.out.println("=== CommandParser State ===");
    System.out.println("Source File: " + sourceFile);
    System.out.println("Event Type: " + eventType.toUpperCase());
    System.out.println("Output Type: " + outputType.toUpperCase());
    System.out.println("Output File: " + outputFile);

    if (eventType.equals(EVENT_TYPE_EVENT)) {
      System.out.println("Date Tag: " + dateTag.toUpperCase());
      if (dateTag.equals(DATE_FROM_TO)) {
        System.out.println("From Date: " + fromDate);
        System.out.println("To Date: " + toDate);
      }
    } else {
      System.out.println("Todo Status: " + todoStatus);
    }
    System.out.println("");
  }
}
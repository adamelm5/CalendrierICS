import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ds filterics, verifier si le calendrier est de todos ou event
//  probleme de fournit la date actuelle extractEventsICS()
public class Main {
  public static void main(String[] args) {
    CommandParser parser = null;

    // prendre les arguments de la ligne de commande
    try {
      parser = new CommandParser();
      parser.parseCommandLine(args);
      parser.printState();
    } catch (IllegalArgumentException e) {
      System.err.println("Erreur: " + e.getMessage());
      System.exit(1);
    }

    // filtrer les évènements et les todos selon les options
    // output : les parties ics formatées en String dans une liste
    List<String> output1 = null;
    try {
      String filePath = args[0];

      // vérifier l'existence du fichier
      if (!Files.exists(Paths.get(filePath))) {
        throw new IllegalArgumentException("Le fichier spécifié n'existe pas: " + filePath);
      }

      // extraire les événements ou les todos
      if (parser.getEventType().equals("events")) {
        String currentDate = "04/11/2025"; // a remplacer par date actuelle
        String dateTag = parser.getDateTag();
        String fromDate = parser.getFromDate();
        String toDate = parser.getToDate();
        output1 =
            FilterIcsEvents.extractEventsICS(currentDate, dateTag, filePath, fromDate, toDate);
      } else if (parser.getEventType().equals("todos")) {
        output1 = FilterIcsTodos.extractTodosICS(parser.getTodoStatus(), filePath);
      }
    } catch (IllegalArgumentException e) {
      System.err.println("Erreur lors de la lecture du fichier ICS: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println(
          "Erreur: Pas assez d'arguments. Usage: clical <fichier> <events|todos> [options]");
    }

    if (output1 == null || output1.isEmpty()) {
      System.out.println("Aucun composant trouvé avec les critères spécifiés.");
      return;
    }

    // générer la sortie selon le format choisi
    String outputType = parser.getOutputType();
    if (outputType == null) {
      outputType = CommandParser.OUTPUT_TEXT;
    }
    outputs outputGenerator = new outputs(outputType);

    String output2 = outputGenerator.generateOutput(output1, outputType);
    if (outputType.equals(CommandParser.OUTPUT_TEXT)) {
      System.out.println(parser.getOutputType() + ":\n" + output2);
    } else if (outputType.equals(CommandParser.OUTPUT_HTML)
        || outputType.equals(CommandParser.OUTPUT_ICS)) {
      try {
        FileWriter writer = new FileWriter(parser.getOutputFile());
        writer.write(output2);
        writer.close();
        System.out.println(" Données sauvegardées au format " + parser.getOutputType()
            + " dans le fichier : " + parser.getOutputFile());
      } catch (IOException e) {
        System.err.println("Erreur lors de l'écriture du fichier de sortie: " + e.getMessage());
      }
    } else {
      System.out.println(" Rien ne s'est passé. ");
    }
  }
}
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class outputs {
  private String outputType; // "text " or "html" or "ics"

  // constructor
  public outputs(String outputType) {
    this.outputType = outputType;
  }

  // getter
  public String getOutputType() {
    return outputType;
  }

  // setter
  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  // DOIT ETRE SUPPRIMÉE APRES AVOIR TROUVÉ UNE SOLUTION DE FACTORISATION
  public String dateTimeParser(String icalDate) {
    if (icalDate == null || icalDate.length() < 15) {
      return null;
    }
    String year = icalDate.substring(0, 4);
    String month = icalDate.substring(4, 6);
    String day = icalDate.substring(6, 8);
    String hour = icalDate.substring(9, 11);
    String minute = icalDate.substring(11, 13);
    String seconde = icalDate.substring(13, 15);
    String date = day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + seconde;
    return date;
  }

  // fonction pour transformer un string representant un seul ics en objet CalendarComponent
  CalendarComponent icsToCalendarComponent(String icsString, String componentType)
      throws IllegalArgumentException {
    String[] lines = icsString.split("\n");
    if (componentType.equals("event")) {
      String uid = "", summary = "", location = "", lastModified = "", dtStamp = "", dtStart = "",
             dtEnd = "", description = "", created = "";
      int sequence = 0;
      for (String line : lines) {
        if (line.startsWith("UID:")) {
          uid = line.substring(4).trim();
        } else if (line.startsWith("SUMMARY:")) {
          summary = line.substring(8).trim();
        } else if (line.startsWith("LOCATION:")) {
          location = line.substring(9).trim();
        } else if (line.startsWith("LAST-MODIFIED:")) {
          lastModified = dateTimeParser(line.substring(14).trim());
        } else if (line.startsWith("SEQUENCE:")) {
          sequence = Integer.parseInt(line.substring(9).trim());
        } else if (line.startsWith("DTSTAMP:")) {
          dtStamp = dateTimeParser(line.substring(8).trim());
        } else if (line.startsWith("DTSTART:")) {
          dtStart = dateTimeParser(line.substring(8).trim());
        } else if (line.startsWith("DTEND:")) {
          dtEnd = dateTimeParser(line.substring(6).trim());
        } else if (line.startsWith("DESCRIPTION:")) {
          description = line.substring(12).trim();
        } else if (line.startsWith("CREATED:")) {
          created = line.substring(8).trim();
        }
      }
      return new Event(uid, summary, location, lastModified, sequence, dtStamp, dtStart, dtEnd,
          description, created);
    } else if (componentType.equals("todo")) {
      String uid = "", summary = "", location = "", lastModified = "", dtStamp = "", dtStart = "",
             dueDate = "", status = "", completed = "", organizer = "", classification = "";
      int sequence = 0, percentComplete = 0, priority = 0;
      for (String line : lines) {
        if (line.startsWith("UID:")) {
          uid = line.substring(4).trim();
        } else if (line.startsWith("SUMMARY:")) {
          summary = line.substring(8).trim();
        } else if (line.startsWith("LOCATION:")) {
          location = line.substring(9).trim();
        } else if (line.startsWith("LAST-MODIFIED:")) {
          lastModified = dateTimeParser(line.substring(14).trim());
        } else if (line.startsWith("SEQUENCE:")) {
          sequence = Integer.parseInt(line.substring(9).trim());
        } else if (line.startsWith("DTSTAMP:")) {
          dtStamp = dateTimeParser(line.substring(8).trim());
        } else if (line.startsWith("DTSTART:")) {
          dtStart = dateTimeParser(line.substring(8).trim());
        } else if (line.startsWith("DUE:")) {
          dueDate = dateTimeParser(line.substring(4).trim());
        } else if (line.startsWith("STATUS:")) {
          status = line.substring(7).trim();
        } else if (line.startsWith("PERCENT-COMPLETE:")) {
          percentComplete = Integer.parseInt(line.substring(17).trim());
        } else if (line.startsWith("PRIORITY:")) {
          priority = Integer.parseInt(line.substring(9).trim());
        } else if (line.startsWith("COMPLETED:")) {
          completed = line.substring(10).trim();
        } else if (line.startsWith("ORGANIZER:")) {
          organizer = line.substring(10).trim();
        } else if (line.startsWith("CLASSIFICATION:")) {
          classification = line.substring(15).trim();
        }
      }
      // System.out.println("type detecté : " + componentType);
      return new Todo(uid, summary, location, lastModified, sequence, dtStamp, dtStart, dueDate,
          status, percentComplete, priority, completed, organizer, classification);
    } else {
      throw new IllegalArgumentException("Unsupported component type: " + componentType);
    }
  }

  // fonction qui transforme une liste de Strings en liste de CalendarComponent
  public ComponentList<CalendarComponent> icsListToCalendarComponentsList(List<String> icsStrings) {
    List<CalendarComponent> components = new ArrayList<>();
    ComponentList<CalendarComponent> elementList = new ComponentList<>(components);
    for (String icsString : icsStrings) {
      String componentType = "";
      if (icsString.contains("BEGIN:VEVENT")) {
        componentType = "event";
      } else if (icsString.contains("BEGIN:VTODO")) {
        componentType = "todo";
      } else {
        throw new IllegalArgumentException(
            "component in string doesn't contain any BEGIN: field: " + icsString);
      }
      CalendarComponent component = icsToCalendarComponent(icsString, componentType);
      elementList.add(component);
    }
    return elementList;
  }

  // fonction qui génère la sortie formatée selon le type spécifié d'une liste de strings ICS
  public String generateOutput(List<String> icsStrings, String outputType) {
    if (outputType == null) {
      outputType = CommandParser.OUTPUT_TEXT; // valeur par défaut est text
    }
    if (outputType.equals(CommandParser.OUTPUT_ICS)) { // le cas non géré par componentList
      this.outputType = outputType;
      String icsOutput = String.join("", icsStrings);
      return icsOutput;
    } else if (outputType.equals(CommandParser.OUTPUT_TEXT)) {
      ComponentList<CalendarComponent> elementList = icsListToCalendarComponentsList(icsStrings);
      String icsOutput = "Éléments du calendrier formatés en texte:\n\n";
      icsOutput += elementList.toStringByType(CommandParser.OUTPUT_TEXT);
      return icsOutput;
    } else if (outputType.equals(CommandParser.OUTPUT_HTML)) {
      ComponentList<CalendarComponent> elementList = icsListToCalendarComponentsList(icsStrings);
      String icsOutput = "<html>"
          + "<head>"
          + "<meta charset=\"UTF-8\">"
          + "<title>Calendrier</title>"
          + "</head>"
          + "<body style=\""
          + "background-color:#f4f6f8;"
          + "margin:0;"
          + "padding:0;"
          + "font-family:Arial, sans-serif;"
          + "\">"
          + "<div style=\""
          + "max-width:900px;"
          + "margin:40px auto;"
          + "padding:20px;"
          + "\">"
          + "<h1 style=\""
          + "text-align:center;"
          + "color:#2c3e50;"
          + "margin-bottom:30px;"
          + "\">"
          + "Éléments du calendrier"
          + "</h1>\n" + elementList.toStringByType(CommandParser.OUTPUT_HTML) + "</div>"
          + "</body>"
          + "</html>";

      return icsOutput;
    } else {
      throw new IllegalArgumentException("Unsupported output type: " + outputType);
    }
  }

  public static void main(String[] args) {
    // test de la classe outputs
    outputs outputGenerator = new outputs(CommandParser.OUTPUT_TEXT);
    List<String> ICSlist = new ArrayList<>();
    String EventStringIcs = "BEGIN:VEVENT\n"
        + "DTSTAMP:20251104T215832Z\n"
        + "DTSTART:20251110T151000Z\n"
        + "DTEND:20251110T154000Z\n"
        + "SUMMARY:Présentation PFA\n"
        + "LOCATION:EA- (AMPHI A)\n"
        + "DESCRIPTION:\\n\\n\\nReunion\\nI2\\nJANIN David\\n(Exporté le:04/11/2025 22:58)\\n\n"
        + "UID:ADE60323032352d323032362d343731362d302d30\n"
        + "CREATED:19700101T000000Z\n"
        + "LAST-MODIFIED:20251104T215832Z\n"
        + "SEQUENCE:2142021698\n"
        + "END:VEVENT";
    String TodoStringIcs = "BEGIN:VTODO\n"
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
        + "DTSTART;VALUE=DATE:20251105\n"
        + "END:VTODO";
    String EventStringIcs2 = "BEGIN:VEVENT\n"
        + "DTSTAMP:20240101T120000Z\n"
        + "DTSTART:20240110T090000Z\n"
        + "DTEND:20240110T100000Z\n"
        + "SUMMARY:Meeting\n"
        + "LOCATION:Office\n"
        + "DESCRIPTION:Project meeting to discuss progress\n"
        + "UID:uid123\n"
        + "CREATED:20231231T110000Z\n"
        + "LAST-MODIFIED:20240101T120000Z\n"
        + "SEQUENCE:1\n"
        + "END:VEVENT";
    ICSlist.add(EventStringIcs);
    ICSlist.add(EventStringIcs2);
    ICSlist.add(TodoStringIcs);

    String outputText = outputGenerator.generateOutput(ICSlist, CommandParser.OUTPUT_TEXT);
    System.out.println("Output en format text:\n" + outputText);

    String outputHtml = outputGenerator.generateOutput(ICSlist, CommandParser.OUTPUT_HTML);
    try (FileWriter writer = new FileWriter("file.html")) {
      writer.write(outputHtml);
      System.out.println("Fichier HTML généré : output.html");
    } catch (IOException e) {
      System.err.println("Erreur lors de l'écriture du fichier HTML: " + e.getMessage());
    }

    String outputIcs = outputGenerator.generateOutput(ICSlist, CommandParser.OUTPUT_ICS);
    System.out.println("Output en format ics:\n" + outputIcs);
  }
}
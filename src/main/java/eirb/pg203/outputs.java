package eirb.pg203;

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
}
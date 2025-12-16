package eirb.pg203;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterIcsEvents {
  // fonction qui retourne la chaine de caractère correspondant à l'attribut
  // se trouvant devant  ":" dans la ligne
  public static String getAttributeName(String line) {
    String[] split = line.split(":");
    return split[0];
  }

  // fonction qui retourne la chaine de caractère correspondant à la valeur de l'attribut
  // se trouvant après ":" dans la ligne
  public static String getAtrributeValue(String line) {
    String[] split = line.split(":", 2);
    return split[1];
  }

  /**
    EVENTS :
    -today - tomorrow - week
    -fromDate -toDate
    *
  */

  // Fonction qui retourne la liste de chaine de caractère correspondant aux composantes
  // event se déroulant dans une date donnée date ( format string jj/mm/aaaa ), sous format ics
  public static List<String> extractTodayEventsICS(String date, String filePath) {
    List<String> todayEvents = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      String currentEvent = "";
      boolean isTodayEvent = false;

      while ((line = br.readLine()) != null) {
        if (line.startsWith("BEGIN:VEVENT") || line.startsWith("BEGIN:VTODO")) {
          currentEvent += line + "\n";
          isTodayEvent = false;

        } else if ((line.startsWith("END:VEVENT") || line.startsWith("END:VTODO"))
            && currentEvent.length() > 0) {
          currentEvent += line + "\n";

          if (isTodayEvent) {
            todayEvents.add(currentEvent);
          }

          currentEvent = "";

        } else if (currentEvent.length() > 0) {
          // À l'intérieur d'un événement
          currentEvent += line + "\n";

          // Vérifier si c'est aujourd'hui
          if (line.startsWith("DTSTART:") && line.length() >= 16) {
            String EventDate = DateUtils.dateParser(getAtrributeValue(line));
            if (date.equals(EventDate)) {
              isTodayEvent = true;
            }
          }
        }
      }

    } catch (IOException e) {
      System.err.println("Erreur: " + e.getMessage());
    }

    return todayEvents;
  }

  // Fonction qui retourne la liste de chaine de caractère correspondant aux composantes
  // event se déroulant le lendemain d'une date donnée date ( format string jj/mm/aaaa ), sous
  // format ics
  public static List<String> extractTomorrowEventsICS(String date, String filePath) {
    String tomorrowDate = DateUtils.tomorrowDateParser(date);
    return extractTodayEventsICS(tomorrowDate, filePath);
  }

  public static List<String> extractWeekEventsICS(String date, String filePath) {
    List<String> weekEvents = new ArrayList<>();
    List<String> weekDays = DateUtils.getWeekDays(date);
    for (String day : weekDays) {
      List<String> Events = extractTodayEventsICS(day, filePath);
      weekEvents.addAll(Events);
    }
    return weekEvents;
  }

  // Fonction qui retourne la liste de chaine de caractère correspondant aux composantes
  // event se déroulant entre deux dates données fromDate et toDate ( format string jj/mm/aaaa ),
  // sous format ics
  public static List<String> extractBetweenDatesEventsICS(
      String fromDate, String toDate, String filePath) {
    List<String> Events = new ArrayList<>();
    String currentDate = fromDate;
    while (true) {
      List<String> todayEvents = extractTodayEventsICS(currentDate, filePath);
      Events.addAll(todayEvents);
      currentDate = DateUtils.tomorrowDateParser(currentDate);
      if (currentDate.equals(DateUtils.tomorrowDateParser(toDate))) {
        break;
      }
    }
    return Events;
  }

  public static List<String> extractEventsICS(String todayDate, String option, String filePath,
      String fromDate, String toDate) throws IllegalArgumentException {
    switch (option) {
      case "today":
        return extractTodayEventsICS(todayDate, filePath);
      case "tomorrow":
        return extractTomorrowEventsICS(todayDate, filePath);
      case "week":
        return extractWeekEventsICS(todayDate, filePath);
      case "from_to":
        return extractBetweenDatesEventsICS(fromDate, toDate, filePath);
      default:
        throw new IllegalArgumentException("Invalid option: " + option);
    }
  }

  public static void main(String[] args) {
    FilterIcsEvents filter = new FilterIcsEvents();
    String eventfile = "zfiles/event.ics";
    String date = "06/10/2025";
    // test pour event
    System.out.println("Test from to");
    List<String> events = filter.extractEventsICS(date, "from_to", eventfile, date, date);
    for (String event : events) {
      System.out.println(event);
      System.out.println(" \n ");
    }
    System.out.println("Test today");
    List<String> todayEvents = filter.extractEventsICS(date, "today", eventfile, "", "");
    for (String event : todayEvents) {
      System.out.println(event);
      System.out.println(" \n ");
    }
  }
}
package eirb.pg203;

import java.util.ArrayList;
import java.util.List;

public class DateUtils {
  DateUtils() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("dateUtils is an utility class only");
  }

  public String getTodayDate() {
    return "16/12/2024"; // jour de mon anniversaire :), à changer plus tard
  }

  // fonction qui retourne les jours de la semaine d'une date donnée date (format string jj/mm/aaaa)
  public static List<String> getWeekDays(String date) {
    List<String> weekDays = new ArrayList<>();
    // pour le moment je vais prendre le jour séléctionné et les 6 à venir
    for (int i = 0; i < 7; i++) {
      weekDays.add(date.substring(0, 10));
      date = tomorrowDateParser(date);
    }
    return weekDays;
  }

  // fonction qui convertit une date heure ics en date heure jj/mm/aaaa hh:mm:ss
  public static String dateTimeParser(String icalDate) throws IllegalArgumentException {
    if (icalDate == null || icalDate.length() < 15) {
      throw new IllegalArgumentException(
          "Invalid ICS date-time format: " + icalDate + ". Expected format: YYYYMMDDTHHMMSSZ");
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

  // fonction qui retourne la date (jj/mm/aaaa) d'une date heure ics
  public static String dateParser(String dateTime) throws IllegalArgumentException {
    if (dateTime == null || dateTime.length() < 8) {
      throw new IllegalArgumentException(
          "Invalid ICS date format: " + dateTime + ". Expected format: YYYYMMDD or longer.");
    } else
      return dateTimeParser(dateTime).substring(0, 10);
  }

  // fonction qui retourne le nombre de jours d'un mois
  private static int getDaysInMonth(int month, int year) throws IllegalArgumentException {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException(
          "Non valid month: " + month + ". Should be between 1 and 12.");
    }
    if (month == 2) {
      return isLeapYear(year) ? 29 : 28;
    }
    if (month == 4 || month == 6 || month == 9 || month == 11) {
      return 30;
    }
    return 31;
  }

  // fonction pour vérifier les années bissextiles
  private static boolean isLeapYear(int year) {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
  }

  // fonction qui retourne le lendemain d'une date donnée date ( format string jj/mm/aaaa )
  public static String tomorrowDateParser(String todayDate) {
    if (todayDate == null || todayDate.length() < 10) {
      return null;
    }

    int day = Integer.parseInt(todayDate.substring(0, 2));
    int month = Integer.parseInt(todayDate.substring(3, 5));
    int year = Integer.parseInt(todayDate.substring(6, 10));
    day++;

    if (day > getDaysInMonth(month, year)) {
      day = 1;
      month++;
      if (month > 12) {
        month = 1;
        year++;
      }
    }
    return String.format("%02d/%02d/%04d", day, month, year);
  }
}
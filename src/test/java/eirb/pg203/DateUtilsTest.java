import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {
  @Test(expected = UnsupportedOperationException.class)
  public void testConstructorIsPrivate() throws Exception {
    // Test que le constructeur est privé et lance une exception
    Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void testDateTimeParserValid() {
    // Test avec une date ICS valide
    String icalDate = "20241216T143000Z";
    String expected = "16/12/2024 14:30:00";
    String result = DateUtils.dateTimeParser(icalDate);
    assertEquals(expected, result);

    // Test avec minuit
    icalDate = "20240101T000000Z";
    expected = "01/01/2024 00:00:00";
    result = DateUtils.dateTimeParser(icalDate);
    assertEquals(expected, result);

    // Test avec fin de journée
    icalDate = "20241231T235959Z";
    expected = "31/12/2024 23:59:59";
    result = DateUtils.dateTimeParser(icalDate);
    assertEquals(expected, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateTimeParserNull() {
    DateUtils.dateTimeParser(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateTimeParserTooShort() {
    DateUtils.dateTimeParser("20241216");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateTimeParserInvalidFormat() {
    DateUtils.dateTimeParser("2024-12-16T14:30:00");
  }

  @Test
  public void testDateParser() {
    // Test avec une date ICS complète
    String icalDateTime = "20241216T143000Z";
    String expected = "16/12/2024";
    String result = DateUtils.dateParser(icalDateTime);
    assertEquals(expected, result);

    // Test avec une date seule (sans heure)
    String icalDate = "20241216";
    expected = "16/12/2024";
    result = DateUtils.dateParser(icalDate);
    assertEquals(expected, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateParserNull() {
    DateUtils.dateParser(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDateParserTooShort() {
    DateUtils.dateParser("2024");
  }

  @Test
  public void testTomorrowDateParser() {
    // Test cas normal
    String today = "16/12/2024";
    String expected = "17/12/2024";
    String result = DateUtils.tomorrowDateParser(today);
    assertEquals(expected, result);

    // Test fin de mois
    today = "31/01/2024";
    expected = "01/02/2024";
    result = DateUtils.tomorrowDateParser(today);
    assertEquals(expected, result);

    // Test fin février année non bissextile
    today = "28/02/2023";
    expected = "01/03/2023";
    result = DateUtils.tomorrowDateParser(today);
    assertEquals(expected, result);

    // Test fin février année bissextile
    today = "28/02/2024";
    expected = "29/02/2024";
    result = DateUtils.tomorrowDateParser(today);
    assertEquals(expected, result);

    // Test fin décembre
    today = "31/12/2024";
    expected = "01/01/2025";
    result = DateUtils.tomorrowDateParser(today);
    assertEquals(expected, result);
  }

  @Test
  public void testTomorrowDateParserNull() {
    assertNull(DateUtils.tomorrowDateParser(null));
  }

  @Test
  public void testTomorrowDateParserInvalid() {
    // Date trop courte
    assertNull(DateUtils.tomorrowDateParser("16/12/24"));

    // Format invalide
    assertNull(DateUtils.tomorrowDateParser("2024-12-16"));
  }

  @Test
  public void testGetWeekDays() {
    String startDate = "16/12/2024";
    List<String> weekDays = DateUtils.getWeekDays(startDate);

    // Doit retourner 7 jours
    assertEquals(7, weekDays.size());

    // Vérifier le premier jour
    assertEquals("16/12/2024", weekDays.get(0));

    // Vérifier les jours suivants
    assertEquals("17/12/2024", weekDays.get(1));
    assertEquals("18/12/2024", weekDays.get(2));
    assertEquals("19/12/2024", weekDays.get(3));
    assertEquals("20/12/2024", weekDays.get(4));
    assertEquals("21/12/2024", weekDays.get(5));
    assertEquals("22/12/2024", weekDays.get(6));
  }

  @Test
  public void testGetWeekDaysAcrossMonth() {
    String startDate = "30/12/2024";
    List<String> weekDays = DateUtils.getWeekDays(startDate);

    assertEquals(7, weekDays.size());
    assertEquals("30/12/2024", weekDays.get(0));
    assertEquals("31/12/2024", weekDays.get(1));
    assertEquals("01/01/2025", weekDays.get(2)); // Changement d'année
    assertEquals("02/01/2025", weekDays.get(3));
    assertEquals("03/01/2025", weekDays.get(4));
    assertEquals("04/01/2025", weekDays.get(5));
    assertEquals("05/01/2025", weekDays.get(6));
  }

  @Test
  public void testGetWeekDaysAcrossFebruaryLeapYear() {
    String startDate = "27/02/2024"; // Année bissextile
    List<String> weekDays = DateUtils.getWeekDays(startDate);

    assertEquals(7, weekDays.size());
    assertEquals("27/02/2024", weekDays.get(0));
    assertEquals("28/02/2024", weekDays.get(1));
    assertEquals("29/02/2024", weekDays.get(2)); // 29 février existe
    assertEquals("01/03/2024", weekDays.get(3));
    assertEquals("02/03/2024", weekDays.get(4));
    assertEquals("03/03/2024", weekDays.get(5));
    assertEquals("04/03/2024", weekDays.get(6));
  }

  @Test
  public void testGetWeekDaysAcrossFebruaryNonLeapYear() {
    String startDate = "27/02/2023"; // Année non bissextile
    List<String> weekDays = DateUtils.getWeekDays(startDate);

    assertEquals(7, weekDays.size());
    assertEquals("27/02/2023", weekDays.get(0));
    assertEquals("28/02/2023", weekDays.get(1));
    assertEquals("01/03/2023", weekDays.get(2)); // Pas de 29 février
    assertEquals("02/03/2023", weekDays.get(3));
    assertEquals("03/03/2023", weekDays.get(4));
    assertEquals("04/03/2023", weekDays.get(5));
    assertEquals("05/03/2023", weekDays.get(6));
  }

  @Test
  public void testIsLeapYear() {
    // Méthode privée, testée indirectement via tomorrowDateParser
    // Années bissextiles
    assertTrue(isLeapYearIndirect(2024));
    assertTrue(isLeapYearIndirect(2000));
    assertTrue(isLeapYearIndirect(2016));

    // Années non bissextiles
    assertFalse(isLeapYearIndirect(2023));
    assertFalse(isLeapYearIndirect(1900));
    assertFalse(isLeapYearIndirect(2017));
  }

  private boolean isLeapYearIndirect(int year) {
    // Test indirect via le 28 février
    String date = "28/02/" + year;
    String nextDay = DateUtils.tomorrowDateParser(date);
    return nextDay.equals("29/02/" + year);
  }

  @Test
  public void testGetDaysInMonthIndirect() {
    // Test indirect via tomorrowDateParser
    // Février année bissextile
    String date = "28/02/2024";
    String result = DateUtils.tomorrowDateParser(date);
    assertEquals("29/02/2024", result); // 29 jours

    // Février année non bissextile
    date = "28/02/2023";
    result = DateUtils.tomorrowDateParser(date);
    assertEquals("01/03/2023", result); // 28 jours

    // Mois de 31 jours
    date = "31/01/2024";
    result = DateUtils.tomorrowDateParser(date);
    assertEquals("01/02/2024", result);

    // Mois de 30 jours
    date = "30/04/2024";
    result = DateUtils.tomorrowDateParser(date);
    assertEquals("01/05/2024", result);
  }

  @Test
  public void testDateParsingEdgeCases() {
    // Test avec différents formats valides
    String[] testCases = {
        "20241216T000000Z", "20241216T235959Z", "20240101T120000Z", "20241231T000000Z"};

    for (String testCase : testCases) {
      try {
        String result = DateUtils.dateTimeParser(testCase);
        assertNotNull(result);
        assertEquals(19, result.length()); // Format "dd/MM/yyyy HH:mm:ss"
      } catch (Exception e) {
        fail("Should not throw exception for valid date: " + testCase);
      }
    }
  }
}
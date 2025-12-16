package eirb.pg203;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleTest {
  @Test
  public void testLoadCalendarData() throws IOException {
    String data = Main.loadCalendarData(Path.of("src", "test", "resources", "i2.ics"));
    Assertions.assertTrue(data.startsWith("BEGIN:VCALENDAR"));
  }
}

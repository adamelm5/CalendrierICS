import java.util.Arrays;
import java.util.List;

public class Event extends CalendarComponent {
  // specific attributes for Event
  private final String dtEnd;
  private final String description;
  private final String created;

  // constructor
  public Event(String uid, String summary, String location, String lastModified, int sequence,
      String dtStamp, String dtStart, String dtEnd, String description, String created) {
    super(uid, summary, location, lastModified, sequence, dtStamp, dtStart);
    this.dtEnd = dtEnd;
    this.description = description;
    this.created = created;
  }

  // getters
  @Override
  public String getType() { // je viens de me rendre compte que c'est inuile, on a instance of
    return "events";
  }
  public String getDtEnd() {
    return dtEnd;
  }
  public String getDescription() {
    return description;
  }
  public String getCreated() {
    return created;
  }

  // toString
  @Override
  public String toString() {
    // Nettoyer la description pour l'affichage
    String cleanDescription = "";
    if (description != null) {
      // Remplacer \\n par de vrais \n
      cleanDescription = description.replace("\\n", "\n").replaceAll("\n{1,}", "  ");
    }

    return String.format(" Start: %s\n  End: %s\n  Summary: %s\n  Location: %s\n  Description: %s",
        this.getDtStart(), this.dtEnd, this.getSummary(), this.getLocation(), cleanDescription);
  }

  public static void main(String[] args) {
    Event event = new Event("uid123", "Meeting", "Office", "20240101T120000Z", 1,
        "20240101T120000Z", "20240110T090000Z", "20240110T100000Z",
        "Project meeting to discuss progress", "20231231T110000Z");
    // test d'affichage
    System.out.println(event);
  }
}

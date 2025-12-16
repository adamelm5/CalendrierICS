public class Todo extends CalendarComponent {
  // specific attributes for Todo
  private final String dueDate;
  private final String status;
  private int percentComplete;
  private int priority;
  private String completed;
  private final String organizer;
  private final String classification;

  // constructor
  public Todo(String uid, String summary, String location, String lastModified, int sequence,
      String dtStamp, String dtStart, String dueDate, String status, int percentComplete,
      int priority, String completed, String organizer, String classification) {
    super(uid, summary, location, lastModified, sequence, dtStamp, dtStart);
    this.dueDate = dueDate;
    this.status = status;
    this.percentComplete = percentComplete;
    this.priority = priority;
    this.completed = completed;
    this.organizer = organizer;
    this.classification = classification;
  }

  // getters
  @Override
  public String getType() {
    return "todos";
  }
  public String getDueDate() {
    return dueDate;
  }
  public String getStatus() {
    return status;
  }
  public int getPercentComplete() {
    return percentComplete;
  }
  public int getPriority() {
    return priority;
  }
  public String getCompleted() {
    return completed;
  }
  public String getOrganizer() {
    return organizer;
  }
  public String getClassification() {
    return classification;
  }

  // setters
  public void setPercentComplete(int percentComplete) {
    this.percentComplete = percentComplete;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public void setCompleted(String completed) {
    this.completed = completed;
  }

  // toString
  @Override
  public String toString() {
    return String.format(" Start: %s\n  Due: %s\n  Summary: %s\n  Location: %s\n  Status: %s\n  "
            + "Percent Complete: %d%%",
        this.getDtStart(), this.getDueDate(), this.getSummary(), this.getLocation(),
        this.getStatus(), this.getPercentComplete());
  }

  public static void main(String[] args) {
    Todo todo = new Todo("uid456", "Submit Report", "Home Office", "20240102T130000Z", 1,
        "20240102T130000Z", "20240115T170000Z", "20240115T170000Z", "IN-PROCESS", 50, 3, "",
        "MAILTO:organizer@example.com", "PRIVATE");
    // test d'affichage
    System.out.println(todo);
  }
}
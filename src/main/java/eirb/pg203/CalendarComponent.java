package eirb.pg203;

public abstract class CalendarComponent {
  // attributes
  protected final String uid;
  protected final String summary;
  protected final String location;
  protected final String lastModified;
  protected final int sequence;
  protected final String dtStamp;
  protected final String dtStart;

  // constructor
  public CalendarComponent(String uid, String summary, String location, String lastModified,
      int sequence, String dtStamp, String dtStart) {
    this.uid = uid;
    this.summary = summary;
    this.location = location;
    this.lastModified = lastModified;
    this.sequence = sequence;
    this.dtStamp = dtStamp;
    this.dtStart = dtStart;
  }

  // getters
  public String getType() {
    return "generic";
  }
  public String getUid() {
    return uid;
  }
  public String getSummary() {
    return summary;
  }
  public String getLocation() {
    return location;
  }
  public String getLastModified() {
    return lastModified;
  }
  public int getSequence() {
    return sequence;
  }
  public String getDtStamp() {
    return dtStamp;
  }
  public String getDtStart() {
    return dtStart;
  }

  // toString (will be implemented by subclasses : Event and Todo)
  @Override public abstract String toString();
}

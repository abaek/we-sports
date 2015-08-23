package wesports.com.wesports;


import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.UUID;

@ParseClassName("Event")
public class Event extends ParseObject {

  public Date getDate() {
    return getDate("date");
  }

  public void setDate(Date date) {
    put("date", date);
  }

  public String getType() {
    return getString("type");
  }

  public void setType(String type) {
    put("type", type);
  }

  public ParseGeoPoint getGeoLocation() {
    return getParseGeoPoint("point");
  }

  public void setPoint(ParseGeoPoint point) {
    put("point", point);
  }

  public String getDetails() {
    return getString("details");
  }

  public void setDetails(String details) {
    put("details", details);
  }

  public String getLocationName() {
    return getString("location_name");
  }

  public void setLocationName(String location) {
    put("location_name", location);
  }

  public int getNumAttending() {
    return getInt("num_attending");
  }

  public void setNumAttending(int numAttending) {
    put("num_attending", numAttending);
  }

  public void setUuidString() {
    UUID uuid = UUID.randomUUID();
    put("uuid", uuid.toString());
  }

  public String getUuidString() {
    return getString("uuid");
  }

  public static ParseQuery<Event> getQuery() {
    return ParseQuery.getQuery(Event.class);
  }

}

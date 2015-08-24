package wesports.com.wesports;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

@ParseClassName("UserInfo")
public class UserInfo extends ParseObject {

  public String getUserId() {
    return getString("userId");
  }

  public void setUserId(String id) {
    put("userId", id);
  }

  public ParseGeoPoint getLocation() {
    return getParseGeoPoint("location");
  }

  public void setLocation(ParseGeoPoint location) {
    put("location", location);
  }

  public String getLocationName() {
    return getString("locationName");
  }

  public void setLocationName(String locationName) {
    put("locationName", locationName);
  }

  public JSONArray getSubscriptions() {
    return getJSONArray("subscriptions");
  }

  public void setSubscriptions(JSONArray subscriptions) {
    put("subscriptions", subscriptions);
  }

  public static ParseQuery<UserInfo> getQuery() {
    return ParseQuery.getQuery(UserInfo.class);
  }
}

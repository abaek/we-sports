package wesports.com.wesports;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

public class Application extends android.app.Application {

  @Override
  public void onCreate() {
    super.onCreate();

    ParseObject.registerSubclass(Event.class);
    ParseObject.registerSubclass(UserInfo.class);
    String PARSE_APPLICATION_KEY = getResources().getString(R.string.parse_application_key);
    String PARSE_CLIENT_KEY = getResources().getString(R.string.parse_client_key);
    Parse.initialize(this, PARSE_APPLICATION_KEY, PARSE_CLIENT_KEY);

    ParseACL defaultACL = new ParseACL();
    defaultACL.setPublicReadAccess(true);
    defaultACL.setPublicWriteAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

    // If opening app for the first time, create a new user.
    if (ParseUser.getCurrentUser() == null) {
      ParseAnonymousUtils.logIn(new LogInCallback() {
        @Override
        public void done(ParseUser user, ParseException e) {
          if (e == null) {
            SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            // Array with all subscriptions.
            final JSONArray subscriptionsArray = new JSONArray();
            for (String game : getResources().getStringArray(R.array.games_array)) {
              editor.putBoolean(game, true);
              subscriptionsArray.put(game);
            }
            editor.commit();

            // Create userInfo.
            final UserInfo userInfo = new UserInfo();
            userInfo.setUserId(user.getObjectId());
            userInfo.setLocation(new ParseGeoPoint(43.4667, -80.5328));
            userInfo.setSubscriptions(subscriptionsArray);
            userInfo.saveInBackground(new SaveCallback() {
              @Override
              public void done(ParseException e) {
                if (e == null) {
                  // Associate installation with userInfo.
                  ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                  installation.put("userInfo", userInfo);
                  installation.saveInBackground();
                }
              }
            });


          }
        }
      });
    }
  }
}

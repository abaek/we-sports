package wesports.com.wesports;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;

public class Application extends android.app.Application {

  @Override
  public void onCreate() {
    super.onCreate();
    ParseObject.registerSubclass(Event.class);
    String PARSE_APPLICATION_KEY = getResources().getString(R.string.parse_application_key);
    String PARSE_CLIENT_KEY = getResources().getString(R.string.parse_client_key);
    Parse.initialize(this, PARSE_APPLICATION_KEY, PARSE_CLIENT_KEY);
    ParseInstallation.getCurrentInstallation().saveInBackground();


    // If logged in for the first time, subscribe to everything.
    SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
    if (!settings.getBoolean("loggedIn", false)) {
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("loggedIn", true);
      for (String game : getResources().getStringArray(R.array.games_array)) {
        ParsePush.subscribeInBackground(game);
        editor.putBoolean(game, true);
      }
      editor.commit();
    }
  }

}

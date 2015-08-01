package wesports.com.wesports;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class Application extends android.app.Application {

  @Override
  public void onCreate() {
    super.onCreate();
    ParseObject.registerSubclass(Event.class);
    String PARSE_APPLICATION_KEY = getResources().getString(R.string.parse_application_key);
    String PARSE_CLIENT_KEY = getResources().getString(R.string.parse_client_key);
    Parse.initialize(this, PARSE_APPLICATION_KEY, PARSE_CLIENT_KEY);
    ParseInstallation.getCurrentInstallation().saveInBackground();
  }

}

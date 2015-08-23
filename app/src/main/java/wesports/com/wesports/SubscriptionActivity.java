package wesports.com.wesports;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;


public class SubscriptionActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_subscription);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.subscriptions);
    getSupportActionBar().setElevation(10);

    LinearLayout layout = (LinearLayout) findViewById(R.id.subscription_list);

    final SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = settings.edit();

    // Restore preferences
    for (final String game : getResources().getStringArray(R.array.games_array)) {
      LinearLayout view = (LinearLayout) LayoutInflater.from(getApplication()).inflate(R.layout.subscription_row, null);

      TextView text = (TextView) view.findViewById(R.id.subscription_name);
      text.setText(game);

      Switch toggle = (Switch) view.findViewById(R.id.subscription_switch);
      final boolean subscribed = settings.getBoolean(game, false);
      toggle.setChecked(subscribed);
      toggle.setOnCheckedChangeListener(
              new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                  editor.putBoolean(game, isChecked);
                  editor.commit();
                  final JSONArray subscriptions = new JSONArray();
                  // Make sure sharedPreferences always matches subscriptions in database.
                  for (String game : getResources().getStringArray(R.array.games_array)) {
                    if (settings.getBoolean(game, false)) {
                      subscriptions.put(game);
                    }
                  }

                  String userId = ParseUser.getCurrentUser().getObjectId();
                  ParseQuery<UserInfo> query = ParseQuery.getQuery("UserInfo");
                  query.whereEqualTo("userId", userId);
                  query.getFirstInBackground(
                          new GetCallback<UserInfo>() {
                            @Override
                            public void done(UserInfo userInfo, ParseException e) {
                              if (e == null) {
                                userInfo.setSubscriptions(subscriptions);
                                userInfo.saveInBackground();
                              }
                            }
                          }

                  );

                }
              }

      );

      layout.addView(view);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Back button.
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

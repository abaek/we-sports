package wesports.com.wesports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;


public class SubscriptionActivity extends AppCompatActivity {

  private TextView homeLocation;

  private static final int PLACE_PICKER_REQUEST = 1;

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

    final SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = settings.edit();

    LinearLayout layout = (LinearLayout) findViewById(R.id.subscription_list);
    homeLocation = (TextView) findViewById(R.id.home_location);
    homeLocation.setText(settings.getString("locationName", ""));

    // Restore preferences
    for (final String game : getResources().getStringArray(R.array.games_array)) {
      LinearLayout view = (LinearLayout) LayoutInflater.from(getApplication()).inflate(R.layout.subscription_row, null);

      TextView text = (TextView) view.findViewById(R.id.subscription_name);
      text.setText(game);

      Switch toggle = new Switch(this);

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

      view.addView(toggle);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
      Place place = PlacePicker.getPlace(data, this);
      double latitude = place.getLatLng().latitude;
      double longitude = place.getLatLng().longitude;
      final ParseGeoPoint location = new ParseGeoPoint(latitude, longitude);
      String locationName = (String) place.getName();

      SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("locationName", locationName);
      editor.commit();
      homeLocation.setText(locationName);

      ParseQuery<UserInfo> query = ParseQuery.getQuery("UserInfo");
      String userId = ParseUser.getCurrentUser().getObjectId();
      query.whereEqualTo("userId", userId);
      query.getFirstInBackground(
              new GetCallback<UserInfo>() {
                @Override
                public void done(UserInfo userInfo, ParseException e) {
                  if (e == null) {
                    userInfo.setLocation(location);
                    userInfo.saveInBackground();
                  }
                }
              }
      );
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public void changeLocation(View view) {
    try {
      PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
      Intent intent = intentBuilder.build(this);
      startActivityForResult(intent, PLACE_PICKER_REQUEST);
    } catch (GooglePlayServicesRepairableException e) {
      e.printStackTrace();
    } catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }
}

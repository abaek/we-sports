package wesports.com.wesports;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreateEventActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener {

  private ImageView icon;
  private TextView mLocationButton;
  private TextView mTimeButton;
  private EditText mDetailsEdit;
  private TextView errorMessage;

  private double latitude;
  private double longitude;

  private Place place;
  private Calendar cal;
  private Spinner typeSpinner;
  private Spinner dateSpinner;
  private boolean tomorrow;

  private static final int PLACE_PICKER_REQUEST = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_game);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.create_event);
    getSupportActionBar().setElevation(10);

    icon = (ImageView) findViewById(R.id.icon);
    mTimeButton = (TextView) findViewById(R.id.time_button);
    mLocationButton = (TextView) findViewById(R.id.location_button);
    mDetailsEdit = (EditText) findViewById(R.id.details_edit);
    errorMessage = (TextView) findViewById(R.id.error_message);

    dateSpinner = (Spinner) findViewById(R.id.date_spinner);
    ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this,
            R.array.date_array, android.R.layout.simple_spinner_item);
    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    dateSpinner.setAdapter(dateAdapter);

    typeSpinner = (Spinner) findViewById(R.id.game_spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.games_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    typeSpinner.setAdapter(adapter);
    typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null) {
          TextView item = (TextView) view;
          String event = item.getText().toString();
          switch (event) {
            case "Basketball":
              icon.setImageResource(R.mipmap.basketball);
              break;
            case "Soccer":
              icon.setImageResource(R.mipmap.soccer);
              break;
            case "Football":
              icon.setImageResource(R.mipmap.football);
              break;
            case "Volleyball":
              icon.setImageResource(R.mipmap.volleyball);
              break;
            case "Tennis":
              icon.setImageResource(R.mipmap.tennis);
              break;
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    typeSpinner.setSelection(0);

    // Current date and time.
    cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, 2);
    cal.set(Calendar.MINUTE, 0);
    // Set selection to tomorrow.
    if (cal.get(Calendar.HOUR_OF_DAY) < 2) {
      tomorrow = true;
      dateSpinner.setSelection(1);
    } else {
      tomorrow = false;
      dateSpinner.setSelection(0);
    }

    dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Today.
        if (position == 0) {
          if (tomorrow) {
            cal.add(Calendar.DATE, -1);
            tomorrow = false;
          }
        }
        // Tomorrow.
        else {
          if (!tomorrow) {
            cal.add(Calendar.DATE, 1);
            tomorrow = true;
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    Date date = cal.getTime();
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText(timeFormat.format(date));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_create_event, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Back button.
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_create:
        createGame();
    }

    return super.onOptionsItemSelected(item);
  }

  public void locationSelect(View view) {
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

  private void createGame() {
    Date now = new Date();
    Date eventDate = cal.getTime();

    // If no location selected.
    if (mLocationButton.getText().toString().equals(getResources().getString(R.string.location_select))) {
      errorMessage.setText(R.string.location_error_message);
      errorMessage.setVisibility(View.VISIBLE);
    }
    // If time is in the past.
    else if (now.after(eventDate)) {
      errorMessage.setText(R.string.date_error_message);
      errorMessage.setVisibility(View.VISIBLE);
    } else {
      final String type = typeSpinner.getSelectedItem().toString();
      final String location = mLocationButton.getText().toString();

      final Event event = new Event();
      event.setUuidString();
      event.setDate(cal.getTime());
      event.setDetails(mDetailsEdit.getText().toString());
      event.setLocationName(location);
      event.setType(type);
      event.setPoint(new ParseGeoPoint(latitude, longitude));
      event.setNumAttending(1);
      event.saveInBackground(new SaveCallback() {
        @Override
        public void done(com.parse.ParseException e) {
          // User who created the event is attending;
          if (e == null) {
            SharedPreferences.Editor editor = getSharedPreferences("Events", Context.MODE_PRIVATE).edit();
            editor.putBoolean(event.getObjectId(), true);
            editor.commit();
            finish();
          }
        }
      });

      // Find users near a given location.
      ParseQuery userQuery = UserInfo.getQuery();
      userQuery.whereWithinKilometers("location", new ParseGeoPoint(latitude, longitude), 50);
      userQuery.whereEqualTo("subscriptions", type);
      // Don't give yourself push notification.
      userQuery.whereNotEqualTo("userId", ParseUser.getCurrentUser().getObjectId());

      // Find devices associated with these users.
      ParseQuery pushQuery = ParseInstallation.getQuery();
      pushQuery.whereMatchesQuery("userInfo", userQuery);

      // Send push notification to query.
      ParsePush push = new ParsePush();
      push.setQuery(pushQuery);
      if (tomorrow) {
        push.setMessage(type + " tomorrow at " + location + "!");
      } else {
        push.setMessage(type + " today at " + location + "!");
      }
      push.sendInBackground();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
      place = PlacePicker.getPlace(data, this);
      latitude = place.getLatLng().latitude;
      longitude = place.getLatLng().longitude;
      final CharSequence name = place.getName();
      mLocationButton.setText(name);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public void setTime(View view) {
    TimePickerDialog tpd = TimePickerDialog.newInstance(
            CreateEventActivity.this,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
    );
    tpd.setThemeDark(true);
    tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        Log.d("TimePicker", "Dialog was cancelled");
      }
    });
    tpd.show(getFragmentManager(), "Timepickerdialog");
  }

  @Override
  public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
    cal.set(Calendar.MINUTE, minute);
    Date date = cal.getTime();
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa", Locale.CANADA);
    mTimeButton.setText(timeFormat.format(date));
  }
}

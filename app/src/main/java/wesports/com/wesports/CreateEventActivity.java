package wesports.com.wesports;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CreateEventActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener {

  private TextView mLocationButton;
  private TextView mTimeButton;
  private EditText mDetailsEdit;

  private double latitude;
  private double longitude;

  private Place place;
  private Calendar cal;
  private Spinner gameSpinner;
  private Spinner dateSpinner;

  private int timeSeconds;
  private int dateSeconds;

  protected static final String TAG = "HomeActivity";
  private static final int PLACE_PICKER_REQUEST = 1;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_game);

    mTimeButton = (TextView) findViewById(R.id.time_button);
    mLocationButton = (TextView) findViewById(R.id.location_button);
    mDetailsEdit = (EditText) findViewById(R.id.details_edit);

    // set date and time text
    cal = Calendar.getInstance();
    Date date = cal.getTime();
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText("at " + timeFormat.format(date));

    // set gameSpinner values
    gameSpinner = (Spinner) findViewById(R.id.game_spinner);
    // Create an ArrayAdapter using the string array and a default gameSpinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.games_array, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the gameSpinner
    gameSpinner.setAdapter(adapter);

    dateSpinner = (Spinner) findViewById(R.id.date_spinner);
    ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this,
            R.array.date_array, android.R.layout.simple_spinner_item);
    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    dateSpinner.setAdapter(dateAdapter);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void backPressed(View view) {
    finish();
  }

  public void locationSelect(View view) {
    try {
      PlacePicker.IntentBuilder intentBuilder =
              new PlacePicker.IntentBuilder();
      Intent intent = intentBuilder.build(this);
      startActivityForResult(intent, PLACE_PICKER_REQUEST);

    } catch (GooglePlayServicesRepairableException e) {
      e.printStackTrace();
    } catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  public void createGame(View view) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          HttpClient httpclient = new DefaultHttpClient();
          HttpPost httppost = new HttpPost("http://we-sports.herokuapp.com/create");

          Game game = new Game();
          game.type = gameSpinner.getSelectedItem().toString();
          game.details = mDetailsEdit.getText().toString();
          game.date = cal.getTimeInMillis() / 1000;

          game.place = game.new Place();
          game.place.lat = latitude;
          game.place.lon = longitude;

          game.contact = game.new Contact();
          game.contact.phone = "";
          game.contact.email = "";
          game.contact.name = "";

          Gson gson = new Gson();
          String jsonString = gson.toJson(game);

          httppost.setEntity(new StringEntity(jsonString));
          httppost.addHeader("content-type", "application/json");
          httpclient.execute(httppost);
        } catch (Exception e) {
          Log.d("HTTP", e.toString());
        }
      }
    });

    thread.start();
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode,
                                  int resultCode, Intent data) {

    if (requestCode == PLACE_PICKER_REQUEST
            && resultCode == Activity.RESULT_OK) {

      place = PlacePicker.getPlace(data, this);
      latitude = place.getLatLng().latitude;
      longitude = place.getLatLng().longitude;
      final CharSequence name = place.getName();
      mLocationButton.setText("at " + name);
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
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText("at " + timeFormat.format(date));
  }
}

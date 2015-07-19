package wesports.com.wesports;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class HomeActivity extends Activity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {


  private Button mLocationButton;
  private Button mDateButton;
  private Button mTimeButton;
  private TextView mRangeText;
  private EditText mDetailsEdit;
  private EditText mNameOfEventEdit;

  private double latitude;
  private double longitude;

  private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
  private static final String CONFIG_CLIENT_ID = BuildConfig.PAYPAL_CLIENT_ID;
  private static final int REQUEST_CODE_PAYMENT = 2;

  private static PayPalConfiguration config = new PayPalConfiguration()
          .environment(CONFIG_ENVIRONMENT)
          .clientId(CONFIG_CLIENT_ID);

  private Place place;
  private Calendar cal;
  private Spinner spinner;

  private int leftRange;
  private int rightRange;

  private int timeSeconds;
  private int dateSeconds;

  protected static final String TAG = "HomeActivity";
  private static final int PLACE_PICKER_REQUEST = 1;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    mDateButton = (Button) findViewById(R.id.date_button);
    mTimeButton = (Button) findViewById(R.id.time_button);
    mLocationButton = (Button) findViewById(R.id.location_button);
    mDetailsEdit = (EditText) findViewById(R.id.details_edit);
    mNameOfEventEdit = (EditText) findViewById(R.id.name_of_event_edit);

    // set date and time text
    cal = Calendar.getInstance();
    Date date = cal.getTime();
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
    mDateButton.setText(dateFormat.format(date));
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText(timeFormat.format(date));

    // set range bar values
    RangeBar mRangeBar = (RangeBar) findViewById(R.id.rangebar);
    mRangeText = (TextView) findViewById(R.id.rangetext);
    mRangeText.setText(6 + " - " + 10);
    leftRange = 6;
    rightRange = 10;
    mRangeBar.setRangePinsByIndices(6, 10);
    mRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
      @Override
      public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                        int rightPinIndex,
                                        String leftPinValue, String rightPinValue) {
        if (leftPinValue == rightPinValue) {
          mRangeText.setText(leftPinValue);
        } else {
          mRangeText.setText(leftPinValue + " - " + rightPinValue);
        }
        leftRange = leftPinIndex;
        rightRange = rightPinIndex;
      }
    });

    // set spinner values
    spinner = (Spinner) findViewById(R.id.game_spinner);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.games_array, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private PayPalPayment getThingToBuy(String paymentIntent) {
    return new PayPalPayment(new BigDecimal("1.75"), "USD", "Prize Money",
            paymentIntent);
  }

  public void onBuyPressed(View pressed) {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
    PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

    Intent intent = new Intent(this, PaymentActivity.class);

    // send the same configuration for restart resiliency
    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
  }

  public void locationSelect(View view) {
    try {
      PlacePicker.IntentBuilder intentBuilder =
              new PlacePicker.IntentBuilder();
      // intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
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
          game.type = spinner.getSelectedItem().toString();
          game.name = mNameOfEventEdit.getText().toString();
          game.desc = mDetailsEdit.getText().toString();
          game.date = cal.getTimeInMillis() / 1000;
          game.bet = 799;

          game.people = game.new People();
          game.people.max = rightRange;
          game.people.min = leftRange;

          game.place = game.new Place();
          game.place.lat = latitude;
          game.place.lon = longitude;

          game.contact = game.new Contact();
          game.contact.phone = "";
          game.contact.email = "";
          game.contact.name = "";

          Gson gson = new Gson();
          String jsonString = gson.toJson(game);

          httppost.setEntity(new StringEntity(jsonString, "UTF-8"));
          HttpResponse response = httpclient.execute(httppost);
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
      mLocationButton.setText(name);

    } else if (requestCode == REQUEST_CODE_PAYMENT) {
      if (resultCode == Activity.RESULT_OK) {
        PaymentConfirmation confirm =
                data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
        if (confirm != null) {
          try {
            Log.i(TAG, confirm.toJSONObject().toString(4));
            Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
            /**
             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
             * or consent completion.
             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
             * for more details.
             *
             * For sample mobile backend interactions, see
             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
             */
            Toast.makeText(
                    getApplicationContext(),
                    "PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG)
                    .show();

          } catch (JSONException e) {
            Log.e(TAG, "an extremely unlikely failure occurred: ", e);
          }
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        Log.i(TAG, "The user canceled.");
      } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
        Log.i(
                TAG,
                "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }


  public void setDate(View view) {
    DatePickerDialog dpd = DatePickerDialog.newInstance(
            HomeActivity.this,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
    );
    dpd.setThemeDark(true);
    dpd.show(getFragmentManager(), "Datepickerdialog");
  }

  public void setTime(View view) {
    TimePickerDialog tpd = TimePickerDialog.newInstance(
            HomeActivity.this,
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
    mTimeButton.setText(timeFormat.format(date));
  }

  @Override
  public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, monthOfYear);
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    Date date = cal.getTime();
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
    mDateButton.setText(dateFormat.format(date));
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText(timeFormat.format(date));
  }
}

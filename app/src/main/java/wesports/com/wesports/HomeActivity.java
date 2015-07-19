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

import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class HomeActivity extends Activity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener{

    private Button mLocationButton;
    private Button mDateButton;
    private Button mTimeButton;
    private TextView mRangeText;
    private EditText mDetailsEdit;

    protected static final String TAG = "HomeActivity";
    private static final int PLACE_PICKER_REQUEST = 1;

    private Place place;
    Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDateButton = (Button) findViewById(R.id.date_button);
        mTimeButton = (Button) findViewById(R.id.time_button);
        mLocationButton = (Button) findViewById(R.id.location_button);

        mDetailsEdit = (EditText) findViewById(R.id.details_edit);

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
            }
        });

        // set spinner values
        Spinner spinner = (Spinner) findViewById(R.id.game_spinner);
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

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            place = PlacePicker.getPlace(data, this);
            final CharSequence name = place.getName();
            mLocationButton.setText(name);

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

package wesports.com.wesports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity {

  private ListView eventList;

  private static final int SUBSCRIPTION_CHANGED_REQUEST = 1;
  private static final int CREATE_EVENT_REQUEST = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    eventList = (ListView) findViewById(R.id.event_list);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setElevation(10);

    loadList();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      Intent intent = new Intent(this, SubscriptionActivity.class);
      startActivityForResult(intent, SUBSCRIPTION_CHANGED_REQUEST);
    }

    return super.onOptionsItemSelected(item);
  }

  public void onAddGame(View view) {
    Intent intent = new Intent(this, CreateEventActivity.class);
    startActivityForResult(intent, CREATE_EVENT_REQUEST);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SUBSCRIPTION_CHANGED_REQUEST) {
      loadList();
    } else if (requestCode == CREATE_EVENT_REQUEST) {
      loadList();
    }
  }

  private void loadList() {
    ParseQueryAdapter<Event> eventsListAdapter = new EventsListAdapter(this);
    eventsListAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Event>() {
      public void onLoading() {
        // Show Spinner;
      }

      @Override
      public void onLoaded(List<Event> list, Exception e) {
        // Hide Spinner
      }
    });
    eventList.setAdapter(eventsListAdapter);
  }

  private class EventsListAdapter extends ParseQueryAdapter<Event> {

    public EventsListAdapter(Context context) {
      super(context, new ParseQueryAdapter.QueryFactory<Event>() {
        public ParseQuery<Event> create() {
          ParseQuery<Event> query = Event.getQuery();
          ArrayList<String> typesSubscribed = new ArrayList<>();
          SharedPreferences settings = getSharedPreferences("Subscriptions", Context.MODE_PRIVATE);
          for (String game : getResources().getStringArray(R.array.games_array)) {
            if (settings.getBoolean(game, false)) {
              typesSubscribed.add(game);
            }
          }
          query.whereContainedIn("type", typesSubscribed);
          query.whereGreaterThan("date", new Date());
          return query;
        }
      });
    }

    @Override
    public View getItemView(final Event event, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_row, parent, false);
      }

      TextView date = (TextView) convertView.findViewById(R.id.date);
      SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa", Locale.CANADA);
      String time = timeFormat.format(event.getDate());
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.CANADA);

      if (dateFormat.format(new Date()).equals(dateFormat.format(event.getDate()))) {
        date.setText(getResources().getString(R.string.today) + " at " + time);
      } else {
        date.setText(getResources().getString(R.string.tomorrow) + " at " + time);
      }

      TextView type = (TextView) convertView.findViewById(R.id.type);
      type.setText(event.getType());

      TextView location = (TextView) convertView.findViewById(R.id.location);
      location.setText(event.getLocation());

      LinearLayout locationButton = (LinearLayout) convertView.findViewById(R.id.location_button);
      locationButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          double latitude = Double.parseDouble(event.getLat());
          double longitude = Double.parseDouble(event.getLon());
          String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=<%f>,<%f>", latitude, longitude, latitude, longitude);
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
          startActivity(intent);
        }
      });

      TextView details = (TextView) convertView.findViewById(R.id.details);
      if (event.getDetails().isEmpty()) {
        details.setVisibility(View.GONE);
      } else {
        details.setVisibility(View.VISIBLE);
        details.setText(event.getDetails());
      }

      final TextView numAttending = (TextView) convertView.findViewById(R.id.num_attending);
      numAttending.setText(String.valueOf(event.getNumAttending()) + " attending");

      final ImageView check = (ImageView) convertView.findViewById(R.id.check);

      final Button acceptButton = (Button) convertView.findViewById(R.id.accept_button);
      SharedPreferences settings = getSharedPreferences("Events", Context.MODE_PRIVATE);
      final SharedPreferences.Editor editor = settings.edit();
      boolean accepted = settings.getBoolean(event.getObjectId(), false);
      if (accepted) {
        check.setVisibility(View.VISIBLE);
        acceptButton.setTextColor(getResources().getColor(R.color.black));
        numAttending.setTextColor(getResources().getColor(R.color.black));
      } else {
        check.setVisibility(View.GONE);
        acceptButton.setTextColor(getResources().getColor(R.color.gray));
        numAttending.setTextColor(getResources().getColor(R.color.gray));
      }
      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

          // Change colour.
          TextView button = (TextView) v;
          final boolean accept = button.getCurrentTextColor() == getResources().getColor(R.color.gray);
          if (accept) {
            button.setTextColor(getResources().getColor(R.color.black));
          } else {
            button.setTextColor(getResources().getColor(R.color.gray));
          }

          // Change in SharedPreferences.
          editor.putBoolean(event.getObjectId(), accept);
          editor.commit();

          // Change num_attending in Parse.
          ParseQuery<Event> query = Event.getQuery();
          query.getInBackground(event.getObjectId(), new GetCallback<Event>() {
            public void done(Event event, ParseException e) {
              if (e == null) {
                int attending = event.getNumAttending();
                if (accept) {
                  event.setNumAttending(attending + 1);
                  check.setVisibility(View.VISIBLE);
                  numAttending.setTextColor(getResources().getColor(R.color.black));
                  numAttending.setText(String.valueOf(attending + 1) + " attending");
                } else {
                  event.setNumAttending(attending - 1);
                  check.setVisibility(View.GONE);
                  numAttending.setTextColor(getResources().getColor(R.color.gray));
                  numAttending.setText(String.valueOf(attending - 1) + " attending");
                }
                event.saveInBackground();
              }
            }
          });
        }
      });

      return convertView;
    }
  }
}

package wesports.com.wesports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

  private ListView eventList;
  private ParseQueryAdapter<Event> eventsListAdapter;

  private static final int SUBSCRIPTION_CHANGED_REQUEST = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    eventList = (ListView) findViewById(R.id.event_list);
    eventsListAdapter = new EventsListAdapter(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setElevation(10);

    // If logged in for the first time, subscribe to everything.
    SharedPreferences settings = getPreferences(0);
    if (!settings.getBoolean("loggedIn", false)) {
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("loggedIn", true);
      for (String game : getResources().getStringArray(R.array.games_array)) {
        ParsePush.subscribeInBackground(game);
        editor.putBoolean(game, true);
      }
      editor.commit();
    }

    // Query events.
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
    ArrayList<String> typesSubscribed = new ArrayList<>();
    for (final String game : getResources().getStringArray(R.array.games_array)) {
      if (settings.getBoolean(game, false)) {
        typesSubscribed.add(game);
      }
    }
    query.whereContainedIn("type", typesSubscribed);
    query.whereGreaterThan("date", new Date());
    query.findInBackground(new FindCallback<ParseObject>() {
      public void done(List<ParseObject> picksList, ParseException e) {
        if (e == null) {
          // Set adapter after picks and picksIds have been found.
          eventList.setAdapter(eventsListAdapter);
          eventsListAdapter.notifyDataSetChanged();
        }
      }
    });
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
    startActivity(intent);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SUBSCRIPTION_CHANGED_REQUEST) {
      if (resultCode == RESULT_OK) {
        eventsListAdapter.notifyDataSetChanged();
      }
    }
  }

  public class GamesAdapter extends ArrayAdapter<Event> {
    public GamesAdapter(Context context, ArrayList<Event> events) {
      super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // Get the data item for this position
      Event event = getItem(position);
      // Check if an existing view is being reused, otherwise inflate the view
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_row, parent, false);
      }

      TextView gameDate = (TextView) convertView.findViewById(R.id.date);
      TextView gameTime = (TextView) convertView.findViewById(R.id.time);
      TextView acceptButton = (TextView) convertView.findViewById(R.id.accept_button);

      gameDate.setText("Today");
      gameTime.setText("06:00 PM");
      // Return the completed view to render on screen

      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          TextView button = (TextView) v;
          if (button.getCurrentTextColor() == getResources().getColor(R.color.black)) {
            button.setTextColor(getResources().getColor(R.color.colorAccent));
          } else {
            button.setTextColor(getResources().getColor(R.color.black));
          }
        }
      });

      return convertView;
    }
  }

  private class EventsListAdapter extends ParseQueryAdapter<Event> {

    public EventsListAdapter(Context context) {
      super(context, new ParseQueryAdapter.QueryFactory<Event>() {
        public ParseQuery<Event> create() {
          ParseQuery<Event> query = Event.getQuery();
          query.orderByAscending("date");
          return query;
        }
      });
    }

    @Override
    public View getItemView(Event event, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_row, parent, false);
      }

      TextView date = (TextView) convertView.findViewById(R.id.date);
      date.setText("Today");

      TextView time = (TextView) convertView.findViewById(R.id.time);
      SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa");
      time.setText(timeFormat.format(event.getDate()));

      TextView type = (TextView) convertView.findViewById(R.id.type);
      type.setText(event.getType());

      TextView location = (TextView) convertView.findViewById(R.id.location);
      location.setText(event.getLocation());

      TextView details = (TextView) convertView.findViewById(R.id.details);
      details.setText(event.getDetails());

      TextView numAttending = (TextView) convertView.findViewById(R.id.num_attending);
      numAttending.setText(String.valueOf(event.getNumAttending()) + " attending");

      TextView acceptButton = (TextView) convertView.findViewById(R.id.accept_button);
      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          TextView button = (TextView) v;
          if (button.getCurrentTextColor() == getResources().getColor(R.color.black)) {
            button.setTextColor(getResources().getColor(R.color.colorAccent));
          } else {
            button.setTextColor(getResources().getColor(R.color.black));
          }
        }
      });

      return convertView;
    }
  }
}

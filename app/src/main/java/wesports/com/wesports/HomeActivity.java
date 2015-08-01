package wesports.com.wesports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

  private ListView eventList;

  private static final int SUBSCRIPTION_CHANGED_REQUEST = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setElevation(10);

    eventList = (ListView) findViewById(R.id.event_list);
    loadGames(eventList);

    SharedPreferences settings = getPreferences(0);

    if (!settings.getBoolean("loggedIn", false)) {
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("loggedIn", true);
      for (String game : getResources().getStringArray(R.array.games_array)) {
        editor.putBoolean(game, true);
      }
      editor.apply();
    }
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
        loadGames(eventList);

        SharedPreferences settings = getPreferences(0);
        for (final String game : getResources().getStringArray(R.array.games_array)) {
          if (settings.getBoolean(game, false)) {
          }
        }
      }
    }
  }

  private void loadGames(final ListView listview) {
    try {
      HttpPost httppost = new HttpPost("http://we-sports.herokuapp.com/events");

      ArrayList<String> types = new ArrayList<>();
      SharedPreferences settings = getPreferences(0);
      for (final String game : getResources().getStringArray(R.array.games_array)) {
        if (settings.getBoolean(game, false)) {
          types.add(game);
        }
      }

      Gson gson1 = new Gson();
      String jsonString = gson1.toJson(types);

      httppost.setEntity(new StringEntity(jsonString));
      httppost.addHeader("content-type", "application/json");

      LoadGamesAsyncTask loadGamesTask =
              new LoadGamesAsyncTask(httppost, new LoadGamesAsyncTask.Callback() {

                @Override
                public void onComplete(Object o, Error error) {
                  if (error != null) {
                    Log.e("LoadGamesTask", error.getMessage());
                    return;
                  }
                  List<Game> gamesList = (List<Game>) o;
                  Log.e("LoadGamesTask", "" + gamesList.size());
                  GamesAdapter adapter = new GamesAdapter(getApplicationContext(), (ArrayList) gamesList);
                  // Attach the adapter to a ListView
                  listview.setAdapter(adapter);
                  adapter.notifyDataSetChanged();
                }
              });

      loadGamesTask.execute();
    } catch (Exception e) {
      Log.d("HTTP", e.toString());
    }
  }

  public class GamesAdapter extends ArrayAdapter<Game> {
    public GamesAdapter(Context context, ArrayList<Game> games) {
      super(context, 0, games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // Get the data item for this position
      Game game = getItem(position);
      // Check if an existing view is being reused, otherwise inflate the view
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_row, parent, false);
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
}

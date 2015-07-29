package wesports.com.wesports;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.util.ArrayList;
import java.util.List;

public class ScreenSlidePageFragment extends Fragment {
  public static final String ARG_PAGE = "page";
  /**
   * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
   */
  private int mPageNumber;

  public static final int NOTIFICATIONS = 0;
  public static final int SUBSCRIPTIONS = 1;

  /**
   * Factory method for this fragment class. Constructs a new fragment for the given page number.
   */
  public static ScreenSlidePageFragment create(int pageNumber) {
    ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_PAGE, pageNumber);
    fragment.setArguments(args);
    return fragment;
  }

  public ScreenSlidePageFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPageNumber = getArguments().getInt(ARG_PAGE);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = null;
    switch (mPageNumber) {
      case NOTIFICATIONS:
        view = getNotificationCard();
        break;
      case SUBSCRIPTIONS:
        view = getSubscriptionCard();
        break;
    }
    return view;
  }

  public View getNotificationCard() {
    ListView listview = (ListView) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_notification, null);
    loadGames(listview);
    return listview;
  }

  public void loadGames(final ListView listview) {
    try {
      HttpPost httppost = new HttpPost("http://we-sports.herokuapp.com/events");

      ArrayList<String> types = new ArrayList<>();
      SharedPreferences settings = getActivity().getPreferences(0);
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
                  GamesAdapter adapter = new GamesAdapter(getActivity(), (ArrayList) gamesList);
                  // Attach the adapter to a ListView
                  listview.setAdapter(adapter);
                }
              });

      loadGamesTask.execute();
    } catch (Exception e) {
      Log.d("HTTP", e.toString());
    }
  }

  public View getSubscriptionCard() {
    LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_subscription, null);
    SharedPreferences settings;
    settings = getActivity().getPreferences(0);
    final SharedPreferences.Editor editor = settings.edit();

    // Restore preferences
    for (final String game : getResources().getStringArray(R.array.games_array)) {

      LinearLayout view = new LinearLayout(getActivity());
      Switch toggle = new Switch(getActivity());
      boolean subscribed = settings.getBoolean(game, false);
      toggle.setChecked(subscribed);
      toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          editor.putBoolean(game, isChecked);
          editor.commit();
        }
      });
      LinearLayout.LayoutParams horizontalSpacingLayout =
              new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                      LinearLayout.LayoutParams.WRAP_CONTENT);
      horizontalSpacingLayout.setMargins(0, 0, 24, 0);
      view.addView(toggle, horizontalSpacingLayout);

      TextView text = new TextView(getActivity());
      text.setTextSize(20);
      text.setTextColor(Color.BLACK);
      text.setText(game);
      view.addView(text);

      LinearLayout.LayoutParams verticalSpacingLayout =
              new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                      LinearLayout.LayoutParams.WRAP_CONTENT);
      verticalSpacingLayout.setMargins(10, 0, 0, 10);
      layout.addView(view, verticalSpacingLayout);
    }
    return layout;
  }

  /**
   * Returns the page number represented by this fragment object.
   */
  public int getPageNumber() {
    return mPageNumber;
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
      // Lookup view for data population
      TextView gameName = (TextView) convertView.findViewById(R.id.game_name);
      TextView gameType = (TextView) convertView.findViewById(R.id.game_type);
      // Populate the data into the template view using the data object
      gameName.setText(game.details);
      gameType.setText(game.type);
      // Return the completed view to render on screen
      return convertView;
    }
  }
}
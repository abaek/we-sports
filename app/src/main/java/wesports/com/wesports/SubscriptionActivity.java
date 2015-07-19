package wesports.com.wesports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;


public class SubscriptionActivity extends FragmentActivity {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;

  public SharedPreferences settings;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private final String[] TITLES = {
                getString(R.string.tab_notifications),
                getString(R.string.tab_subscriptions)
        };

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }
    }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_subscription);

      TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
      Fabric.with(this, new TwitterCore(authConfig), new Digits());
      setContentView(R.layout.activity_subscription);

      mPager = (ViewPager) findViewById(R.id.pager);
      mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
      mPager.setAdapter(mPagerAdapter);
      final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
      tabs.setViewPager(mPager);

      settings = getPreferences(0);
      final SharedPreferences.Editor editor = settings.edit();

      final DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
      digitsButton.setCallback(new AuthCallback() {
          @Override
          public void success(DigitsSession session, String phoneNumber) {
              digitsButton.setVisibility(View.INVISIBLE);
              tabs.setVisibility(View.VISIBLE);
              mPager.setVisibility(View.VISIBLE);
              editor.putString("loggedIn", phoneNumber);
          }

          @Override
          public void failure(DigitsException exception) {
              // Do something on failure
          }
      });

      String loggedIn = settings.getString("loggedIn", "");
      if(loggedIn.length() < 1){
          tabs.setVisibility(View.INVISIBLE);
          mPager.setVisibility(View.INVISIBLE);
      }else{
        digitsButton.setVisibility(View.INVISIBLE);
      }
    setUpPusher();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_subscription, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void onAddGame(View view) {
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
  }

  void setUpPusher() {
    Pusher pusher = new Pusher(BuildConfig.PUSHER_KEY);
    pusher.connect();

    // Subscribe to channel.
    Channel channel = pusher.subscribe("notifications");

    // Listen for specific events.
    channel.bind("new_event", new SubscriptionEventListener() {
      @Override
      public void onEvent(String channel, String event, String data) {
        Gson gson = new Gson();
        final Game game = gson.fromJson(data, Game.class);

          Log.e("SubscriptionActivity", "GAME TYPE: " + game.type);
        // If you are subscribed to the newly created game, you are notified.
        if (settings.getBoolean(game.type, false)) {
          // Do something significant here.
          Log.d("Pusher Recieved", "Found event: " + game.name);
          // Make toast.
          runOnUiThread(new Runnable() {
            public void run() {
              Toast.makeText(getApplicationContext(), "Found event: " + game.name, Toast.LENGTH_LONG).show();
            }
          });

        }
      }
    });

  }
}

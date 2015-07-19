package wesports.com.wesports;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import io.fabric.sdk.android.Fabric;


public class SubscriptionActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;


    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());
        setContentView(R.layout.activity_subscription);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
                                     @Override
                                     public void success(DigitsSession session, String phoneNumber) {
                                         // Do something with the session
                                     }

                                     @Override
                                     public void failure(DigitsException exception) {
                                         // Do something on failure
                                     }
                                 });

        layout = (LinearLayout) findViewById(R.id.subscription_layout);

        SharedPreferences settings = this.getPreferences(0);
        final SharedPreferences.Editor editor = settings.edit();

        // Restore preferences
        for(final String game : getResources().getStringArray(R.array.games_array)){

            LinearLayout view = new LinearLayout(this);
            Switch toggle = new Switch(this);
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
            view.addView(toggle,horizontalSpacingLayout);

            TextView text = new TextView(this);
            text.setTextSize(20);
            text.setTextColor(Color.BLACK);
            text.setText(game);
            view.addView(text);

            LinearLayout.LayoutParams verticalSpacingLayout =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            verticalSpacingLayout.setMargins(10, 0, 0, 10);
            layout.addView(view);
        }
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
}

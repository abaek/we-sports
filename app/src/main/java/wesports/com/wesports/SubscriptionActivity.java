package wesports.com.wesports;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


public class SubscriptionActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_subscription);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.subscriptions);
    getSupportActionBar().setElevation(10);

    LinearLayout layout = (LinearLayout) findViewById(R.id.subscription_list);

    SharedPreferences settings;
    settings = getPreferences(0);
    final SharedPreferences.Editor editor = settings.edit();

    // Restore preferences
    for (final String game : getResources().getStringArray(R.array.games_array)) {
      LinearLayout view = new LinearLayout(this);
      Switch toggle = new Switch(this);
      boolean subscribed = settings.getBoolean(game, false);
      toggle.setChecked(subscribed);
      toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          editor.putBoolean(game, isChecked);
          editor.apply();
        }
      });
      LinearLayout.LayoutParams horizontalSpacingLayout =
              new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                      LinearLayout.LayoutParams.WRAP_CONTENT);
      horizontalSpacingLayout.setMargins(0, 0, 24, 0);
      view.addView(toggle, horizontalSpacingLayout);

      TextView text = new TextView(this);
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
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Back button.
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

package wesports.com.wesports;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


public class SubscriptionActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_subscription);
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

  public void backPressed(View view) {
    finish();
  }

}

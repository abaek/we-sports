package wesports.com.wesports;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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
        switch(mPageNumber){
            case NOTIFICATIONS: view = getNotificationCard();
                break;
            case SUBSCRIPTIONS: view = getSubscriptionCard();
                break;
        }
        return view;
    }

    public View getNotificationCard(){
        return null;
    }

    public View getSubscriptionCard(){
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
}
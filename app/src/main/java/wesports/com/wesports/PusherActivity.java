package wesports.com.wesports;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import org.json.JSONObject;


public class PusherActivity extends Activity {

//  Channel testChannel;
  PrivateChannel channel;
  EditText eventName;
  Button createEventButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pusher);
    eventName = (EditText) findViewById(R.id.event_name);
    createEventButton = (Button) findViewById(R.id.create_event_button);
    createEventButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        createEvent(eventName.getText().toString());
      }
    });
    setUpPusher();
  }

  void setUpPusher() {
    // Create a new Pusher instance
    HttpAuthorizer authorizer = new HttpAuthorizer("http://we-sports.herokuapp.com/pusher/auth");
    PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
    Pusher pusher = new Pusher("", options);

    pusher.connect(new ConnectionEventListener() {
      @Override
      public void onConnectionStateChange(ConnectionStateChange change) {
        printToast("State changed from " + change.getPreviousState() +
                " to " + change.getCurrentState());
      }

      @Override
      public void onError(String message, String code, Exception e) {
        printToast("There was a problem connecting!");
      }
    }, ConnectionState.ALL);

    // Subscribe to a channel
    channel = pusher.subscribePrivate("private-notifications",
            new PrivateChannelEventListener() {
              @Override
              public void onAuthenticationFailure(String message, Exception e) {
                printToast("Authentication failed");
              }

              @Override
              public void onSubscriptionSucceeded(String channelName) {
                printToast("Subscription successful");
                channel.trigger("client-myEvent", "{\"myName\":\"Bob\"}");
              }

              @Override
              public void onEvent(String channelName, String eventName, String data) {
                printToast("Event");
              }
            });

//    testChannel = pusher.subscribe("test");
//
//    // Bind to listen for events called "my-event" sent to "my-channel"
//    testChannel.bind("test", new SubscriptionEventListener() {
//      @Override
//      public void onEvent(String channel, String event, String data) {
//        printToast("Received event with data: " + data);
//      }
//    });

    // Disconnect from the service (or become disconnected my network conditions)
//    pusher.disconnect();

    // Reconnect, with all channel subscriptions and event bindings automatically recreated
//    pusher.connect();
    // The state change listener is notified when the connection has been re-established,
    // the subscription to "my-channel" and binding on "my-event" still exist.

  }

  void printToast(final String text) {
    runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  void createEvent(String eventName) {
    JSONObject message = new JSONObject();
    try {
      message.put("name", eventName);
      channel.trigger("client-create", message.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

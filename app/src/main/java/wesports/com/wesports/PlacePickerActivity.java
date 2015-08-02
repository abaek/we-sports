package wesports.com.wesports;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlacePickerActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

  protected GoogleApiClient mGoogleApiClient;
  protected Location mLastLocation;
  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private LocationRequest mLocationRequest;
  protected static final String TAG = "MapActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_place_picker);
    setUpMapIfNeeded();
    buildGoogleApiClient();

    mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
            .setFastestInterval(1 * 1000); // 1 second, in milliseconds
  }

  protected synchronized void buildGoogleApiClient() {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
  }


  @Override
  public void onConnected(Bundle bundle) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    if (mLastLocation != null) {
      handleNewLocation(mLastLocation);
    } else {
      LocationServices.FusedLocationApi
              .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
  }

  private void handleNewLocation(Location location) {
    Log.d(TAG, location.toString());
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    MarkerOptions options = new MarkerOptions()
            .position(latLng)
            .title("I am here!");
    mMap.addMarker(options);
    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.i(TAG, "Connection suspended");
    mGoogleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    // might need more error handling
    Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
  }

  @Override
  protected void onResume() {
    super.onResume();
    setUpMapIfNeeded();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mGoogleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
      mGoogleApiClient.disconnect();
    }
  }

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call {@link #setUpMap()} once when {@link #mMap} is not null.
   * <p/>
   * If it isn't installed {@link SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   * install/update the Google Play services APK on their device.
   * <p/>
   * A user can return to this FragmentActivity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   * have been completely destroyed during this process (it is likely that it would only be
   * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
   * method in {@link #onResume()} to guarantee that it will be called.
   */
  private void setUpMapIfNeeded() {
    // Do a null check to confirm that we have not already instantiated the map.
    if (mMap == null) {
      // Try to obtain the map from the SupportMapFragment.
      mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
              .getMap();
      // Check if we were successful in obtaining the map.
      if (mMap != null) {
        setUpMap();
      }
    }
  }

  /**
   * This is where we can add markers or lines, add listeners or move the camera. In this case, we
   * just add a marker near Africa.
   * <p/>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  private void setUpMap() {
    MarkerOptions options = new MarkerOptions()
            // .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
            .position(new LatLng(0, 0))
            .title("Marker");
    mMap.addMarker(options);
  }

  @Override
  public void onLocationChanged(Location location) {
    handleNewLocation(location);
  }
}

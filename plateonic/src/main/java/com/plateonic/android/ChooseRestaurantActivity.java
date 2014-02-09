package com.plateonic.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.plateonic.R;
import com.plateonic.android.com.plateonic.utils.FacebookDetails;
import com.plateonic.android.com.plateonic.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Oleg on 2/8/14.
 */
public class ChooseRestaurantActivity extends ActionBarActivity {

    private static final String BASE_URL_TA = "http://api.tripadvisor.com/api/partner/1.0/location/60745/restaurants?key=92C34F58BB4F4E03894F5D171B79857E&cuisines=";
    private static final int RADIUS = 3; // miles
    private static final String TAG = "plateonic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserest);
        FacebookDetails userDetails = (FacebookDetails) getIntent().getSerializableExtra("fb");

        if (userDetails != null) {
            setTitle(userDetails.getFirstName() + ", choose a restaurant");
        } else {
            setTitle("Choose a restaurant");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_rest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Session.getActiveSession().closeAndClearTokenInformation();
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
                return true;
            case R.id.action_tripadvisor:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tripadvisor.com/"));
                startActivity(browserIntent);
                Toast.makeText(this, "Thanks to TripAdvisor for this app's restaurant recommendations!", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static class ChooseRestaurantFragment extends Fragment implements Response.ErrorListener, Response.Listener<JSONObject>, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

        private GridView mCategories;
        private LocationClient mLocationClient;
        private Button mRefresh;
        private ListView mList;
        private LinearLayout mRestLayout;

        private ChooseWhereEatActivity.Cuisine mCuisine;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_chooserest, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mRefresh = (Button) getView().findViewById(R.id.rerandomize);
            mList = (ListView) getView().findViewById(R.id.restaurants);
            mRestLayout = (LinearLayout) getView().findViewById(R.id.rest_layout);

            mCuisine = (ChooseWhereEatActivity.Cuisine) getActivity().getIntent().getSerializableExtra("cuisine");
            Drawable bg = getResources().getDrawable(mCuisine.getImageResId()).mutate();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.1f);
            bg.setColorFilter(new ColorMatrixColorFilter(cm));
            //noinspection deprecation
            mRestLayout.setBackgroundDrawable(bg);

            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getActivity(), BrowsePeopleActivity.class);
                    intent.putExtras(getActivity().getIntent());
                    intent.putExtra("restaurant", adapterView.getItemAtPosition(i).toString());
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            if (GooglePlayServicesUtil.
                    isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
                Toast.makeText(getActivity(), "Google Play connection services not available. Exiting.", Toast.LENGTH_LONG);
                getActivity().finish();
                return;
            }

            mLocationClient = new LocationClient(getActivity(), this, this);
            getRestaurants();

            mRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    getRestaurants();
                }
            });
        }

        @Override
        public void onStart() {
            mLocationClient.connect();
            super.onStart();
        }

        @Override
        public void onStop() {
            mLocationClient.disconnect();
            super.onStop();
        }

        protected void getRestaurants() {
            mRefresh.setEnabled(false);
            String url = BASE_URL_TA + getString(mCuisine.getTitleResId());
            Log.i("plateonic", "Fetching url: " + url);
            JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null, this, this);
            VolleySingleton.getInstance(getActivity().getApplicationContext()).add(request);
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(getActivity(), "Network error: " + volleyError, Toast.LENGTH_SHORT).show();
            mRefresh.setEnabled(true);
        }

        @Override
        public void onResponse(JSONObject jsonObject) {
            Log.i(TAG, "Received response: " + jsonObject);
            mRefresh.setEnabled(true);

            Location lastLocation = mLocationClient.getLastLocation();
            Log.i(TAG, "last location: " + lastLocation.toString());

            try {
                double uLat = lastLocation.getLatitude();
                double uLong = lastLocation.getLongitude();
                List<JSONObject> distSortedRestaurants = new LinkedList<JSONObject>();

                JSONArray restaurants = (JSONArray) jsonObject.get("data");
                for (int i = 0; i < restaurants.length(); ++i) {
                    JSONObject res = (JSONObject) restaurants.get(i);
                    double rLat = res.getDouble("latitude");
                    double rLong = res.getDouble("longitude");
                    double dist = distanceBetween(rLat, rLong, uLat, uLong);
                    if (dist < RADIUS) {
                        res.put("distanceCalculated", dist);
                        distSortedRestaurants.add(res);
                    }
                }

                if (distSortedRestaurants.size() > 5) {
                    for (int i = distSortedRestaurants.size(); i > 5; i--) {
                        Random rand = new Random();
                        int x = rand.nextInt(distSortedRestaurants.size());
                        distSortedRestaurants.remove(x);
                    }
                }

                displayRestaurants(distSortedRestaurants);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

        }

        private void displayRestaurants(List<JSONObject> distSortedRestaurants) {
            Log.i(TAG, "Displaying restaurants: " + distSortedRestaurants.size());

            mList.setAdapter(new RestaurantAdapter(getActivity(), distSortedRestaurants));
        }

        private static class RestaurantAdapter extends ArrayAdapter<JSONObject> {

            final Activity mActivity;
            final List<JSONObject> mRestaurants;

            RestaurantAdapter(Activity activity, List<JSONObject> restaurants) {
                super(activity, R.layout.restaurant_item, restaurants);
                mActivity = activity;
                mRestaurants = restaurants;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(R.layout.restaurant_item, parent, false);
                }

                TextView restName = (TextView) convertView.findViewById(R.id.rest_name);
                TextView restAddress = (TextView) convertView.findViewById(R.id.rest_address);
                TextView restDist = (TextView) convertView.findViewById(R.id.rest_distance);
                RatingBar restRating = (RatingBar) convertView.findViewById(R.id.rest_rating);

                JSONObject rest;
                rest = mRestaurants.get(position);
                try {

                    restName.setText(rest.optString("name", "[Unknown]"));
                    JSONObject addr = (JSONObject) rest.get("address_obj");
                    StringBuilder addrText = new StringBuilder();
                    addrText.append(addr.optString("street1"));
                    addrText.append('\n');
                    addrText.append(addr.optString("city"));
                    addrText.append(", ");
                    addrText.append(addr.optString("state").substring(0, 2).toUpperCase());
                    addrText.append(' ');
                    String zipcode = addr.optString("postalcode");
                    if (zipcode != null) {
                        if (zipcode.length() >= 5) {
                            addrText.append(zipcode.substring(0, 5));
                        } else {
                            addrText.append(zipcode);
                        }
                    }
                    restAddress.setText(addrText.toString());
                    double dist = rest.optDouble("distanceCalculated");
                    restDist.setText(new DecimalFormat("#.#").format(dist) + "mi");
                    restRating.setRating((float) rest.optDouble("rating", 0.0));
                } catch (Exception e) {
                    // also bad practice to catch all exceptions, for now it's fine
                    Toast.makeText(mActivity, "Error. Please try again in a bit.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "ERROR: " + rest.toString(), e);
                }

                return convertView;
            }
        }

        public static double toRad(double degrees) {
            return degrees * Math.PI / 180;
        }

        public static double distanceBetween(double lat1, double long1, double lat2, double long2) {
            double EARTH_RADIUS = 3963.1906;
            double dLat = toRad(lat1 - lat2);
            double dLon = toRad(long1 - long2);
            double l1 = toRad(lat1);
            double l2 = toRad(lat2);

            double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)) + (Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(l1) * Math.cos(l2));
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return EARTH_RADIUS * c;
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "GPS: CONNECTED");
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "GPS: DISCONNECTED");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "GPS: FAILED: " + connectionResult);
        }
    }

}
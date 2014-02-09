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
public class BrowsePeopleActivity extends ActionBarActivity {

    private static final String BASE_URL_TA = "172.16.7.237:8080/";
    private static final String TAG = "plateonic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserest);
        if (LoginActivity.mName != null) {
            setTitle(LoginActivity.mName + ", choose a person");
        } else {
            setTitle("Choose a person");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public static class BrowsePeopleFragment extends Fragment implements Response.ErrorListener, Response.Listener<JSONObject> {

        private ListView people;
        private ListView mList;
        private LinearLayout mRestLayout;

        private ChooseWhereEatActivity.Cuisine mCuisine;
        private JSONObject mRestaurant;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_browseperson, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mList = (ListView) getView().findViewById(R.id.people);

            mCuisine = (ChooseWhereEatActivity.Cuisine) getActivity().getIntent().getSerializableExtra("cuisine");
            Drawable bg = getResources().getDrawable(mCuisine.getImageResId()).mutate();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.1f);
            bg.setColorFilter(new ColorMatrixColorFilter(cm));
            //noinspection deprecation
            mList.setBackgroundDrawable(bg);

            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getActivity(), "Clicked on: " + i, Toast.LENGTH_SHORT).show();
                }
            });

            if (GooglePlayServicesUtil.
                    isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
                Toast.makeText(getActivity(), "Google Play connection services not available. Exiting.", Toast.LENGTH_LONG);
                getActivity().finish();
                return;
            }

            getPeople();

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

                try {
                    TextView restName = (TextView) convertView.findViewById(R.id.rest_name);
                    TextView restAddress = (TextView) convertView.findViewById(R.id.rest_address);
                    TextView restDist = (TextView) convertView.findViewById(R.id.rest_distance);
                    RatingBar restRating = (RatingBar) convertView.findViewById(R.id.rest_rating);

                    JSONObject rest = mRestaurants.get(position);

                    restName.setText(rest.optString("name", "[Unknown]"));
                    JSONObject addr = (JSONObject) rest.get("address_obj");
                    restAddress.setText(addr.optString("street1") + "\n" + addr.optString("city")
                            + ", " + addr.optString("state").substring(0, 2).toUpperCase() + " "
                            + addr.optString("postalcode").substring(0, 5));
                    double dist = rest.optDouble("distanceCalculated");
                    restDist.setText(new DecimalFormat("#.#").format(dist) + "mi");
                    restRating.setRating((float) rest.optDouble("rating", 0.0));
                } catch (Exception e) {
                    // also bad practice to catch all exceptions, for now it's fine
                    Toast.makeText(mActivity, "Error. Please try again in a bit.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                return convertView;
            }
        }


////        public void postDataBeforeRestaurant() {
////            // Create a new HttpClient and Post Header
////            HttpClient httpclient = new DefaultHttpClient();
////            HttpPost httppost = new HttpPost("");
////
////            try {
////                // Add data
////                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
////                nameValuePairs.add(new BasicNameValuePair("restaurantId", "45678")); /// Restaurant ID
////                nameValuePairs.add(new BasicNameValuePair("phoneNumber", "5084983232")); // phone
////                nameValuePairs.add(new BasicNameValuePair("id", "12345")); /// FB
////                nameValuePairs.add(new BasicNameValuePair("avatarUrl", "graph.facebook.com/username/picture")); //picture
////                nameValuePairs.add(new BasicNameValuePair("foodPreference", "Italian")); //picture
////                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
////
////                // Execute HTTP Post Request (Response = List of people)
////                HttpResponse response = httpclient.execute(httppost);
////                String responseString = inputStreamToString(response);
////
////            } catch (ClientProtocolException e) {
////                // TODO Auto-generated catch block
////            } catch (IOException e) {
////                // TODO Auto-generated catch block
////            }
////        }
//
    }

}
package com.plateonic.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.plateonic.R;
import com.plateonic.android.com.plateonic.utils.FacebookDetails;
import com.plateonic.android.com.plateonic.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Oleg on 2/8/14.
 */
public class BrowsePeopleActivity extends ActionBarActivity {

    private static final String BASE_URL_TA = "http://192.241.239.205:8080";
    private static final String FB_AVATAR = "http://graph.facebook.com/%s/picture?type=square&width=180&height=180";
    private static final String TAG = "plateonic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browseperson);
        FacebookDetails userDetails = (FacebookDetails) getIntent().getSerializableExtra("fb");

        if (userDetails.getFirstName() != null) {
            setTitle(userDetails.getFirstName() + ", choose a person");
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

    public static class BrowsePeopleFragment extends Fragment implements Response.ErrorListener {

        private ListView mList;
        private LinearLayout mLayout;
        private Button mCheckin;

        private ChooseWhereEatActivity.Cuisine mCuisine;
        private JSONObject mRestaurant;
        private FacebookDetails mUserDetails;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_browseperson, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mList = (ListView) getView().findViewById(R.id.people);
            mLayout = (LinearLayout) getView().findViewById(R.id.people_layout);
            mCheckin = (Button) getView().findViewById(R.id.checkin);
            mCuisine = (ChooseWhereEatActivity.Cuisine) getActivity().getIntent().getSerializableExtra("cuisine");
            mUserDetails = (FacebookDetails) getActivity().getIntent().getSerializableExtra("fb");

            mCheckin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: use fragment
                    new AlertDialog.Builder(getActivity()).setMessage("Check in? You will be listed as going to this restaurant for the next " + getTimeframeHours() + " hours and others interested will be able to call you at " + getPhoneNumber() + " or contact you on Facebook.").setNegativeButton("No", null).setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCheckin.setEnabled(false);
                            checkIn();
                        }
                    }).show();
                }
            });

            Drawable bg = getResources().getDrawable(mCuisine.getImageResId()).mutate();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.1f);
            bg.setColorFilter(new ColorMatrixColorFilter(cm));
            //noinspection deprecation
            mLayout.setBackgroundDrawable(bg);

            try {
                mRestaurant = new JSONObject(getActivity().getIntent().getStringExtra("restaurant"));
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "Error. Please try again later.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            getActivity().setTitle("People at " + mRestaurant.optString("name"));

            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    JSONObject user = (JSONObject) adapterView.getItemAtPosition(i);
                    String userName = user.optString("user_name", "[No name]");
                    String phone = user.optString("phone_number");
                    String id = user.optString("user_id");
                    UserDetailsDialogFragment.createInstance(userName, phone, id).show(getFragmentManager(), null);
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

        private int getTimeframeHours() {
            return 5;
        }

        protected void getPeople() {
            String url = BASE_URL_TA + "/view";
            Log.i("plateonic", "Fetching url to get people: " + url);

            JSONObject json = getRequestJson();
            if (json != null) {
                JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        boolean success = "success".equals(jsonObject.optString("status"));
                        if (!success) {
                            Toast.makeText(getActivity(), "Error: server returned failed response. " + jsonObject.toString(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            JSONArray matches = jsonObject.getJSONArray("matches");

                            List<JSONObject> matchesArray = new LinkedList<JSONObject>();
                            if (matches != null && matches.length() > 0) {
                                for (int i = 0; i < matches.length(); i++) {
                                    matchesArray.add(matches.getJSONObject(i));
                                }
                            }
                            displayPeople(matchesArray);
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Error parsing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, this);
                VolleySingleton.getInstance(getActivity().getApplicationContext()).add(request);
            }
        }

        protected void checkIn() {
            String url = BASE_URL_TA + "/new";
            Log.i("plateonic", "Fetching url to checkin: " + url);
            mCheckin.setEnabled(false);

            JSONObject json = getRequestJson();
            if (json != null) {
                JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        boolean success = "success".equals(jsonObject.optString("status"));
                        if (!success) {
                            Toast.makeText(getActivity(), "Error: server returned failed response. " + jsonObject.toString(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "You are going to " + mRestaurant.optString("name") + "!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, this);
                VolleySingleton.getInstance(getActivity().getApplicationContext()).add(request);
            }
        }

        private JSONObject getRequestJson() {
            JSONObject jsonReq = new JSONObject();
            try {
                jsonReq.put("restaurant_id", mRestaurant.get("location_id"));
                jsonReq.put("phone_number", getPhoneNumber());
                jsonReq.put("user_id", mUserDetails.getId());
                jsonReq.put("user_name", mUserDetails.getFirstName());
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "Error: " + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return jsonReq;
        }

        private String getPhoneNumber() {
            TelephonyManager tManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            return tManager.getLine1Number();
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(getActivity(), "Network error: " + volleyError, Toast.LENGTH_SHORT).show();
            //mRefresh.setEnabled(true);
        }

        private void displayPeople(List<JSONObject> people) {
            Log.i(TAG, "Got this many people: " + people.size());
            mList.setAdapter(new PeopleAdapter(getActivity(), people));
        }

        private static class PeopleAdapter extends ArrayAdapter<JSONObject> {

            final Activity mActivity;
            final List<JSONObject> mPeople;

            PeopleAdapter(Activity activity, List<JSONObject> people) {
                super(activity, R.layout.person_item, people);
                mActivity = activity;
                mPeople = people;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(R.layout.person_item, parent, false);
                }

                try {
                    TextView personName = (TextView) convertView.findViewById(R.id.person_name);
                    NetworkImageView personAvatar = (NetworkImageView) convertView.findViewById(R.id.person_avatar);

                    JSONObject person = mPeople.get(position);

                    personName.setText(person.optString("user_name", "[Unknown]"));
                    personName.setTag(person.optString("user_id"));
                    String avatarUrl = String.format(FB_AVATAR, person.optString("user_id"));
                    personAvatar.setImageUrl(avatarUrl, VolleySingleton.getImageLoader(getContext().getApplicationContext()));

                } catch (Exception e) {
                    // also bad practice to catch all exceptions, for now it's fine
                    Toast.makeText(mActivity, "Error. Please try again in a bit.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                return convertView;
            }
        }
    }

}
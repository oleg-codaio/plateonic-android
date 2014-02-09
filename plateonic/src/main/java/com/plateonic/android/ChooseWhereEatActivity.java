package com.plateonic.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.manuelpeinado.glassactionbar.GlassActionBarHelper;
import com.manuelpeinado.glassactionbar.GridViewScrollObserver;
import com.plateonic.R;
import com.plateonic.android.com.plateonic.utils.FacebookDetails;

import java.io.Serializable;

public class ChooseWhereEatActivity extends ActionBarActivity {

    private static final Cuisine[] CATEGORIES = new Cuisine[]{
            new Cuisine(R.string.allll, R.drawable.allll),
            new Cuisine(R.string.ameri, R.drawable.ameri),
            new Cuisine(R.string.asian, R.drawable.asian),
            new Cuisine(R.string.cafeb, R.drawable.cafeb),
            new Cuisine(R.string.chine, R.drawable.chine),
            new Cuisine(R.string.desse, R.drawable.desse),
            /*new Cuisine(R.string.exoti, R.drawable.exoti),*/
            new Cuisine(R.string.frenc, R.drawable.frenc),
            new Cuisine(R.string.india, R.drawable.india),
            new Cuisine(R.string.itali, R.drawable.itali),
            new Cuisine(R.string.japan, R.drawable.japan),
            new Cuisine(R.string.medit, R.drawable.medit),
            new Cuisine(R.string.mexic, R.drawable.mexic),
            new Cuisine(R.string.pizza, R.drawable.pizza),
            new Cuisine(R.string.veget, R.drawable.veget)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        FacebookDetails details = (FacebookDetails) getIntent().getSerializableExtra("fb");
        String firstName = details.getFirstName();
        if (firstName != null) {
            setTitle(firstName + ", " + getString(R.string.choose_category_question).toLowerCase());
        } else {
            setTitle(getString(R.string.choose_category_question));
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
                i.putExtras(getIntent());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public static class CategoryAdapter extends ArrayAdapter<Cuisine> {

        final Activity mActivity;
        final Cuisine[] mObjects;

        public CategoryAdapter(Activity activity, Cuisine[] objects) {
            super(activity, R.layout.category_item, objects);
            mActivity = activity;
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.category_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.text);
            ImageView image = (ImageView) convertView.findViewById(R.id.image);

            title.setText(mObjects[position].getTitleResId());
            image.setImageResource(mObjects[position].getImageResId());

            return convertView;
        }

    }

    public static class ChooseWhereEatFragment extends Fragment {

        private GlassActionBarHelper helper;

        private GridView mCategories;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            helper = (GlassActionBarHelper) new GlassActionBarHelper().contentLayout(R.layout.fragment_choose);
            return helper.createView(getActivity());
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mCategories = (GridView) getView().findViewById(R.id.categories);
            mCategories.setAdapter(new CategoryAdapter(getActivity(), CATEGORIES));
            GridViewScrollObserver observer = new GridViewScrollObserver(mCategories);
            observer.setOnScrollUpAndDownListener(helper);
            mCategories.setOnItemClickListener(mOnItemClickListener);
        }

        private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ChooseRestaurantActivity.class);
                intent.putExtras(getActivity().getIntent());
                intent.putExtra("cuisine", CATEGORIES[i]);

                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        };

    }

    // note on android parcelable is faster, but i'm lazy right now
    public static class Cuisine implements Serializable {

        private int titleResId;
        private int imageResId;

        public Cuisine(int titleResId, int imageResId) {
            this.titleResId = titleResId;
            this.imageResId = imageResId;
        }

        public int getTitleResId() {
            return titleResId;
        }

        public int getImageResId() {
            return imageResId;
        }

    }

}

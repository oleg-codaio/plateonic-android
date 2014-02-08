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
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.manuelpeinado.glassactionbar.GlassActionBarHelper;
import com.manuelpeinado.glassactionbar.ListViewScrollObserver;
import com.plateonic.android.com.plateonic.android.widgets.GridViewScrollObserver;

public class ChooseWhereEatActivity extends ActionBarActivity {

    private static final Category[] CATEGORIES = new Category[]{
            new Category(R.string.allll, R.drawable.allll),
            new Category(R.string.ameri, R.drawable.ameri),
            new Category(R.string.asian, R.drawable.asian),
            new Category(R.string.cafeb, R.drawable.cafeb),
            new Category(R.string.chine, R.drawable.chine),
            new Category(R.string.desse, R.drawable.desse),
            /*new Category(R.string.exoti, R.drawable.exoti),*/
            new Category(R.string.frenc, R.drawable.frenc),
            new Category(R.string.india, R.drawable.india),
            new Category(R.string.itali, R.drawable.itali),
            new Category(R.string.japan, R.drawable.japan),
            new Category(R.string.medit, R.drawable.medit),
            new Category(R.string.mexic, R.drawable.mexic),
            new Category(R.string.pizza, R.drawable.pizza),
            new Category(R.string.veget, R.drawable.veget)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        setTitle("What do you want to eat?");
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
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public static class CategoryAdapter extends ArrayAdapter<Category> {

        final Activity activity;
        final Category[] objects;

        public CategoryAdapter(Activity activity, Category[] objects) {
            super(activity, R.layout.category_item, objects);
            this.activity = activity;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.category_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.text);
            ImageView image = (ImageView) convertView.findViewById(R.id.image);

            title.setText(objects[position].getTitleResId());
            image.setImageResource(objects[position].getImageResId());

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
//            setContentView(helper.createView(getActivity()));

            //return inflater.inflate(R.layout.fragment_choose, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mCategories = (GridView) getView().findViewById(R.id.categories);
            mCategories.setAdapter(new CategoryAdapter(getActivity(), CATEGORIES));
            GridViewScrollObserver observer = new GridViewScrollObserver(mCategories);
            observer.setOnScrollUpAndDownListener(helper);
            mCategories.setOnItemClickListener(mOnItemClickListener);

            onSessionOpened(Session.getActiveSession());
        }

        private void onSessionOpened(Session session) {
            // make request to the /me API
            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        Toast.makeText(getActivity(), "Hello, " + user.getFirstName() + "!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "category tapped: " + getString(CATEGORIES[i].getTitleResId()), Toast.LENGTH_SHORT).show();
            }
        };

    }


    private static class Category {

        private int titleResId;
        private int imageResId;

        public Category(int titleResId, int imageResId) {
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

package com.plateonic.android;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.plateonic.R;
import com.plateonic.android.com.plateonic.utils.VolleySingleton;

/**
 * Created by Oleg on 2/9/14.
 */
public class UserDetailsDialogFragment extends DialogFragment {
    private static final String FB_AVATAR = "http://graph.facebook.com/%s/picture?type=square&width=180&height=180";

    NetworkImageView mUserAvatar;
    TextView mUserName;
    Button mFb;
    Button mCall;

    public static UserDetailsDialogFragment createInstance(String userName, String phoneNumber, String userId) {
        UserDetailsDialogFragment frag = new UserDetailsDialogFragment();
        Bundle args = new Bundle(4);
        args.putString("userName", userName);
        args.putString("phoneNumber", phoneNumber);
        args.putString("userId", userId);
        frag.setArguments(args);
        return frag;
    }

    private UserDetailsDialogFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.person_details, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserAvatar = (NetworkImageView) view.findViewById(R.id.person_avatar);
        mUserName = (TextView) view.findViewById(R.id.person_name);
        mCall = (Button) view.findViewById(R.id.call);
        mFb = (Button) view.findViewById(R.id.fb);

        final String fbId = getArguments().getString("userId");
        mUserName.setText(getArguments().getString("userName"));
        String avatarUrl = String.format(FB_AVATAR, fbId);
        mUserAvatar.setImageUrl(avatarUrl, VolleySingleton.getImageLoader(getActivity().getApplicationContext()));

        final String number = getArguments().getString("phoneNumber");
        if (number != null) {
            mCall.setText(number);
        } else {
            mCall.setText("No number");
            mCall.setEnabled(false);
        }

        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "tel:" + number;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                startActivity(intent);
            }
        });

        mFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // http://stackoverflow.com/questions/4810803/open-facebook-page-from-android-app
                try {
                    // if FB app exists
                    getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + fbId)));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + fbId)));
                }
            }
        });
    }
}

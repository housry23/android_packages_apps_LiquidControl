
package com.liquid.control.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liquid.control.R;

public class DeveloperPreference extends Preference {

    TextView mName;
    TextView twitter;
    TextView donateText;
    ImageView icon;
    LinearLayout twitterLayout;
    LinearLayout donateLayout;
    ImageView donateButton;
    ImageView avatar;

    String twitterHandle;
    String donateLink;
    String name;
    int avatarImage;

    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeveloperPreference);
        twitterHandle = a.getString(R.styleable.DeveloperPreference_twitterHandle);
        donateLink = a.getString(R.styleable.DeveloperPreference_donateLink);
        avatarImage = a.getResourceId(R.styleable.DeveloperPreference_avatar, 0);
        a.recycle();

    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        View layout = View.inflate(getContext(), R.layout.devs_preference, null);

        mName = (TextView) layout.findViewById(com.android.internal.R.id.title);
        twitter = (TextView) layout.findViewById(R.id.twitter_handle);
        donateButton = (ImageView) layout.findViewById(R.id.donate_button);
        twitterLayout = (LinearLayout) layout.findViewById(R.id.twitter_layout);
        icon = (ImageView) layout.findViewById(R.id.twitter_icon);
        avatar = (ImageView) layout.findViewById(R.id.avatar);

        return layout;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        twitter.setText(twitterHandle);
        icon.setImageResource(R.drawable.twitter_bird);

        if (twitterHandle == null) {
            twitterLayout.setVisibility(View.GONE);
        } else
            this.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uriUrl = Uri.parse("http://twitter.com/#!/" + twitterHandle);
                    Intent twitter = new Intent(Intent.ACTION_VIEW, uriUrl);
                    getContext().startActivity(twitter);
                    return true;
                }
            });

        if (donateLink == null)
            donateButton.setVisibility(View.GONE);
        else
            donateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uriUrl = Uri.parse(donateLink);
                    Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
                    getContext().startActivity(donate);
                }
            });

        // no clicky action for our images
        if (avatarImage == 0)
            avatar.setVisibility(View.GONE);
        else
            avatar.setImageResource(avatarImage);
    }
}

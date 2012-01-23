
package com.liquid.control.fragments;

import java.util.ArrayList;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.liquid.control.R;
import com.liquid.control.fragments.StatusBarToggles.TogglesLayout;
import com.liquid.control.widgets.TouchInterceptor;

public class Navbar extends PreferenceFragment implements
        OnPreferenceChangeListener {

    // move these later
    private static final String PREF_EANBLED_BUTTONS = "enabled_buttons";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_HOME_LONGPRESS = "long_press_home";

    // move these later
    ColorPickerPreference mNavigationBarColor;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mHomeLongpress;
    Preference mNavBarEnabledButtons;
    Preference mLayout;

    private final String[] buttons = {
            "HOME", "BACK", "TASKS", "SEARCH"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();

        menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
        menuDisplayLocation.setOnPreferenceChangeListener(this);
        menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_LOCATION,
                0) + "");

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
        mNavBarMenuDisplay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_VISIBILITY,
                0) + "");

        mHomeLongpress = (ListPreference) findPreference(PREF_HOME_LONGPRESS);
        mHomeLongpress.setOnPreferenceChangeListener(this);
        mHomeLongpress.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
                0) + "");

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mNavBarEnabledButtons = findPreference(PREF_EANBLED_BUTTONS);
        mLayout = findPreference("buttons");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (preference == mNavBarEnabledButtons) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

            ArrayList<String> enabledToggles = getButtonsStringArray(this.getActivity()
                    .getApplicationContext());

            boolean checkedToggles[] = new boolean[buttons.length];

            for (int i = 0; i < checkedToggles.length; i++) {
                if (enabledToggles.contains(buttons[i])) {
                    checkedToggles[i] = true;
                }
            }

            builder.setTitle("Choose which buttons to use");
            builder.setCancelable(false);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setMultiChoiceItems(buttons,
                    checkedToggles,
                    new OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            String toggleKey = (buttons[which]);

                            if (isChecked)
                                addButton(getActivity(), toggleKey);
                            else
                                removeButton(getActivity(), toggleKey);
                        }
                    });

            AlertDialog d = builder.create();
            d.show();

            return true;
        } else if (preference == mLayout) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NavbarLayout fragment = new NavbarLayout();
            ft.addToBackStack("navbar_layout");
            ft.replace(this.getId(), fragment);
            ft.commit();

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == menuDisplayLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;

        } else if (preference == mHomeLongpress) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
                    Integer.parseInt((String) newValue));
            return true;
        }
        return false;
    }

    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = getButtonsStringArray(context);
        enabledToggles.add(key);
        setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = getButtonsStringArray(context);
        enabledToggles.remove(key);
        setButtonsFromStringArray(context, enabledToggles);
    }

    public static class NavbarLayout extends ListFragment {
        private static final String TAG = "NavbarLayout";

        Context mContext;
        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(
                    R.layout.order_power_widget_buttons_activity, container, false);

            return v;
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mButtonList = this.getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        };

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> toggles = getButtonsStringArray(mContext);

                // move the button
                if (from < toggles.size()) {
                    String toggle = toggles.remove(from);

                    if (to <= toggles.size()) {
                        toggles.add(to, toggle);

                        // save our buttons
                        setButtonsFromStringArray(mContext, toggles);

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<Toggle> mToggles;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                ArrayList<String> toggles = getButtonsStringArray(mContext);

                mToggles = new ArrayList<Toggle>();
                for (String toggle : toggles) {
                    mToggles.add(new Toggle(toggle, 0));
                }
            }

            public int getCount() {
                return mToggles.size();
            }

            public Object getItem(int position) {
                return mToggles.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                Toggle toggle = mToggles.get(position);
                final TextView name = (TextView) v.findViewById(R.id.name);
                name.setText(toggle.getId());
                return v;
            }
        }

        public class Toggle {
            private String mId;
            private int mTitleResId;

            public Toggle(String id, int titleResId) {
                mId = id;
                mTitleResId = titleResId;
            }

            public String getId() {
                return mId;
            }

            public int getTitleResId() {
                return mTitleResId;
            }
        }
    }

    public static void setButtonsFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (String s : newGoodies)
            newToggles += s + "|";

        // remote last |
        newToggles = newToggles.substring(0, newToggles.length() - 1);

        Settings.System.putString(c.getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTONS,
                newToggles);
    }

    public static ArrayList<String> getButtonsStringArray(Context c) {
        String clusterfuck = Settings.System.getString(c.getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS);

        if (clusterfuck == null) {
            clusterfuck = "BACK|HOME|TASKS";
        }

        String[] togglesStringArray = clusterfuck.split("\\|");
        ArrayList<String> iloveyou = new ArrayList<String>();
        for (String s : togglesStringArray) {
            iloveyou.add(s);
        }

        return iloveyou;
    }
}
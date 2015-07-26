package com.ideasfrombrain.search_based_launcher_v3;


import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


import android.view.inputmethod.InputMethodManager;

import android.widget.ViewAnimator;

import android.widget.EditText;
import android.widget.RadioGroup;


@SuppressWarnings("Convert2Lambda")
public class MainActivity extends Activity {


    public static final int FIRST_INDEX = 0;
    static String APP_PACKAGE_NAME = "com.ideasfrombrain.search_based_launcher_v3";
    public static final App MENU_APP = new App(APP_PACKAGE_NAME + ".Menu", " Menu-Launcher", APP_PACKAGE_NAME + ".Menu");

    boolean newerAndroidVersion = true;

    List<App> pkg = new ArrayList<>();
    List<App> filtered = new ArrayList<>();
    Set<App> extra = new HashSet<>();
    Set<App> hidden = new HashSet<>();
    List<App> recent = new ArrayList<>();

    EditText dialogInput;

    private final BroadcastReceiver mPkgApplicationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApps();
            recent.retainAll(pkg);
            extra.retainAll(pkg);
            hidden.retainAll(pkg);
            filtered.retainAll(pkg);
            saveExtRemLists();
            loadApps();
            refresh();
        }
    };
    private SearchText searchText;
    private AppListView appListView;
    private AutostartButton autostartButton;
    private WifiButton wifiButton;
    private BluetoothButton bluetoothButton;
    private CameraButton cameraButton;
    private RadioButtons radioButtons;
    private PreferencesAdapter preferencesAdapter;

    private void registerIntentReceivers() {
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addDataScheme("package");
        registerReceiver(mPkgApplicationsReceiver, pkgFilter);
    }

    public void loadApps() {
        Log.d("DEBUG", "start loading apps");
        Log.d("DEBUG", "activity arrays prepared");

        final Intent main = new Intent(Intent.ACTION_MAIN, null);
        final PackageManager pm = getPackageManager();

        pkg.clear();

        switch (radioButtons.getCheckedRadioButton()) {
            case 0:
                loadNormal(main, pm);
                break;
            case 1:
                loadAll(main, pm);
                break;
            case 2:
                pkg.addAll(extra);
                break;
            case 3:
                pkg.addAll(hidden);
                break;
        }
        pkg.add(MENU_APP);

    }

    private void loadAll(Intent main, PackageManager pm) {
        final List<ResolveInfo> launchables = pm.queryIntentActivities(main, 0);
        for (ResolveInfo launchable : launchables) {
            App app = new App(launchable.activityInfo.packageName, deriveNick(launchable), launchable.activityInfo.name);
            pkg.add(app);
        }
    }

    private String deriveNick(ResolveInfo launchable) {
        String[] split = launchable.activityInfo.name.split("\\.");
        String nick = split[1];
        for (int j = 2; j < split.length; j++) {
            nick = nick + ":" + split[j];
        }
        return nick;
    }

    private void loadNormal(Intent main, PackageManager pm) {
        pkg.addAll(extra);

        main.addCategory(Intent.CATEGORY_LAUNCHER); // will show only Regular Apps
        final List<ResolveInfo> launchables = pm.queryIntentActivities(main, 0);

        for (ResolveInfo launchable : launchables) {
            String nick = launchable.activityInfo.name;
            String name = launchable.activityInfo.packageName;
            String activity = launchable.activityInfo.loadLabel(pm).toString();
            final App app = new App(name, nick, activity);
            if (!hidden.contains(app)) {
                pkg.add(app);
            }
        }
    }

    public void loadExtRemLists() {
        try {
            extra = preferencesAdapter.loadSet("extra");
            hidden = preferencesAdapter.loadSet("hidden");
        } catch (Exception e) {
            saveExtRemLists();
        }
    }

    public void saveExtRemLists() {
        preferencesAdapter.saveSet(extra, "extra");
        preferencesAdapter.saveSet(hidden, "hidden");
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_MENU) {
            myShowNext(false);
        } else if (keycode == KeyEvent.KEYCODE_SEARCH) {
            startSearch("", false, null, true);
        } else if (keycode == KeyEvent.KEYCODE_BACK) {
            ViewAnimator mViewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
            if (mViewAnimator.getDisplayedChild() == 1) {
                myShowNext(false);
            }
        } else {
            return super.onKeyUp(keycode, event);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferencesAdapter = new PreferencesAdapter(this);
        appListView = new AppListView(this);
        searchText = new SearchText(this);
        autostartButton = new AutostartButton(this);
        wifiButton = new WifiButton(this);
        bluetoothButton = new BluetoothButton(this);
        cameraButton = new CameraButton(this);
        createMenuDonateButton();
        radioButtons = new RadioButtons(this);
        setAndroidVersion();
        setAppLists(savedInstanceState);
        registerIntentReceivers();
    }

    private void createMenuDonateButton() {
        findViewById(R.id.donateButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(marketIntent);
            }
        });
    }

    private void setAppLists(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadExtRemLists();
            loadApps();
        } else {
            pkg = new ArrayList<>(App.getApps(new HashSet<>(savedInstanceState.getStringArrayList("pkg"))));
            extra = App.getApps(new HashSet<>(savedInstanceState.getStringArrayList("extra")));
            hidden = App.getApps(new HashSet<>(savedInstanceState.getStringArrayList("hidden")));
        }
    }

    private void setAndroidVersion() {
        String Aversion = android.os.Build.VERSION.RELEASE;
        newerAndroidVersion = !(Aversion.startsWith("1.") ||
                Aversion.startsWith("2.0") ||
                Aversion.startsWith("2.1"));
    }


    public void myShowNext(Boolean DoLoadApps) {
        searchText.setNormalColor();
        ViewAnimator mViewAnimator = (ViewAnimator) findViewById(R.id.viewAnimator);
        mViewAnimator.showNext();

        if (mViewAnimator.getDisplayedChild() == 0) {
            if (DoLoadApps) {
                loadApps();
            }

            if (radioButtons.getCheckedRadioButton() > 0) {
                searchText.setSpaceCharacterToText();
                radioButtons.setInvisible();
            } else {
                wifiButton.setVisibleIfAvailable();
                bluetoothButton.setVisibleIfAvailable();
                cameraButton.setVisibleIfAvailable();
                searchText.clearText();
            }

        } else {
            RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
            mRadioGroup.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        radioButtons.save();
        savedInstanceState.putStringArrayList("pkg", new ArrayList<>(App.getJson(new HashSet<>(pkg))));
        savedInstanceState.putStringArrayList("extra", new ArrayList<>(App.getJson(extra)));
        savedInstanceState.putStringArrayList("hidden", new ArrayList<>(App.getJson(hidden)));
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchText.setNormalColor();
        if ((radioButtons.getCheckedRadioButton() == 0) && autostartButton.isOn()) {
            searchText.clearText();
        }
        toggleKeyboard();
    }


    @Override
    public void onDestroy() {
        wifiButton.unregisterReceiver();
        unregisterReceiver(mPkgApplicationsReceiver);
        super.onDestroy();
    }

    public void runApp(int appIndex) {
        final App app = filtered.get(appIndex);
        searchText.setActivatedColor();
        recent.remove(app);
        toggleKeyboard();
        if (app.isMenu()) {
            myShowNext(false);
        } else {
            try {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(app.getName(), app.getActivity()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                searchText.setNormalColor();
                toggleKeyboard();
            }
        }
    }

    private void toggleKeyboard() {
        if (!newerAndroidVersion) {
            final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    public void refresh() {
        filtered.clear();
        String filterText = searchText.getFilterText();
        for (App app: recent) {
            if (app.getNick().toLowerCase().matches(filterText)) {
                filtered.add(app.getAsRecent());
            }
        }
        for (App app: pkg) {
            if (app.getNick().toLowerCase().matches(filterText) && (recent.contains(app))) {
                filtered.add(app);
            }
        }
        if (filtered.size() == 1 && autostartButton.isOn()) {
            runApp(FIRST_INDEX);
        } else {
            appListView.viewAppList(filtered);
        }
    }

    public boolean showOptionsForApp(final int appIndex) {
        final App app = filtered.get(appIndex);
        if ((app.getActivity().equals(APP_PACKAGE_NAME + ".Menu"))) {
            return false;
        }

        switch (radioButtons.getCheckedRadioButton()) {
            case 0:
                showNormalOptions(app);
                break;
            case 1:
                showAddExtraAppOptions(app);
                break;
            case 2:
                showRemoveExtraAppOptions(app);
                break;
            case 3:
                showUnhideAppOptions(app);
                break;
        }
        return false;
    }

    private void showUnhideAppOptions(final App app) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(app.getNick());

        dialogInput = new EditText(this);
        dialogInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogInput.setText(app.getNick());

        dialog.setView(dialogInput);
        dialog.setMessage("Remove this activity (hidden app) from hidden applications list?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (hidden.remove(app)) {
                    MainActivity.this.saveExtRemLists();
                    loadApps();
                    refresh();
                    dialog.dismiss();
                }
            }
        });

        dialog.create().show();
    }

    private void showRemoveExtraAppOptions(final App app) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(app.getNick());

        dialogInput = new EditText(this);
        dialogInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogInput.setText(app.getNick());

        dialog.setView(dialogInput);
        dialog.setMessage("Remove this (extra added list of all activities) activity from applications list?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (extra.remove(app) | recent.remove(app)) {
                    MainActivity.this.saveExtRemLists();
                    loadApps();
                    refresh();
                    dialog.dismiss();
                }
            }
        });

        dialog.create().show();
    }

    private void showAddExtraAppOptions(App app) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(app.getNick());

        dialogInput = new EditText(this);
        dialogInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogInput.setText(app.getNick());

        dialog.setView(dialogInput);
        dialog.setMessage("Add this activity to applications list?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                extra.add(app);
                app.setNick(dialogInput.getText().toString());
                saveExtRemLists();
                loadApps();
                refresh();
                dialog.dismiss();
            }
        });

        dialog.create().show();
    }

    private void showNormalOptions(final App app) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(app.getNick());
        if (!extra.contains(app) && !hidden.contains(app)) {
            showHideApp(app, dialog);
        } else if (extra.contains(app) && hidden.contains(app)) {
            showRenamedApp(app, dialog);
        } else if (extra.contains(app)) {
            showExtraAddedApp(app, dialog);
        } else {
            //FIXME
        }
        dialog.create().show();
    }

    private void showExtraAddedApp(final App app, AlertDialog.Builder dialog) {
        dialog.setMessage("Remove activity " + app + " from extra added (to applications list) list ?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                extra.remove(app);
                saveExtRemLists();
                loadApps();
                refresh();
                dialog.dismiss();
            }
        });
    }

    private void showRenamedApp(final App app, AlertDialog.Builder dialog) {
        dialogInput = new EditText(this);
        dialogInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogInput.setText(app.getNick());

        dialog.setView(dialogInput);
        dialog.setMessage("This application is in both add and hide lists, thus is probably renamed.");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (hidden.contains(app)) {
                    extra.remove(app);
                    saveExtRemLists();
                    loadApps();
                    refresh();
                    dialog.dismiss();
                }
            }
        });

        dialog.setNeutralButton("Uninstall", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + app.getName()));
                startActivity(intent);
                refresh();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                app.setNick(dialogInput.getText().toString());
                saveExtRemLists();
                loadApps();
                refresh();
                dialog.dismiss();
            }
        });
    }

    private void showHideApp(final App app, AlertDialog.Builder dialog) {
        dialogInput = new EditText(this);
        dialogInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        dialogInput.setText(app.getNick());

        dialog.setView(dialogInput);
        dialog.setMessage("Hide this application from applications list, rename application (add to Hide and Extra list with diferent names) or uninstall it?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (hidden.remove(app)) {
                    saveExtRemLists();
                    loadApps();
                    refresh();
                    dialog.dismiss();
                }
            }
        });

        dialog.setNeutralButton("Uninstall", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + app.getName()));
                startActivity(intent);
                //loadApps();
                refresh();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!hidden.contains(app)) {
                    hidden.add(app);
                }
                app.setNick(dialogInput.getText().toString());
                extra.add(app);
                recent.remove(app);
                recent.add(app);
                saveExtRemLists();
                loadApps();
                refresh();
                dialog.dismiss();
            }
        });
    }
}
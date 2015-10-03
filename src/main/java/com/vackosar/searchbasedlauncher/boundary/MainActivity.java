package com.vackosar.searchbasedlauncher.boundary;


import android.view.KeyEvent;

import com.google.inject.Inject;
import com.vackosar.searchbasedlauncher.R;

import roboguice.activity.RoboActivity;
import roboguice.event.EventManager;
import roboguice.inject.ContentView;

@SuppressWarnings("Convert2Lambda")
@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

    public static final String INITIAL_SEARCH_QUERY = "";
    @Inject private MenuButton menuButton;
    @Inject private AppsView appsView;
    @Inject private EventManager eventManager;
    @Inject private WifiButton wifiButton;
    @Inject private BluetoothButton bluetoothButton;
    @Inject private CameraButton cameraButton;
    @Inject private WikiButton wikiButton;

    @Override
    public boolean onKeyUp(int keycode, KeyEvent event) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_BACK:
                eventManager.fire(new MenuButton.ToggleEvent());
                break;
            case KeyEvent.KEYCODE_SEARCH:
                startSearch(INITIAL_SEARCH_QUERY, false, null, true);
                break;
            default:
                return super.onKeyUp(keycode, event);
        }
        return true;
    }
}
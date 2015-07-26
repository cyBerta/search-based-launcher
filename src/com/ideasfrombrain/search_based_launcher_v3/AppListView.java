package com.ideasfrombrain.search_based_launcher_v3;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AppListView implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    final MainActivity mainActivity;
    final ListView listView;

    public AppListView (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        listView = (ListView) mainActivity.findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainActivity.getAppListManager().runApp(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return mainActivity.getAppListManager().showOptionsForApp(position);
    }

    public void viewAppList(List<App> appList) {
        final List<String> list = new ArrayList<String>();
        for (App app: appList) {
            list.add(app.getNick());
        }
        listView.setAdapter(new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, android.R.id.text1, list));
    }
}

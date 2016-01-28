package com.tomandfelix.stapp2.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;

import java.util.ArrayList;
import java.util.Set;

public class SensorSelection extends ServiceActivity {
    private ArrayList<String> deviceNames;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sensor_selection);
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.sensor_selection_list);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            //Get paired devices and add their address to the list
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0) {
                deviceNames = new ArrayList<>();
                for(BluetoothDevice d:pairedDevices) {
                    if(d.getName().contains("RN42")) {
                        deviceNames.add(d.getAddress());
                    }
                }
            } else {
                deviceNames = new ArrayList<>();
                deviceNames.add("No devices Found");
            }

            //Populate the listView
            ArrayAdapter deviceNamesAdapter = new ArrayAdapter<>(this, R.layout.list_item_devices, deviceNames);
            listView.setAdapter(deviceNamesAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra("address", deviceNames.get(position));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            finish();
        }
    }
}
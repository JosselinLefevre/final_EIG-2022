package eig_2022.com.tp2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Josselin on 18/11/2017.
 * BT AsyncTask
 */

class BTManagerTask extends AsyncTask<Void, Boolean, Boolean> {

    private Context context;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> mBoundedDevice = null;
    private ArrayList<String> mBoundedDevicesArray = new ArrayList<>();
    private Spinner mSpinnner;
    private MainActivity mainActivity;

    Set<BluetoothDevice> getBoundedDevice() {
        return mBoundedDevice;
    }

    BTManagerTask(Context _activityContext, Spinner _spinner, Activity activity) {
        this.context = _activityContext;
        this.mSpinnner = _spinner;
        this.mainActivity = (MainActivity) activity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        publishProgress(true);

        mBoundedDevicesArray.add(context.getString(R.string.none));

        mBoundedDevice = mBluetoothAdapter.getBondedDevices();

        while (mBoundedDevice.size() == 1) {
            mBoundedDevice = mBluetoothAdapter.getBondedDevices();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        publishProgress(false);

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        for (BluetoothDevice device : mBoundedDevice) {
            mBoundedDevicesArray.add(device.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.spinner_item, mBoundedDevicesArray);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        mSpinnner.setAdapter(adapter);
    }

    @Override
    protected void onProgressUpdate(Boolean... progress) {
        mainActivity.showProgress(progress[0], mSpinnner);
    }

    @Override
    protected void onCancelled() {
    }
}

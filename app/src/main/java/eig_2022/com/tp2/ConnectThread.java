package eig_2022.com.tp2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Josselin on 14/11/2017.
 * Joli programme tout bo
 */

class ConnectThread extends Thread {
    private BluetoothSocket mmSocket = null;
    private int mFrequency = 50;
    private int mDirection = 4;

    ConnectThread(BluetoothDevice _device, UUID _uuid) {
        Log.wtf("ConnectThread", "Begin");

        BluetoothSocket tmp = null;

        try {
            tmp = _device.createRfcommSocketToServiceRecord(_uuid);
        } catch (IOException ignored) {
        }
        mmSocket = tmp;
    }

    void setDirection(int _direction) {
        this.mDirection = _direction;
        Log.wtf("setDirection-String", _direction + "");

        Log.wtf("Send", mFrequency + mDirection + "");
    }

    void setFrequency(int _frequency) {
        this.mFrequency = _frequency;
        Log.wtf("setFrequency", _frequency + "");
    }

    private void manageConnectedSocket(BluetoothSocket bluetoothSocket) {
        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();

            while (true) {
                outputStream.write(mFrequency + mDirection);
                Log.wtf("Send", mFrequency + mDirection + "");
                Thread.sleep(30);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Log.wtf("ConnectThread", "Attempt to connect");
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
            return;
        }
        manageConnectedSocket(mmSocket);
    }

    void cancel() {
        try {
            mmSocket.close();
        } catch (IOException ignored) {
        }
    }

}

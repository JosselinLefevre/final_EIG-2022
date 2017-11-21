package eig_2022.com.tp2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Josselin on 14/11/2017.
 *
 */

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread(BluetoothAdapter bluetoothAdapter, UUID uuid) {
        Log.wtf("AcceptThread", "Begin");

        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Vic T", uuid);
        } catch (IOException ignored) {
            Log.wtf("ERROR", "Constructeur");
        }
        mmServerSocket = tmp;
    }

    public void run() {
        Log.wtf("AcceptThread", "run()");
        BluetoothSocket socket;
        while (true) {
            try {
                Log.wtf("AcceptThread","define socket");
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            if (socket != null) {
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            else{
                Log.wtf("AcceptThread", "BAD");
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket bluetoothSocket){
        Log.wtf("AcceptThread", "manageConnectedSocket()");
        try {
            InputStream outputStream = bluetoothSocket.getInputStream();

            while(true){
                Log.wtf("AcceptThread", outputStream.read()+"");
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException ignored) { }
    }
}
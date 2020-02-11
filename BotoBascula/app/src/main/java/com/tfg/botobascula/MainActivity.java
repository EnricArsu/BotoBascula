package com.tfg.botobascula;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.botobascula.bluetooth.BluetoothCommunication;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.createInstance(getApplicationContext());
        Button connectBtn = (Button) findViewById(R.id.connectbtn);
        Button updateBtn = (Button) findViewById(R.id.updatebtn);
        final TextView pesText = (TextView) findViewById(R.id.pesText);
        pesText.setText("Benvingut a l'app de mesura");
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Botó que conecta a openscale
                invokeConnectToBluetoothDevice();
                Toast.makeText(getApplicationContext(), "Connectant a la bàscula", Toast.LENGTH_SHORT).show();
                pesText.setText("Mantingui la bàscula encesa fins que es realitzi la connexió");
            }

        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateText();
            }
        });

    }


    public void updateText(){
        String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/measures.txt";
        File measures = new File(filePath);
        final TextView pesText = findViewById(R.id.pesText);
        pesText.setText("El pes és de: "+ readFromFile(measures) + "kg.");
    }

    private void invokeConnectToBluetoothDevice() {

        final App app = App.getInstance();


        String deviceName = "0131986C2D3F38C1A4";
        String hwAddress = "A4:C1:38:3F:2D:6C";

        if (!BluetoothAdapter.checkBluetoothAddress(hwAddress)) {

            Toast.makeText(getApplicationContext(), "Dispositiu no conectat", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (!bluetoothManager.getAdapter().isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            return;
        }

        Toast.makeText(getApplicationContext(), "Trying to connect to device", Toast.LENGTH_SHORT).show();


        if (!app.connectToBluetoothDevice(deviceName, hwAddress, callbackBtHandler)) {
            Toast.makeText(getApplicationContext(), "Bàscula no suportada", Toast.LENGTH_SHORT).show();
        }
    }

    private final Handler callbackBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            BluetoothCommunication.BT_STATUS btStatus = BluetoothCommunication.BT_STATUS.values()[msg.what];

            switch (btStatus) {

                case INIT_PROCESS:

                    Toast.makeText(getApplicationContext(),"Bluetooth initializing", Toast.LENGTH_SHORT).show();
                    Timber.d("Bluetooth initializing");
                    break;
                case CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                    Timber.d("Bluetooth connection lost");
                    break;
                case NO_DEVICE_FOUND:

                    Toast.makeText(getApplicationContext(), "No Bluetooth device found", Toast.LENGTH_SHORT).show();
                    Timber.e("No Bluetooth device found");
                    break;
                case CONNECTION_RETRYING:
                    Toast.makeText(getApplicationContext(), "No Bluetooth device found retrying", Toast.LENGTH_SHORT).show();
                    Timber.e("No Bluetooth device found retrying");
                    break;
                case CONNECTION_ESTABLISHED:
                    Toast.makeText(getApplicationContext(), "Bluetooth connection successfully established", Toast.LENGTH_SHORT).show();
                    Timber.d("Bluetooth connection successfully established");
                    break;
                case CONNECTION_DISCONNECT:
                    Toast.makeText(getApplicationContext(), "Bluetooth connection successfully disconnected", Toast.LENGTH_SHORT).show();
                    Timber.d("Bluetooth connection successfully disconnected");
                    break;
                case UNEXPECTED_ERROR:
                    Toast.makeText(getApplicationContext(), "Bluetooth unexpected error", Toast.LENGTH_SHORT).show();
                    Timber.e("Bluetooth unexpected error: %s", msg.obj);
                    break;
                case SCALE_MESSAGE:
                    String toastMessage = String.format(getResources().getString(msg.arg1), msg.obj);
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public String readFromFile(File file){

        FileInputStream fis = null;
        String textWeight="";
        try {
            fis = new FileInputStream(file);

            System.out.println("Total file size to read (in bytes) : "
                    + fis.available());

            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and display it
                textWeight=textWeight + (char) content;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (fis != null) {
                    fis.close();
                }
                return textWeight;
            } catch (IOException ex) {
                ex.printStackTrace();
                return textWeight;
            }
        }
    }



}

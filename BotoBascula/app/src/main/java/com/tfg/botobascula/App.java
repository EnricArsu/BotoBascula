package com.tfg.botobascula;

import com.tfg.botobascula.bluetooth.*;

import android.content.Context;

import android.os.Handler;


import timber.log.Timber;

public class App {
    private static App instance;

    private BluetoothCommunication btDeviceDriver;


    private Context context;



    private App(Context context) {
        this.context = context;

        btDeviceDriver = null;


    }


    public static void createInstance(Context context) {
        if (instance != null) {
            return;
        }

        instance = new App(context);
    }

    public static App getInstance() {
        if (instance == null) {
            throw new RuntimeException("No App instance created");
        }

        return instance;
    }


    public boolean connectToBluetoothDevice(String deviceName, String hwAddress, Handler callbackBtHandler) {
        Timber.d("Trying to connect to bluetooth device [%s] (%s)", hwAddress, deviceName);

        disconnectFromBluetoothDevice();

        btDeviceDriver = BluetoothFactory.createDeviceDriver(context, deviceName);
        if (btDeviceDriver == null) {
            return false;
        }

        btDeviceDriver.registerCallbackHandler(callbackBtHandler);
        btDeviceDriver.connect(hwAddress);


        return true;
    }

    public boolean disconnectFromBluetoothDevice() {
        if (btDeviceDriver == null) {
            return false;
        }

        Timber.d("Disconnecting from bluetooth device");
        btDeviceDriver.disconnect();
        btDeviceDriver = null;

        return true;
    }

}

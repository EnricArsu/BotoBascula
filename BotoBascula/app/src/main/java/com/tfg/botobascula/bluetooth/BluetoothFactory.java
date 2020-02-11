package com.tfg.botobascula.bluetooth;

import android.content.Context;



public class BluetoothFactory {

    public static BluetoothCommunication createDeviceDriver(Context context, String deviceName) {

        // BS444 || BS440
        if (deviceName.startsWith("013197") || deviceName.startsWith("013198") || deviceName.startsWith("0202B6")) {
            return new BluetoothMedisanaBS44x(context, true);
        }

        //BS430
        if (deviceName.startsWith("0203B")) {
            return new BluetoothMedisanaBS44x(context, false);
        }
        return null;
    }
}

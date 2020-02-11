package com.tfg.botobascula.bluetooth;

import android.content.Context;
import android.widget.Toast;

import com.tfg.botobascula.Converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class BluetoothMedisanaBS44x extends BluetoothCommunication {
    private final UUID WEIGHT_MEASUREMENT_SERVICE = BluetoothGattUuid.fromShortCode(0x78b2);
    private final UUID WEIGHT_MEASUREMENT_CHARACTERISTIC = BluetoothGattUuid.fromShortCode(0x8a21); // indication, read-only
    private final UUID FEATURE_MEASUREMENT_CHARACTERISTIC = BluetoothGattUuid.fromShortCode(0x8a22); // indication, read-only
    private final UUID CMD_MEASUREMENT_CHARACTERISTIC = BluetoothGattUuid.fromShortCode(0x8a81); // write-only
    private final UUID CUSTOM5_MEASUREMENT_CHARACTERISTIC = BluetoothGattUuid.fromShortCode(0x8a82); // indication, read-only


    private boolean applyOffset;

    private float weight;
    // Scale time is in seconds since 2010-01-01
    private static final long SCALE_UNIX_TIMESTAMP_OFFSET = 1262304000;


    public BluetoothMedisanaBS44x(Context context, boolean applyOffset) {
        super(context);
        this.applyOffset = applyOffset;
        this.weight = 0;
    }

    @Override
    public String driverName() {
        return "Medisana BS44x";
    }

    @Override
    protected boolean onNextStep(int stepNr) {
        switch (stepNr) {
            case 0:
                // set indication on for feature characteristic
                setIndicationOn(WEIGHT_MEASUREMENT_SERVICE, FEATURE_MEASUREMENT_CHARACTERISTIC);
                break;
            case 1:
                // set indication on for weight measurement
                setIndicationOn(WEIGHT_MEASUREMENT_SERVICE, WEIGHT_MEASUREMENT_CHARACTERISTIC);
                break;
            case 2:
                // set indication on for custom5 measurement
                setIndicationOn(WEIGHT_MEASUREMENT_SERVICE, CUSTOM5_MEASUREMENT_CHARACTERISTIC);
                break;
            case 3:
                // send magic number to receive weight data
                long timestamp = new Date().getTime() / 1000;
                if (applyOffset) {
                    timestamp -= SCALE_UNIX_TIMESTAMP_OFFSET;
                }
                byte[] date = Converters.toInt32Le(timestamp);

                byte[] magicBytes = new byte[]{(byte) 0x02, date[0], date[1], date[2], date[3]};

                writeBytes(WEIGHT_MEASUREMENT_SERVICE, CMD_MEASUREMENT_CHARACTERISTIC, magicBytes);
                break;
            case 4:
                Toast.makeText(context.getApplicationContext(), "Pugeu a la bàscula", Toast.LENGTH_SHORT).show();
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onBluetoothNotify(UUID characteristic, byte[] value) {
        final byte[] data = value;
        if (characteristic.equals(WEIGHT_MEASUREMENT_CHARACTERISTIC)) {
            parseWeightData(data);
        }
    }

    private void parseWeightData(byte[] weightData) {
        float weight = Converters.fromUnsignedInt16Le(weightData, 1) / 100.0f;
        String textWeight = String.valueOf(weight);
        String filePath = context.getFilesDir().getPath().toString() + "/measures.txt";

        File weightFile = new File(filePath);
        if (!weightFile.exists()) {
            try {
                weightFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream oFile = null;
        try {
            oFile = new FileOutputStream(weightFile, false);
            oFile.write(textWeight.getBytes());
            oFile.flush();
            oFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
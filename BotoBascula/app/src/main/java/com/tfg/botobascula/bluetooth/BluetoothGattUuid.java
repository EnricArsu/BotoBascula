package com.tfg.botobascula.bluetooth;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

public class BluetoothGattUuid {
    private static final String STANDARD_SUFFIX = "-0000-1000-8000-00805f9b34fb";

    public static final UUID fromShortCode(long code) {
        return UUID.fromString(String.format("%08x%s", code, STANDARD_SUFFIX));
    }

    public static final String prettyPrint(UUID uuid) {
        if (uuid == null) {
            return "null";
        }

        String str = uuid.toString();

        if (str.endsWith(STANDARD_SUFFIX)) {
            String code = str.substring(0, str.length() - STANDARD_SUFFIX.length());
            if (code.startsWith("0000")) {
                code = code.substring(4);
            }
            str = "0x" + code;
        }

        for (Field field : BluetoothGattUuid.class.getFields()) {
            try {
                if (uuid.equals(field.get(null))) {
                    String name = field.getName();
                    name = name.substring(name.indexOf('_') + 1);
                    str = String.format("%s \"%s\"", str,
                            name.replace('_', ' ').toLowerCase(Locale.US));
                    break;
                }
            }
            catch (IllegalAccessException e) {
                // Ignore
            }
        }

        return str;
    }

}

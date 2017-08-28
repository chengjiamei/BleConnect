package roc.cjm.bleconnect.bles;

import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Created by Administrator on 2017/8/23.
 */

public final class BluetoothUuid {

    static final ParcelUuid BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
    static final int UUID_BYTES_16_BIT = 2;
    static final int UUID_BYTES_32_BIT = 4;
    static final int UUID_BYTES_128_BIT = 16;

    BluetoothUuid() {
    }

    static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
        if(uuidBytes == null) {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        } else {
            int length = uuidBytes.length;
            if(length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT && length != UUID_BYTES_128_BIT) {
                throw new IllegalArgumentException("uuidBytes length invalid - " + length);
            } else if(length == UUID_BYTES_128_BIT) {
                ByteBuffer shortUuid1 = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
                long msb = shortUuid1.getLong(8);
                long lsb = shortUuid1.getLong(0);
                return new ParcelUuid(new UUID(msb, lsb));
            } else {
                long shortUuid;
                if(length == UUID_BYTES_16_BIT) {
                    shortUuid = (long)(uuidBytes[0] & 0xff);
                    shortUuid += (long)((uuidBytes[1] & 0xff) << 8);
                } else {
                    shortUuid = (long)(uuidBytes[0] & 0xff);
                    shortUuid += (long)((uuidBytes[1] & 0xff) << 8);
                    shortUuid += (long)((uuidBytes[2] & 0xff) << 16);
                    shortUuid += (long)((uuidBytes[3] & 0xff) << 24);
                }

                long msb1 = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
                long lsb1 = BASE_UUID.getUuid().getLeastSignificantBits();
                return new ParcelUuid(new UUID(msb1, lsb1));
            }
        }
    }
}

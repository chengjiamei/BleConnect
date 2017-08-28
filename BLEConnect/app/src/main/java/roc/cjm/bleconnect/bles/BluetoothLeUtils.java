package roc.cjm.bleconnect.bles;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.RequiresPermission;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/23.
 */

public class BluetoothLeUtils {

    BluetoothLeUtils() {
    }

    static String toString(SparseArray<byte[]> array) {
        if(array == null) {
            return "null";
        } else if(array.size() == 0) {
            return "{}";
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');

            for(int i = 0; i < array.size(); ++i) {
                buffer.append(array.keyAt(i)).append("=").append(Arrays.toString((byte[])array.valueAt(i)));
            }

            buffer.append('}');
            return buffer.toString();
        }
    }

    static <T> String toString(Map<T, byte[]> map) {
        if(map == null) {
            return "null";
        } else if(map.isEmpty()) {
            return "{}";
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            Iterator it = map.entrySet().iterator();

            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Object key = entry.getKey();
                buffer.append(key).append("=").append(Arrays.toString((byte[])map.get(key)));
                if(it.hasNext()) {
                    buffer.append(", ");
                }
            }

            buffer.append('}');
            return buffer.toString();
        }
    }

    static boolean equals(SparseArray<byte[]> array, SparseArray<byte[]> otherArray) {
        if(array == otherArray) {
            return true;
        } else if(array != null && otherArray != null) {
            if(array.size() != otherArray.size()) {
                return false;
            } else {
                for(int i = 0; i < array.size(); ++i) {
                    if(array.keyAt(i) != otherArray.keyAt(i) || !Arrays.equals((byte[])array.valueAt(i), (byte[])otherArray.valueAt(i))) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    static <T> boolean equals(Map<T, byte[]> map, Map<T, byte[]> otherMap) {
        if(map == otherMap) {
            return true;
        } else if(map != null && otherMap != null) {
            if(map.size() != otherMap.size()) {
                return false;
            } else {
                Set keys = map.keySet();
                if(!keys.equals(otherMap.keySet())) {
                    return false;
                } else {
                    Iterator var3 = keys.iterator();

                    Object key;
                    do {
                        if(!var3.hasNext()) {
                            return true;
                        }

                        key = var3.next();
                    } while(Objects.deepEquals(map.get(key), otherMap.get(key)));

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH")
    static void checkAdapterStateOn(BluetoothAdapter adapter) {
        if(adapter == null || adapter.getState() != 12) {
            throw new IllegalStateException("BT Adapter is not turned ON");
        }
    }
}

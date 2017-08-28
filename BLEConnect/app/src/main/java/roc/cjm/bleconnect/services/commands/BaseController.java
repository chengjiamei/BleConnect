package roc.cjm.bleconnect.services.commands;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by Administrator on 2017/8/24.
 */

public abstract class BaseController extends BluetoothGattCallback {

    private String TAG = "BaseController";

    public final static int STATE_CONNECTED = BluetoothGatt.STATE_CONNECTED;
    public final static int STATE_CONNECTING = BluetoothGatt.STATE_CONNECTING;
    public final static int STATE_DISCONNECTED = BluetoothGatt.STATE_DISCONNECTED;
    public final static int STATE_DISCONNECTING = BluetoothGatt.STATE_DISCONNECTING;
    public final static int STATE_DISCOVERING = 4;///正在查找服务
    public final static int STATE_DISCOVERED = 5;////已经搜索到服务


    ///Connection State
    public static String ACTION_CONNECTION_STATE = "roc.cjm.bleconnect.services.commands.ACTION_CONNECTION_STATE";
    public static String EXTRA_CONNECTION_STATE = "roc.cjm.bleconnect.services.commands.EXTRA_CONNECTION_STATE";
    public static String EXTRA_CONNECTION_DEVICE = "roc.cjm.bleconnect.services.commands.EXTRA_CONNECTION_DEVICE";

    public static String ACTION_SERVICE_DISCOVERED = "roc.cjm.bleconnect.services.commands.ACTION_SERVICE_DISCOVERED";
    public static String EXTRA_SERVICE_LIST = "roc.cjm.bleconnect.services.commands.EXTRA_SERVICE_LIST";

    public static String ACTION_DISCOVER_STATE = "roc.cjm.bleconnect.services.commands.ACTION_SERVICE_DISCOVERED";
    public static String EXTRA_DISCOVER_STATE = "roc.cjm.bleconnect.services.commands.EXTRA_DISCOVER_STATE";

    public BluetoothGatt mBluetoothGatt;
    public int connectState;
    public int discoverState;
    public BluetoothDevice mDevice;
    private OnControllerListener onControllerListener;

    public void connect(BluetoothDevice device) {
        this.mDevice = device;
    }

    public void disconnect() {

    }

    public int getConnectState() {
        return connectState;
    }

    public int getDiscoverState() {
        return discoverState;
    }

    public void setOnControllerListener(OnControllerListener onControllerListener) {
        this.onControllerListener = onControllerListener;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);

    }

    public void readDescriptor(BluetoothGattDescriptor descriptor) {
        if(connectState == BaseController.STATE_CONNECTED) {
            mBluetoothGatt.readDescriptor(descriptor);
        }
    }

    public boolean write(UUID serviceUUID, UUID charactericUUID, byte[] value) {
        if (connectState == STATE_CONNECTED) {
            BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(charactericUUID);
                if (characteristic != null) {
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristic.setValue(value);
                    setDeviceBusy(mBluetoothGatt);
                    return mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }
        }
        return false;
    }

    public void read(UUID serviceUUID, UUID charactericUUID) {
        if (connectState == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(serviceUUID).getCharacteristic(charactericUUID);
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    private void setDeviceBusy(BluetoothGatt gatt) {
        if (gatt != null) {
            try {
                Field mDeviceBusy = gatt.getClass().getDeclaredField("mDeviceBusy");
                if (mDeviceBusy != null) {
                    mDeviceBusy.setAccessible(true);
                    boolean device = (boolean) mDeviceBusy.get(gatt);
                    if (device) {
                        mDeviceBusy.set(gatt, false);
                    }

                }
            } catch (Exception localException) {
                Log.e("parserGatt()", localException.getMessage());
            }
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mBluetoothGatt != null) {
                    mBluetoothGatt.readDescriptor(descriptor);
                }
            }
        },100);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (onControllerListener != null) {
            onControllerListener.onCharacteristicWrite(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (onControllerListener != null) {
            onControllerListener.onCharacteristicRead(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (onControllerListener != null) {
            onControllerListener.onCharacteristicChanged(gatt, characteristic);
        }
    }

    public interface OnControllerListener {
        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    }

    public boolean enableNotificationIndicate(UUID serviceUUID, UUID characteristicUUID, boolean isenable) {
        if (connectState == STATE_CONNECTED) {
            BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
            if (service == null)
                return false;
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null)
                return false;
            final int properties = characteristic.getProperties();
            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE) {
                return internalEnableIndications(characteristic, isenable);
            }else if((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                return internalEnableNotifications(characteristic, isenable);
            }
            return false;
        }
        return false;
    }

    private boolean internalEnableIndications(BluetoothGattCharacteristic characteristic, boolean isenable) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, isenable);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getDescriptors().get(0).getUuid());
        if (descriptor != null) {
            descriptor.setValue(isenable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private boolean internalEnableNotifications(BluetoothGattCharacteristic characteristic, boolean isenable) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, isenable);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getDescriptors().get(0).getUuid());
        if (descriptor != null) {
            descriptor.setValue(isenable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

}

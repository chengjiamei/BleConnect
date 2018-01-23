package roc.cjm.bleconnect.services.commands;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.activitys.normal.entry.Command;

/**
 * Created by Administrator on 2017/8/24.
 */

public class CmdController extends BaseController {

    private String TAG = "CmdController";
    private Context mContext;

    private static CmdController sInstance;
    private List<Command> commandList;

    private CmdController(Context context){
        this.mContext = context;
    }

    public static CmdController getInstance(Context context) {
        if(sInstance == null) {
            synchronized (CmdController.class) {
                if(sInstance == null) {
                    sInstance = new CmdController(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @Override
    public void connect(BluetoothDevice device) {
        super.connect(device);
        BluetoothDevice bd = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
        if(mBluetoothGatt != null) {
            synchronized (mLock) {
                if(mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            mBluetoothGatt = device.connectGatt(mContext, false, this, BluetoothDevice.TRANSPORT_LE);
            return;
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt = connectGattApi21(device,mContext, false, this);
            return;
        }
        mBluetoothGatt = device.connectGatt(mContext, false, this);
    }

    private BluetoothGatt connectGattApi21(BluetoothDevice device,Context context,boolean autoconnect,BluetoothGattCallback callback1) {
        try {
            Method mod = device.getClass().getMethod("connectGatt", new Class[]{Context.class, Boolean.TYPE, BluetoothGattCallback.class, Integer.TYPE});
            if(mod != null) {
                return (BluetoothGatt) mod.invoke(device, new Object[]{context, autoconnect, callback1, Integer.valueOf(2)});
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return device.connectGatt(context, autoconnect, callback1);
    }

    public void disconnect() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        mDevice = null;
    }

    private Object mLock = new Object();
    private void close() {
        if(mBluetoothGatt != null) {
            synchronized (mLock) {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.e(TAG, "onConnectionStateChange status = "+status);
        connectState = newState;
        Intent intent = new Intent(ACTION_CONNECTION_STATE);
        intent.putExtra(EXTRA_CONNECTION_DEVICE, getDevice());
        intent.putExtra(EXTRA_CONNECTION_STATE, connectState);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        if(status == BluetoothGatt.GATT_SUCCESS) {
            if(newState == BaseController.STATE_CONNECTED) {
                discoverState = BaseController.STATE_DISCOVERING;
                intent = new Intent(ACTION_DISCOVER_STATE);
                intent.putExtra(EXTRA_DISCOVER_STATE, BaseController.STATE_DISCOVERING);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                commandHandler.sendEmptyMessageDelayed(0x01, 600);
            }else if(newState == BaseController.STATE_DISCONNECTED) {
                discoverState = BaseController.STATE_DISCOVERED;
                gatt.close();
                close();
            }
        }else {

            if(newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.close();
                close();
            }else {
                disconnect();
            }
            discoverState = BaseController.STATE_DISCOVERED;

        }

    }



    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        discoverState = BaseController.STATE_DISCOVERED;
        Intent discoverintent = new Intent(ACTION_DISCOVER_STATE);
        discoverintent.putExtra(EXTRA_DISCOVER_STATE, BaseController.STATE_DISCOVERED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(discoverintent);

        ArrayList<BluetoothGattService> listService = (ArrayList<BluetoothGattService>) gatt.getServices();
        Intent intent = new Intent(BaseController.ACTION_SERVICE_DISCOVERED);
        intent.putExtra(BaseController.EXTRA_SERVICE_LIST,listService);

        if(listService != null && listService.size()>0) {
            for (int i=0;i<listService.size();i++) {
                BluetoothGattService service = listService.get(i);
                List<BluetoothGattCharacteristic> listChara = service.getCharacteristics();
                Log.e(TAG, service.getUuid().toString());

                if(listChara != null && listChara.size()>0) {
                    for (int j=0;j<listChara.size(); j++) {
                        BluetoothGattCharacteristic characteristic = listChara.get(j);
                        Log.e(TAG, "    "+characteristic.getUuid().toString());
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        Log.e(TAG, "onServicesDiscovered");
    }

    private Handler commandHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    if(mBluetoothGatt != null) {
                        mBluetoothGatt.discoverServices();
                    }
                    break;
            }
        }
    };

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        Log.e(TAG, "onCharacteristicWrite");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        Log.e(TAG, "onCharacteristicChanged");
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
    }
}

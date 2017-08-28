package roc.cjm.bleconnect.services.commands;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.services.entry.Command;

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
        mBluetoothGatt = device.connectGatt(mContext, false, this);
    }

    public void disconnect() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        mDevice = null;
    }

    private void close() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
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
                gatt.discoverServices();
            }else if(newState == BaseController.STATE_DISCONNECTED) {
                discoverState = BaseController.STATE_DISCOVERED;
                gatt.close();
                close();
            }
        }else {
            discoverState = BaseController.STATE_DISCOVERED;
            gatt.close();
            close();
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
        ArrayList<BluetoothGattService> listService = (ArrayList<BluetoothGattService>) gatt.getServices();
        Intent intent = new Intent(BaseController.ACTION_SERVICE_DISCOVERED);
        intent.putExtra(BaseController.EXTRA_SERVICE_LIST,listService);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        ArrayList<BluetoothGattService> listService = (ArrayList<BluetoothGattService>) gatt.getServices();
        Intent intent = new Intent(BaseController.ACTION_SERVICE_DISCOVERED);
        intent.putExtra(BaseController.EXTRA_SERVICE_LIST,listService);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}

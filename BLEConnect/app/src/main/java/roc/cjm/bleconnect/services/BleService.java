package roc.cjm.bleconnect.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import roc.cjm.bleconnect.bles.ScanManager;
import roc.cjm.bleconnect.bles.ScanResult;
import roc.cjm.bleconnect.services.commands.BaseController;
import roc.cjm.bleconnect.services.commands.CmdController;

/**
 * Created by Carmy Cheng on 2017/8/23.
 */

public class BleService extends Service implements ScanManager.OnScanManagerListener, BaseController.OnControllerListener {

    private String TAG = "BleService";

    private static BleService sInstance;
    private ScanManager scanManager;
    private BaseController baseController;
    private OnBleService onBleService;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        scanManager = ScanManager.getInstance(this);
        scanManager.setScanListener(this);
    }

    public static BleService getInstance() {
        if(sInstance == null) {
            synchronized (BleService.class){
                if(sInstance != null) {
                    return sInstance;
                }
            }
        }
        return sInstance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IBleService();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setOnBleService(OnBleService onBleService) {
        this.onBleService = onBleService;
    }

    public void connect(BluetoothDevice device) {
        cancelScan();
        if(baseController == null){
            baseController = CmdController.getInstance(this);
            baseController.setOnControllerListener(this);
        }
        baseController.connect(device);
    }

    public void disconnect() {
        if(baseController == null){
            baseController = CmdController.getInstance(this);
        }
        baseController.disconnect();
    }

    public int getConnectState() {
        if(baseController == null)
            return BaseController.STATE_DISCONNECTED;
        return baseController.getConnectState();
    }

    public int getDiscoverState() {
        if(baseController == null)
            return BaseController.STATE_DISCONNECTED;
        return baseController.getDiscoverState();
    }

    public boolean enableNotifyIndicate(UUID serviceUUID, UUID characterUUID,boolean isEnable) {
        if(getConnectState() == BaseController.STATE_CONNECTED) {
            return baseController.enableNotificationIndicate(serviceUUID, characterUUID, isEnable);
        }
        return false;
    }

    public boolean write(UUID serviceUUID, UUID charactericUUID, byte[] value) {
        if (getConnectState() == BaseController.STATE_CONNECTED) {
            return baseController.write(serviceUUID, charactericUUID, value);
        }
        return false;
    }

    public void read(UUID serviceUUID, UUID charactericUUID) {
        if (getConnectState() == BaseController.STATE_CONNECTED) {
            baseController.read(serviceUUID, charactericUUID);
        }
    }

    public void readDescriptor(BluetoothGattDescriptor descriptor) {
        if (getConnectState() == BaseController.STATE_CONNECTED) {

        }
    }

    public List<BluetoothGattService> getServiceList() {
        if (getConnectState() == BaseController.STATE_CONNECTED) {
           return baseController.mBluetoothGatt.getServices();
        }
        return (new ArrayList<>());
    }

    public void setScanTime(long time) {
        if(scanManager != null) {
            scanManager.setScanTime(time);
        }
    }

    public void startScan() {
        if(scanManager != null) {
            scanManager.startLeScan();
        }
    }



    public void cancelScan() {
        if(scanManager != null) {
            scanManager.cancelLeScan();
        }
    }

    @Override
    public void onScanFinished() {
        EventBus.getDefault().post(new ScanResult(null,null,0,0));
    }

    @Override
    public void onScanFailed(int errorCode) {

    }



    public BaseController getBaseController() {
        return this.baseController;
    }

    /**
     * 扫描
     * @param results
     */
    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        EventBus.getDefault().post(results);
    }

    //BEGIN
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(onBleService != null) {
            onBleService.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(onBleService != null) {
            onBleService.onCharacteristicWrite(gatt, characteristic, status);
        }
    }

    public static String ACTION_DESCRIPTORWRITE = "roc.cjm.bleconnect.services.ACTION_DESCRIPTORWRITE";

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Intent intent = new Intent(ACTION_DESCRIPTORWRITE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.e(TAG, "onDescriptorRead");
        if(onBleService != null) {

        }
    }



    //End

    public class IBleService extends Binder{

        public BleService getBleService(){
            return BleService.this;
        }
    }

    public interface OnBleService {
        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    }
}

package roc.cjm.bleconnect.bles;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Carmy Cheng on 2017/8/23.
 *
 */

public class BleLeScanCallback implements BluetoothAdapter.LeScanCallback {

    private String TAG = "BleLeScanCallback";
    private OnLeScanCallback mScanCallback;

    public BleLeScanCallback() {

    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        Log.e(TAG, "onLeScan");
        ScanResult result = new ScanResult(bluetoothDevice,ScanRecord.parseFromBytes(scanRecord), rssi, Calendar.getInstance().getTimeInMillis());
        if(mScanCallback != null) {
            mScanCallback.onScanResult(result);
        }
    }

    public void setScanCallback(OnLeScanCallback mScanCallback) {
        this.mScanCallback = mScanCallback;
    }

    interface OnLeScanCallback {
        void onScanResult(ScanResult result);
    }
}

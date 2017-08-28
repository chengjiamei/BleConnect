package roc.cjm.bleconnect.bles;

import android.bluetooth.le.ScanCallback;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carmy Cheng on 2017/8/23.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleScanCallback extends ScanCallback {

    private String TAG = "BleScanCallback";

    private OnScanCallback onScanCallback;

    public BleScanCallback() {

    }

    public void setOnScanCallback(OnScanCallback onScanCallback) {
        this.onScanCallback = onScanCallback;
    }

    @Override
    public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
        Log.e(TAG, "onScanResult");
        /*ScanResult scanResult = new ScanResult(result.getRssi(),
                ScanRecord.parseFromBytes(result.getScanRecord().getBytes()), result.getDevice());
        super.onScanResult(callbackType, result);
        if(onScanCallback != null) {
            onScanCallback.onScanResult(scanResult);
        }*/
    }

    @Override
    public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
        super.onBatchScanResults(results);
        Log.e(TAG, "onBatchScanResults size = "+results.size());
        if(results != null && results.size()>0) {
            ArrayList<ScanResult> list = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                android.bluetooth.le.ScanResult scanResult = results.get(i);
                list.add(new ScanResult( scanResult.getDevice(), ScanRecord.parseFromBytes(scanResult.getScanRecord().getBytes()),
                        scanResult.getRssi(), scanResult.getTimestampNanos()));
                Log.e(TAG, "devicename = "+scanResult.getDevice().getName());
            }
            if(onScanCallback != null) {
                onScanCallback.onBatchScanResults(list);
            }
        }

    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    public interface OnScanCallback {
        void onScanResult(ScanResult result);
        void onBatchScanResults(List<ScanResult> results);
    }


}

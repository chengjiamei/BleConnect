package roc.cjm.bleconnect.bles;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by  Carmy Cheng on 2017/8/23.
 */

public class ScanManager implements BleScanCallback.OnScanCallback, BleLeScanCallback.OnLeScanCallback {

    public static final String ACTION_FOUND = "roc.cjm.bleconnect.bles.ACTION_FOUND";
    public static final String EXTRA_SCANRESULT = "";

    public static final int SCAN_FAILED_ALREADY_STARTED = 1;
    public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;
    public static final int SCAN_FAILED_INTERNAL_ERROR = 3;
    public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;


    public static ScanManager sInstance;
    private Context mContext;
    private BleLeScanCallback bleLeScanCallback;
    private BleScanCallback bleScanCallback;
    private static ScanHandler scanHandler;
    private long scanTime = 10000;
    private boolean isScaning = false;
    private OnScanManagerListener mScanListener;

    private ScanManager(Context context) {
        mContext = context;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanCallback = new BleScanCallback();
            bleScanCallback.setOnScanCallback(this);
        }else {*/
            bleLeScanCallback = new BleLeScanCallback();
            bleLeScanCallback.setScanCallback(this);
        //}
        scanHandler = new ScanHandler(this);
    }

    public static ScanManager getInstance(Context context) {
        if(sInstance == null ) {
            synchronized (ScanManager.class) {
                sInstance = new ScanManager(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    public void setScanTime(long scanTime) {
        this.scanTime = scanTime;
    }

    public void setScanListener(OnScanManagerListener listener) {
        this.mScanListener = listener;
    }

    /**
     *
     * @return
     */
    public boolean startLeScan() {
        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bleAdapter == null || !bleAdapter.isEnabled()){
            return false;
        }
        if(isScaning){
            isScaning = false;
            cancelLeScan();
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bleAdapter.getBluetoothLeScanner();
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setReportDelay(1000);
            ScanSettings settings = settingsBuilder.build();
            scanner.startScan(null, settings, bleScanCallback);
        }else {*/
            bleAdapter.startLeScan( bleLeScanCallback);
        //}

        isScaning = true;
        scanHandler.sendEmptyMessageDelayed(0x01, scanTime);
        return true;
    }

    public boolean cancelLeScan() {
        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bleAdapter == null || !bleAdapter.isEnabled()){
            return false;
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bleAdapter.getBluetoothLeScanner();
            scanner.stopScan(bleScanCallback);
        }else {*/
            bleAdapter.stopLeScan( bleLeScanCallback);
        //}
        if(scanHandler.hasMessages(0x01)) {
            scanHandler.removeMessages(0x01);
        }
        if(isScaning) {
            if(mScanListener != null) {
                mScanListener.onScanFinished();
            }
        }
        isScaning = false;
        return true;
    }

    //搜索回调
    @Override
    public void onScanResult(ScanResult result) {
        if(mScanListener != null) {
            mScanListener.onScanResult(result);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        if(mScanListener != null) {
            mScanListener.onBatchScanResults(results);
        }
    }

    class ScanHandler extends Handler {
        private WeakReference<ScanManager> scanManager;

        public ScanHandler(ScanManager manager) {
            scanManager = new WeakReference<ScanManager>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(scanManager.get() != null) {
                switch (msg.what) {
                    case 0x01:
                        scanManager.get().cancelLeScan();
                        break;
                }
            }
        }
    }

    public interface OnScanManagerListener {
        public void onScanResult(ScanResult result);
        public void onBatchScanResults(List<ScanResult> results);
        public void onScanFinished();
    }

}

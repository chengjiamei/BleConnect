package roc.cjm.bleconnect.activitys;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.bles.ScanRecord;
import roc.cjm.bleconnect.bles.ScanResult;
import roc.cjm.bleconnect.services.BleService;
import roc.cjm.bleconnect.services.commands.BaseController;
import roc.cjm.bleconnect.utils.DeviceConfiger;
import roc.cjm.bleconnect.utils.Util;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private String CONFIG = "btconnect";
    private String KEY_FILTER_NAME = "filter_name";
    private String KEY_FILTER_ADDRESS = "filter_address";

    private String TAG = "MainActivity";
    private BleService bleService = null;
    private Map<String, ScanResult> mapScanResult;
    private List<String> listAddress;
    private List<Boolean> listSelectState;
    private ListView listView;
    private MyAdapter myAdapter;
    private ProgressBar progressBar;
    private ProgressDialog dialog;
    private ScanResult scanResult;
    private String filter;
    private String filterName, filterAddress, tempAddress, tempName;
    private ExpandableListView expandableListView;
    private MyExpandedAdapter expandedAdapter;
    private View splitView;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "onServiceConnected");
            bleService = ((BleService.IBleService)iBinder).getBleService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
            bleService = null;
        }
    };

    public String getFilterString() {
        boolean nameEmpty = Util.isEmptyString(filterName);
        boolean addressEmpty = Util.isEmptyString(filterAddress);
        if(!nameEmpty && !addressEmpty) {
            return filterName +", "+ filterAddress;
        }
        if(!nameEmpty)
            return filterName;
        if(!addressEmpty)
            return filterAddress;
        return getString(R.string.no_filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(CONFIG , MODE_PRIVATE);
        editor = preferences.edit();

        filterAddress = preferences.getString(KEY_FILTER_ADDRESS,"");
        filterName = preferences.getString(KEY_FILTER_NAME, "");
        tempAddress = filterAddress;
        tempName = filterName;
        filter = getFilterString();

        View hv = new View(this);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceConfiger.dp2px(45));
        hv.setLayoutParams(params);
        expandableListView = (ExpandableListView) findViewById(R.id.main_expandableListView);
        expandedAdapter = new MyExpandedAdapter();
        expandableListView.setAdapter(expandedAdapter);

        splitView = findViewById(R.id.main_split_view);
        splitView.setVisibility(View.GONE);

        int wid = getResources().getDisplayMetrics().widthPixels;
        expandableListView.setIndicatorBounds(wid - DeviceConfiger.dp2px(40), wid - DeviceConfiger.dp2px(10));
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(expandableListView.isGroupExpanded(groupPosition)) {
                    splitView.setVisibility(View.GONE);
                    filterName = tempName;
                    filterAddress = tempAddress;
                    filter = getFilterString();
                    expandedAdapter.notifyDataSetChanged();
                    editor.putString(KEY_FILTER_NAME, filterName);
                    editor.putString(KEY_FILTER_ADDRESS, filterAddress);
                    editor.commit();
                    scan();
                }else {
                    splitView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });




        listView = (ListView) findViewById(R.id.main_listview);
        listView.addHeaderView(hv, null, false);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        progressBar.setVisibility(View.GONE);
        bindService(new Intent(this, BleService.class), connection ,BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

        listAddress = new ArrayList<>();
        listSelectState = new ArrayList<>();
        mapScanResult = new HashMap<>();
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem >= 0) {
                    expandableListView.setVisibility(View.VISIBLE);
                }else {
                    expandableListView.setVisibility(View.GONE);
                }
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(bleService != null) {
                    bleService.disconnect();
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseController.ACTION_CONNECTION_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        verifyPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_main_scan:
                scan();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void scan() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.no_access_to_location), Toast.LENGTH_LONG).show();
            return ;
        }
        if(adapter == null || !adapter.isEnabled()){
            Toast.makeText(this, getString(R.string.open_ble_first), Toast.LENGTH_LONG).show();
            return ;
        }
        if(bleService != null) {
            listAddress.clear();
            listSelectState.clear();
            mapScanResult.clear();
            myAdapter.notifyDataSetChanged();
            bleService.startScan();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        EventBus.getDefault().unregister(this);
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void verifyPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
    }

    @Override
    public void onClick(View v) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        switch (v.getId()) {

        }
    }

    public void addResult(ScanResult result) {
        if(result != null) {
            String mac = result.getDevice().getAddress();
            ScanResult result1 = null;

            boolean emptyName = Util.isEmptyString(filterName);
            boolean emptyAddress = Util.isEmptyString(filterAddress);
            if(!emptyAddress && !result.getDevice().getAddress().toLowerCase().contains(filterAddress.toLowerCase()) ) {
                return;
            }


            if(listAddress.contains(mac)){
                if(result.getDevice().getName() == null && result.getScanRecord().getDeviceName() == null) {
                    mapScanResult.get(mac).setRssi(result.getRssi());
                }else {
                    String name = result.getDevice().getName();
                    if(name == null) {
                        ScanRecord record = result.getScanRecord();
                        name = record.getDeviceName();
                    }
                    if(!emptyName && !(name != null && name.toLowerCase().contains(filterName.toLowerCase())))
                        return;
                    mapScanResult.remove(mac);
                    mapScanResult.put(mac, result);
                }
            }else {
                String name = result.getDevice().getName();
                if(name == null) {
                    ScanRecord record = result.getScanRecord();
                    name = record.getDeviceName();
                }
                if(!emptyName && !(name != null && name.toLowerCase().contains(filterName.toLowerCase())))
                    return;
                listAddress.add(mac);
                listSelectState.add(false);
                mapScanResult.put(mac, result);
            }
        }
    }

    public void onEventMainThread(ScanResult result) {
        if(result.getDevice() != null) {
            addResult(result);
            myAdapter.notifyDataSetChanged();
        }else {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(List<ScanResult> resultList){
        if(resultList != null && resultList.size()>0 ) {
            for (int i=0;i<resultList.size();i++) {
                addResult(resultList.get(i));
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position != 0) {
            boolean state = listSelectState.get(position-1);
            listSelectState.remove(position-1);
            listSelectState.add(position-1, !state);
            ((MyAdapter)((HeaderViewListAdapter) (parent.getAdapter())).getWrappedAdapter()).notifyDataSetChanged();
        }
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listAddress.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            final ScanResult result = mapScanResult.get(listAddress.get(position));
            final ScanRecord record = result.getScanRecord();
            if(convertView == null ) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main,null);
                holder = new Holder();
                holder.btnConnect = (Button) convertView.findViewById(R.id.item_btn_connect);
                holder.deviceAddress = (TextView) convertView.findViewById(R.id.item_deviceaddress);
                holder.deviceName = (TextView) convertView.findViewById(R.id.item_devicename);
                holder.deviceBondState = (TextView) convertView.findViewById(R.id.item_tv_bond_state);
                holder.deviceRssi = (TextView) convertView.findViewById(R.id.item_tv_rssi);
                holder.tvType = (TextView) convertView.findViewById(R.id.item_type);
                holder.tv16Bits = (TextView) convertView.findViewById(R.id.item_list_16_bit);
                holder.tv128Bits = (TextView) convertView.findViewById(R.id.item_list_128_bit);
                holder.tvFlags = (TextView) convertView.findViewById(R.id.item_Flags );
                holder.tvLocalName = (TextView) convertView.findViewById(R.id.item_complete_name);
                holder.tvServiceData = (TextView) convertView.findViewById(R.id.item_service_data);
                holder.viewLinear = convertView.findViewById(R.id.item_linear);

                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            holder.btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bleService != null) {
                        scanResult = result;
                        bleService.connect(result.getDevice());
                        String name = result.getDevice().getName();
                        if(name == null) {
                            name = record.getDeviceName();
                        }
                        dialog.setMessage(getString(R.string.connecting)+" "+name);
                        if(!dialog.isShowing()) {
                            dialog.show();
                        }
                    }
                }
            });
            holder.viewLinear.setVisibility(listSelectState.get(position)?View.VISIBLE:View.GONE);
            holder.deviceAddress.setText(result.getDevice().getAddress());
            String name = result.getDevice().getName();
            if(name == null) {
                name = record.getDeviceName();
            }
            holder.deviceName.setText(name);
            holder.deviceRssi.setText(result.getRssi()+" dBm");
            holder.deviceBondState.setText(result.getDevice().getBondState() == BluetoothDevice.BOND_BONDED?getString(R.string.bonded):getString(R.string.no_bonded));

            holder.tvType.setText(getString(R.string.types,getType(result.getDevice().getType())));
            holder.tvLocalName.setText(getString(R.string.complete_local_name,record.getDeviceName()));
            StringBuilder builder = new StringBuilder();
            Map<ParcelUuid, byte[]> maps = record.getServiceData();
            if(maps == null || maps.size() == 0){
                holder.tvServiceData.setText(getString(R.string.service_data,""));
            }else {

                for (ParcelUuid uuid:maps.keySet()) {
                    byte[] bs = maps.get(uuid);
                    byte[] bs1 = new byte[]{bs[0], bs[1]};
                    byte[] bs2 = new byte[bs.length - 2];
                    System.arraycopy(bs,2, bs2,0,bs2.length );
                    builder.append("UUID:").append(uuid.getUuid().toString().substring(4,8)).append(" Data:").append(bytes2String(bs,1));
                }
                holder.tvServiceData.setText(getString(R.string.service_data, record.getServiceData() == null ? "" : builder.toString()));
            }
            List<UUID> list16 = record.getList16BitUUIDs();
            if(list16 != null && list16.size()>0) {
                builder = new StringBuilder();
                for (int i=0; i<list16.size();i++){
                    builder.append(list16.get(i)+(i == list16.size()-1?"":"\r\n"));
                }
                holder.tv16Bits.setText(getString(R.string.list_16_bits,builder.toString()));
            }else {
                holder.tv16Bits.setText(getString(R.string.list_16_bits,""));
            }
            List<UUID> list128 = record.getList128BitUUIDs();
            if(list128 != null && list128.size()>0) {
                builder = new StringBuilder();
                for (int i=0; i<list128.size();i++){
                    builder.append(list128.get(i)+(i == list128.size()-1?"":"\r\n"));
                }
                holder.tv128Bits.setText(getString(R.string.list_128_bits,builder.toString()));
            }else {
                holder.tv128Bits.setText(getString(R.string.list_128_bits,""));
            }


            return convertView;
        }
    }

    private class Holder {
        Button btnConnect;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceBondState;
        TextView deviceRssi;
        TextView tvType;
        TextView tvFlags;
        TextView tv16Bits;
        TextView tv128Bits;
        TextView tvLocalName;
        TextView tvServiceData;
        View viewLinear;
    }

    /**
     *
     * @param bytes
     * @param order 倒序 或 正序  -1 1
     * @return
     */
    public static String bytes2String(byte[] bytes, int order){
        if(bytes == null || bytes.length == 0)
            return "";
        StringBuilder builder = new StringBuilder();
        if(order == -1) {
            builder.append("0x");
            for (int i=bytes.length-1;i>=0; i--) {
                builder.append(String.format("%02X",bytes[i]));
            }
        }else if(order == 1){
            builder.append("0x");
            for (int i=0;i<bytes.length; i++) {
                builder.append(String.format("%02X",bytes[i]));
            }
        }
        return builder.toString();
    }

    public String getType(int type) {
        switch (type) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return getString(R.string.br_edr);
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                return getString(R.string.br_edr_le);
            case BluetoothDevice.DEVICE_TYPE_LE:
                return getString(R.string.le_only);
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                return getString(R.string.unknow);
        }
        return "";
    }

    public void showMessage(int resid) {
        dialog.setMessage(getString(resid)+" "+scanResult.getDevice().getName());
        if(!dialog.isShowing()) {
            dialog.show();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Intent intentDe = null;
            if(action.equals(BaseController.ACTION_CONNECTION_STATE)) {
                int state = intent.getIntExtra(BaseController.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                switch (state) {
                    case BaseController.STATE_CONNECTED:
                        showMessage(R.string.connected);
                        if(dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        intentDe = new Intent(context, DetailActivity.class);
                        intentDe.putExtra("device", intent.getParcelableExtra(BaseController.EXTRA_CONNECTION_DEVICE));
                        startActivity(intentDe);
                        break;
                    case BaseController.STATE_CONNECTING:
                        showMessage(R.string.connecting);
                        break;
                    case BaseController.STATE_DISCONNECTED:
                        showMessage(R.string.disconnected);
                        if(dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        break;
                    case BaseController.STATE_DISCONNECTING:
                        showMessage(R.string.disconnecting);
                        break;

                }
            }
        }
    };

    private class MyExpandedAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main_header,null);
            }
            TextView tvFilter = (TextView) convertView.findViewById(R.id.item_main_tv_filter_state);
            tvFilter.setText(filter == null ?"":filter);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main_expand_view,null);
            }
            final EditText etName = (EditText) convertView.findViewById(R.id.item_main_filter_name);
            final EditText etAddress = (EditText) convertView.findViewById(R.id.item_main_filter_address);
            etAddress.setText(filterAddress == null?"":filterAddress);
            etName.setText(filterName == null?"":filterName);

            etAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    tempAddress = s.toString();
                }
            });

            etName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    tempName = s.toString();
                }
            });

            convertView.findViewById(R.id.item_main_img_clear_address).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etAddress.setText("");
                    filterAddress = "";
                }
            });
            convertView.findViewById(R.id.item_main_img_clear_name).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etName.setText("");
                    filterName = "";
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}

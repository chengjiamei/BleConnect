package roc.cjm.bleconnect.activitys;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.cmds.Config;
import roc.cjm.bleconnect.activitys.normal.WriteActivity;
import roc.cjm.bleconnect.activitys.w311test.McWriteActivity;
import roc.cjm.bleconnect.services.BleService;
import roc.cjm.bleconnect.services.commands.BaseController;

/**
 * Created by Administrator on 2017/8/24.
 */

public class DetailActivity extends BaseActivity implements View.OnClickListener{

    public static String PROFILE_TYPE = "profile_type";

    private String TAG = "DetailActivity";

    private BluetoothDevice device;
    private int connectState;
    private int discoverState;
    private ExpandableListView listView;
    private List<BluetoothGattService> listServices;
    private MyAdapter myAdapter;
    private String[] serviceUUIDs;
    private String[] characterUUIDs;
    private String[] descriptorUUIDs;
    private String[] serviceType;
    private TextView tvConnectState;
    private Menu menu;

    private String CONFIG_PATH = "detail_path";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int type;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_detail);

        sharedPreferences = getSharedPreferences(CONFIG_PATH , MODE_PRIVATE);
        editor = sharedPreferences.edit();

        type = sharedPreferences.getInt(PROFILE_TYPE , Config.PROFILE_NORMAL);

        device = getIntent().getParcelableExtra("device");
        if (device == null) {
            finish();
            return;
        }
        setTitle(device.getName());
        serviceUUIDs = getResources().getStringArray(R.array.services_uuids);
        characterUUIDs = getResources().getStringArray(R.array.characteristic_uuids);
        descriptorUUIDs = getResources().getStringArray(R.array.descriptor_uuids);
        serviceType = getResources().getStringArray(R.array.service_type);

        tvConnectState = (TextView) findViewById(R.id.detail_connect_state);
        listView = (ExpandableListView) findViewById(R.id.detail_expand_listview);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setGroupIndicator(null);
        listView.setChildIndicator(null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseController.ACTION_SERVICE_DISCOVERED);
        filter.addAction(BaseController.ACTION_CONNECTION_STATE);
        filter.addAction(BaseController.ACTION_DISCOVER_STATE);
        filter.addAction(BleService.ACTION_DESCRIPTORWRITE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceive, filter);
        if(BleService.getInstance() == null) {
            finish();
            return;
        }
        listServices = BleService.getInstance().getServiceList();
        myAdapter.notifyDataSetChanged();
        connectState = BleService.getInstance().getConnectState();
        discoverState = BleService.getInstance().getDiscoverState();
        updateUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        device = intent.getParcelableExtra("device");
        if (device == null) {
            finish();
            return;
        }
        connectState = BleService.getInstance().getConnectState();
        discoverState = BleService.getInstance().getDiscoverState();
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        this.menu = menu;
        updateUI();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        switch (item.getItemId()) {
            case R.id.menu_detail_connect:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.no_access_to_location), Toast.LENGTH_LONG).show();
                    return true;
                }
                if(adapter == null || !adapter.isEnabled()){
                    Toast.makeText(this, getString(R.string.open_ble_first), Toast.LENGTH_LONG).show();
                    return true;
                }
                if(BleService.getInstance() != null && connectState != BaseController.STATE_CONNECTED) {
                    BleService.getInstance().connect(device);
                }else {
                    BleService.getInstance().disconnect();
                }
                break;
            case R.id.menu_detail_mcprofile:
                editor.putInt(PROFILE_TYPE, Config.PROFILE_MC).commit();
                type = Config.PROFILE_MC;
                break;
            case R.id.menu_detail_normal:
                editor.putInt(PROFILE_TYPE, Config.PROFILE_NORMAL).commit();
                type = Config.PROFILE_NORMAL;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceive);
    }

    private BroadcastReceiver mReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BaseController.ACTION_SERVICE_DISCOVERED)) {
                listServices = intent.getParcelableArrayListExtra(BaseController.EXTRA_SERVICE_LIST);
                myAdapter.notifyDataSetChanged();
                if(BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
            } else if (action.equals(BaseController.ACTION_CONNECTION_STATE)) {
                connectState = intent.getIntExtra(BaseController.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if(BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
                myAdapter.notifyDataSetChanged();
            } else if (action.equals(BaseController.ACTION_DISCOVER_STATE) ) {
                discoverState = intent.getIntExtra(BaseController.EXTRA_DISCOVER_STATE,BaseController.STATE_DISCOVERED);
            } else if (action.equals(BleService.ACTION_DESCRIPTORWRITE)) {
                myAdapter.notifyDataSetChanged();
            }
            updateUI();
        }
    };

    private String[] getStringByUUID(String uuid) {
        String[] uids = new String[2];
        if (uuid.startsWith("0000")) {
            String subString = uuid.substring(4, 8);
            int uid = Integer.valueOf(subString, 16);
            if (uid >= 0x1800 && uid <= 0x1828) {
                uids[0] = serviceUUIDs[uid - 0x1800];
                uids[1] = subString;
                return uids;
            }
        }
        uids[0] = getString(R.string.unknow_service);
        uids[1] = uuid;
        return uids;
    }

    private String[] getStringByCharacUUID(String uuid) {
        String[] uids = new String[2];
        if (uuid.startsWith("0000")) {
            String subString = uuid.substring(4, 8);
            int uid = Integer.valueOf(subString, 16);
            if (uid >= 0x2A00 && uid <= 0x2ADA) {
                uids[0] = characterUUIDs[uid - 0x2A00];
                uids[1] = subString;
                return uids;
            }
        }
        uids[0] = getString(R.string.unknow_characteristic);
        uids[1] = uuid;
        return uids;
    }

    private String[] getStringByDescriptorUUID(String uuid) {
        String[] uids = new String[2];
        if (uuid.startsWith("0000")) {
            String subString = uuid.substring(4, 8);
            int uid = Integer.valueOf(subString, 16);
            if (uid >= 0x2900 && uid <= 0x290E) {
                uids[0] = descriptorUUIDs[uid - 0x2900];
                uids[1] = subString;
                return uids;
            }
        }
        uids[0] = getString(R.string.unknow_descriptor);
        uids[1] = uuid;
        return uids;
    }

    private String getStringByType(int type) {
        return serviceType[type];
    }

    private String getPropertiesSting(int properties) {
        StringBuilder builder = new StringBuilder();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_BROADCAST) == BluetoothGattCharacteristic.PROPERTY_BROADCAST) {
            builder.append("BROADCAST");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) == BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) {
            builder.append(", EXTENDED PROPS");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE) {
            builder.append(", INDICATE");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
            builder.append(", NOTIFY");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == BluetoothGattCharacteristic.PROPERTY_READ) {
            builder.append(", READ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) == BluetoothGattCharacteristic.PROPERTY_WRITE) {
            builder.append(", WRITE");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) == BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) {
            builder.append(", SIGNED WRITE");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) {
            builder.append(", WRITE NO RESPONSE");
        }
        if (builder.toString().startsWith(",")) {
            return builder.toString().substring(1);
        }
        return builder.toString();
    }

    private long downTime = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            downTime = 0;
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (downTime == 0) {
                downTime = Calendar.getInstance().getTimeInMillis();
                handler.sendEmptyMessageDelayed(0x01, 500);
                Toast.makeText(this, "Tap second to exit!", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                if (Calendar.getInstance().getTimeInMillis() - downTime < 500) {
                    if (handler.hasMessages(0x01))
                        handler.removeMessages(0x01);
                    BleService.getInstance().disconnect();
                } else {
                    downTime = 0;
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void updateUI(){
        if(menu != null)
            menu.findItem(R.id.menu_detail_connect).setTitle(connectState == BaseController.STATE_CONNECTED?getString(R.string.disconnected):getString(R.string.connected));
        if(tvConnectState != null) {
            if (connectState == BaseController.STATE_CONNECTED && discoverState == BaseController.STATE_DISCOVERING) {
                tvConnectState.setText(getString(R.string.discovering));
            } else if (connectState == BaseController.STATE_CONNECTED) {
                tvConnectState.setText(getString(R.string.connected));
            } else if (connectState == BaseController.STATE_CONNECTING) {
                tvConnectState.setText(getString(R.string.connecting));
            } else if (connectState == BaseController.STATE_DISCONNECTED) {
                tvConnectState.setText(getString(R.string.disconnected));
            } else if (connectState == BaseController.STATE_DISCONNECTING) {
                tvConnectState.setText(getString(R.string.disconnecting));
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        switch (v.getId()) {


        }
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return listServices == null ? 0 : listServices.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            BluetoothGattService service = listServices.get(groupPosition);
            List<BluetoothGattCharacteristic> list = service.getCharacteristics();
            return list == null ? 0 : list.size();
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
            GroupHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.item_detail_group, null);
                holder = new GroupHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.item_group_sevicename);
                holder.tvType = (TextView) convertView.findViewById(R.id.item_group_service_type);
                holder.tvUUID = (TextView) convertView.findViewById(R.id.item_group_uuid);
                convertView.setTag(holder);
            }
            holder = (GroupHolder) convertView.getTag();
            BluetoothGattService service = listServices.get(groupPosition);
            String[] strings = getStringByUUID(service.getUuid().toString());
            holder.tvName.setText(strings[0]);
            holder.tvUUID.setText(": " + strings[1]);
            holder.tvType.setText(getStringByType(service.getType()));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.item_detail_child, null);
                holder = new ChildHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.item_child_name);
                holder.tvProperties = (TextView) convertView.findViewById(R.id.item_child_properties);
                holder.tvUUID = (TextView) convertView.findViewById(R.id.item_child_uuid);
                holder.tvDescriptName = (TextView) convertView.findViewById(R.id.item_child_descript_name);
                holder.tvValue = (TextView) convertView.findViewById(R.id.item_child_descript_value);
                holder.tvDesUUID = (TextView) convertView.findViewById(R.id.item_child_descript_uuid);
                holder.tvCharaValue = (TextView) convertView.findViewById(R.id.item_child_chara_value);

                holder.imgIndicate = (ImageView) convertView.findViewById(R.id.item_child_indicate);
                holder.imgNotification = (ImageView) convertView.findViewById(R.id.item_child_notification);
                holder.imgRead = (ImageView) convertView.findViewById(R.id.item_child_read);
                holder.imgWrite = (ImageView) convertView.findViewById(R.id.item_child_write);
                holder.imgDescripRead = (ImageView) convertView.findViewById(R.id.item_child_img_read);

                holder.viewDescript = convertView.findViewById(R.id.item_child_descript);
                holder.viewLinearDescript = convertView.findViewById(R.id.linear_proper_2);
                holder.viewLinearProper = convertView.findViewById(R.id.linear_proper_1);
                holder.viewCharacValue = convertView.findViewById(R.id.item_child_chara_linear);
                convertView.setTag(holder);
            }
            holder = (ChildHolder) convertView.getTag();
            BluetoothGattService service = listServices.get(groupPosition);
            final BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(childPosition);
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            final BluetoothGattDescriptor descriptor;



            String[] strings = getStringByCharacUUID(characteristic.getUuid().toString());
            holder.tvName.setText(strings[0]);
            holder.tvUUID.setText(strings[1]);
            int properties = characteristic.getProperties();

            holder.imgWrite.setVisibility(((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) == BluetoothGattCharacteristic.PROPERTY_WRITE ||
                    (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) ? View.VISIBLE : View.GONE);
            holder.imgRead.setVisibility((properties & BluetoothGattCharacteristic.PROPERTY_READ) != BluetoothGattCharacteristic.PROPERTY_READ ? View.GONE : View.VISIBLE);
            holder.imgIndicate.setVisibility((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != BluetoothGattCharacteristic.PROPERTY_INDICATE?View.GONE:View.VISIBLE);
            holder.imgNotification.setVisibility((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != BluetoothGattCharacteristic.PROPERTY_NOTIFY?View.GONE:View.VISIBLE);
            final String characValue = MainActivity.bytes2String(characteristic.getValue(),1);
            if(characValue == null || characValue.trim().equals("")){
                holder.tvCharaValue.setText("");
                holder.viewCharacValue.setVisibility(View.GONE);
            }else {
                holder.tvCharaValue.setText(characValue);
                holder.viewCharacValue.setVisibility(View.VISIBLE);
            }

            if(connectState == BaseController.STATE_CONNECTED) {
                holder.viewLinearProper.setVisibility(View.VISIBLE);
                holder.viewLinearDescript.setVisibility(View.VISIBLE);
            }else {
                holder.viewLinearProper.setVisibility(View.GONE);
                holder.viewLinearDescript.setVisibility(View.GONE);
            }

            if ((descriptors == null || descriptors.size() == 0)) {
                holder.viewDescript.setVisibility(View.GONE);
                descriptor = null;
            } else {
                descriptor = descriptors.get(0);
                holder.viewDescript.setVisibility(View.VISIBLE);
                String[] sts = getStringByDescriptorUUID(descriptor.getUuid().toString());
                holder.tvDescriptName.setText(sts[0]);
                holder.tvDesUUID.setText(sts[1]);
                byte[] values = BleService.getInstance().getBaseController().isEnableNotifyIndicate(characteristic);
                if(values != null && values.length == 2){
                    if((values[0]& 0xff) == 0x01 && (values[1] & 0xff) == 0) {
                        holder.imgNotification.setSelected(true);
                        holder.imgIndicate.setSelected(false);
                        holder.tvValue.setText(getString(R.string.enable_notification));
                    }else if((values[0]& 0xff) == 0x02 && (values[1] & 0xff) == 0) {
                        holder.tvValue.setText(getString(R.string.enable_indicate));
                        holder.imgIndicate.setSelected(true);
                        holder.imgNotification.setSelected(false);
                    }else {
                        holder.tvValue.setText(getString(R.string.indicate_notification));
                        holder.imgIndicate.setSelected(false);
                        holder.imgNotification.setSelected(false);
                    }
                }else {
                    holder.tvValue.setText(MainActivity.bytes2String(descriptor.getValue(), 1));
                }
            }

            holder.tvProperties.setText(getPropertiesSting(properties));
            holder.imgDescripRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BleService.getInstance().readDescriptor(descriptor);
                }
            });
            holder.imgNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BleService.getInstance().enableNotifyIndicate(characteristic.getService().getUuid(),characteristic.getUuid(),!isEnable(descriptor));
                }
            });
            holder.imgIndicate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BleService.getInstance().enableNotifyIndicate(characteristic.getService().getUuid(),characteristic.getUuid(),!isEnable(descriptor));
                }
            });
            holder.imgRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BleService.getInstance().read(characteristic.getService().getUuid(), characteristic.getUuid());
                }
            });
            holder.imgWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    switch (type) {
                        case Config.PROFILE_NORMAL:
                            intent = new Intent(DetailActivity.this, WriteActivity.class);
                            break;
                        case Config.PROFILE_MC:
                            intent = new Intent(DetailActivity.this, McWriteActivity.class);
                            intent.putExtra(PROFILE_TYPE, type);
                            break;
                    }

                    intent.putExtra("characteristicuuid", characteristic.getUuid().toString());
                    intent.putExtra("serviceuuid", characteristic.getService().getUuid().toString());
                    intent.putExtra("device" , device);
                    startActivity(intent);

                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    public boolean isEnable(BluetoothGattDescriptor descriptor) {
        byte[] values = BleService.getInstance().getBaseController().isEnableNotifyIndicate(descriptor.getCharacteristic());
        if(values != null && values.length == 2){
            if((values[0]& 0xff) == 0x01 && (values[1] & 0xff) == 0) {
                return true;
            }else if((values[0]& 0xff) == 0x02 && (values[1] & 0xff) == 0) {
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    class GroupHolder {
        TextView tvName;
        TextView tvUUID;
        TextView tvType;
    }

    class ChildHolder {
        TextView tvName;
        TextView tvUUID;
        TextView tvProperties;
        TextView tvCharaValue;

        TextView tvValue;
        TextView tvDescriptName;
        TextView tvDesUUID;

        View viewDescript;
        View viewLinearProper;
        View viewLinearDescript;
        View viewCharacValue;

        ImageView imgWrite;
        ImageView imgRead;
        ImageView imgIndicate;
        ImageView imgNotification;
        ImageView imgDescripRead;

    }
}

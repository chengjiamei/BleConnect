package roc.cjm.bleconnect.activitys;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.databases.DbCommand;
import roc.cjm.bleconnect.services.BleService;
import roc.cjm.bleconnect.services.commands.BaseController;
import roc.cjm.bleconnect.services.entry.Command;
import roc.cjm.bleconnect.services.entry.CommandList;

/**
 * Created by Administrator on 2017/8/25.
 */

public class WriteActivity extends BaseActivity implements BleService.OnBleService{

    public static int REQUES_CODE_ADD = 1;
    public static int REQUES_CODE_HIST = 2;

    private String serviceUUID;
    private String characUUID;
    private BluetoothDevice device;
    private TextView tvConnectState,tvLog;
    private ScrollView scrollView;
    private int connectState;
    private CommandList commandList;
    private List<Command> listCommand = new ArrayList<>();
    private MyAdapter myAdapter;
    private StringBuilder logBuilder = new StringBuilder();
    private boolean isWriteCmd = false;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_write);

        serviceUUID = getIntent().getStringExtra("serviceuuid");
        characUUID = getIntent().getStringExtra("characteristicuuid");
        device = getIntent().getParcelableExtra("device");

        ListView listView = (ListView) findViewById(R.id.write_list);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        setTitle(device.getName());
        tvConnectState = (TextView) findViewById(R.id.write_connect_state);
        tvLog = (TextView) findViewById(R.id.tv_log);
        scrollView = (ScrollView) findViewById(R.id.write_scrollview);
        connectState = BleService.getInstance().getConnectState();
        updateUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseController.ACTION_CONNECTION_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        BleService.getInstance().setOnBleService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUES_CODE_ADD && resultCode == RESULT_OK) {
            ArrayList<Command> list = data.getParcelableArrayListExtra("list");
            String mark = data.getStringExtra("mark");
            listCommand = list;
            ArrayList<Command> tpl = new ArrayList<>();
            tpl.addAll(list);
            commandList = new CommandList(java.util.Calendar.getInstance().getTimeInMillis(), mark, tpl);
            myAdapter.notifyDataSetChanged();
            saveCommandList(commandList);
        }else if(requestCode == REQUES_CODE_HIST && resultCode == RESULT_OK) {
            ArrayList<Command> list = data.getParcelableArrayListExtra("list");
            String mark = data.getStringExtra("mark");
            listCommand = list;
            ArrayList<Command> tpl = new ArrayList<>();
            tpl.addAll(list);
            commandList = new CommandList(java.util.Calendar.getInstance().getTimeInMillis(), mark, tpl);
            myAdapter.notifyDataSetChanged();
        }
    }

    public void saveCommandList(CommandList list) {
        DbCommand.getInstance().saveOrUpdate(list);
    }

    public void updateUI(){
        if(tvConnectState != null) {
            if (connectState == BaseController.STATE_CONNECTED) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(isWriteCmd) {
            Toast.makeText(this, "Is Writing command!", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_write_add:
                intent = new Intent(this, AddCmdActivity.class);
                intent.putExtra("characteristicuuid", characUUID);
                intent.putExtra("serviceuuid", serviceUUID);
                startActivityForResult(intent,REQUES_CODE_ADD);
                break;
            case R.id.menu_write_cleaer:
                logBuilder = new StringBuilder();
                setLogTV();
                break;
            case R.id.menu_write_hist:
                intent = new Intent(this, HistoryCmdActivity.class);
                startActivityForResult(intent, REQUES_CODE_HIST);
                break;
            case R.id.menu_write_write:

                if(listCommand == null || listCommand.size() == 0){
                    Toast.makeText(this, "Add command first!", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if(BleService.getInstance() != null && connectState != BaseController.STATE_CONNECTED) {
                    Toast.makeText(this, "Connect first!", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                isWriteCmd = true;
                logBuilder = new StringBuilder();
                boolean b = BleService.getInstance().write(UUID.fromString(serviceUUID), UUID.fromString(characUUID), listCommand.get(0).getCmd());
                if(!b) {
                    logBuilder.append(characUUID+" write failed\r\n").append(MainActivity.bytes2String(listCommand.get(0).getCmd(),1)+"\r\n");
                    listCommand.remove(0);
                    commandHandler.sendEmptyMessageDelayed(0x01, 150);
                }
                setLogTV();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLogTV(){
        commandHandler.sendEmptyMessage(0x02);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BaseController.ACTION_CONNECTION_STATE)) {
                connectState = intent.getIntExtra(BaseController.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if(connectState == BaseController.STATE_DISCONNECTED) {
                    Toast.makeText(context, "Please connect to device first!", Toast.LENGTH_SHORT).show();
                }
                updateUI();
            }
        }
    };

    private Handler commandHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                if (listCommand != null && listCommand.size() > 0) {
                    boolean b = BleService.getInstance().write(UUID.fromString(serviceUUID), UUID.fromString(characUUID), listCommand.get(0).getCmd());
                    if (!b) {
                        logBuilder.append(characUUID + " write failed\r\n").append(MainActivity.bytes2String(listCommand.get(0).getCmd(), 1) + "\r\n");
                        setLogTV();
                        listCommand.remove(0);
                        commandHandler.sendEmptyMessageDelayed(0x01, 150);
                    }
                } else {
                    if(commandList != null && commandList.getCommandList() != null) {
                        if(listCommand == null || listCommand.size() == 0) {
                            listCommand = new ArrayList<>();
                            listCommand.addAll(commandList.getCommandList());
                        }
                    }
                    isWriteCmd = false;
                    Toast.makeText(WriteActivity.this, "Write Finished!", Toast.LENGTH_SHORT).show();
                }
                break;
                case 0x02:
                    tvLog.setText(logBuilder.toString());
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    break;
            }
        }
    };

    //OnBleService Begin
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(status == BluetoothGatt.GATT_SUCCESS) {
            logBuilder.append(characteristic.getUuid().toString()+" write success\r\n").append(MainActivity.bytes2String(characteristic.getValue(),1)+"\r\n\r\n");
        }else {
            logBuilder.append(characteristic.getUuid().toString()+" write failur\r\n").append(MainActivity.bytes2String(characteristic.getValue(),1)+"\r\n\r\n");
        }
        setLogTV();
        if(commandList != null && commandList.getCommandList() != null && commandList.getCommandList().size() > 0) {
            if(listCommand != null && listCommand.size()>0) {
                listCommand.remove(0);
            }
        }else {
            //指令发送完成
        }
        commandHandler.sendEmptyMessageDelayed(0x01, 150);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        logBuilder.append(characteristic.getUuid().toString()+" received\r\n").append(MainActivity.bytes2String(characteristic.getValue(),1)+"\r\n\r\n");
        setLogTV();
    }
    //OnBleService End

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return commandList == null?0:(commandList.getCommandList() == null ?0:commandList.getCommandList().size());
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
            if(convertView == null) {
                convertView = LayoutInflater.from(WriteActivity.this).inflate(R.layout.item_command,null);
                holder = new Holder();
                holder.tvCommand = (TextView) convertView.findViewById(R.id.item_tv_command);
                holder.tvRemark = (TextView) convertView.findViewById(R.id.item_tv_remark);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            Command cmd = commandList.getCommandList().get(position);
            holder.tvCommand.setText(MainActivity.bytes2String(cmd.getCmd(), 1));
            if(cmd.getRemark() == null || cmd.getRemark().equals("")){
                holder.tvRemark.setVisibility(View.GONE);
            }else {
                holder.tvRemark.setVisibility(View.VISIBLE);
            }
            holder.tvRemark.setText(cmd.getRemark() == null?"":cmd.getRemark());
            return convertView;
        }
    }

    private class Holder {
        TextView tvCommand;
        TextView tvRemark;
    }
}

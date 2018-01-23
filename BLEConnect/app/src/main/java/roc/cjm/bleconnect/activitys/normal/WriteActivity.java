package roc.cjm.bleconnect.activitys.normal;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.BaseActivity;
import roc.cjm.bleconnect.activitys.MainActivity;
import roc.cjm.bleconnect.databases.DbCommand;
import roc.cjm.bleconnect.logs.Logger;
import roc.cjm.bleconnect.services.BleService;
import roc.cjm.bleconnect.services.commands.BaseController;
import roc.cjm.bleconnect.activitys.normal.entry.Command;
import roc.cjm.bleconnect.activitys.normal.entry.CommandList;
import roc.cjm.bleconnect.utils.DateUtil;

/**
 * Created by Administrator on 2017/8/25.
 */

public class WriteActivity extends BaseActivity implements BleService.OnBleService {

    private int MAX_LOG_SIZE = 1000;
    public static int REQUES_CODE_ADD = 1;
    public static int REQUES_CODE_HIST = 2;

    private String serviceUUID;
    private String characUUID;
    private BluetoothDevice device;
    private TextView tvConnectState;
    private int connectState;
    private int discoverState;
    private Menu menu;
    private CommandList commandList;
    private ArrayList<Command> listCommand = new ArrayList<>();
    private MyAdapter myAdapter;
    private boolean isWriteCmd = false;
    private ListView logListView;
    private LogAdapter logAdapter;
    private List<SpannableString> logList = Collections.synchronizedList(new ArrayList<SpannableString>());
    private AlertDialog alertDialog;
    private EditText etCmd;
    private EditText etMark;
    private int currentIndex = -1;

    @Override
    protected void  onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_write);

        serviceUUID = getIntent().getStringExtra("serviceuuid");
        characUUID = getIntent().getStringExtra("characteristicuuid");
        device = getIntent().getParcelableExtra("device");
        ListView listView = (ListView) findViewById(R.id.write_list);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currentIndex = position;
                alertDialog.setMessage("Modify");
                etCmd.setText(MainActivity.bytes2String(listCommand.get(position).getCmd()));
                etMark.setText(listCommand.get(position).getRemark());
                alertDialog.show();
                return false;
            }
        });
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        setTitle(device.getName());
        tvConnectState = (TextView) findViewById(R.id.write_connect_state);

        logListView = (ListView) findViewById(R.id.write_log_listview);
        logAdapter = new LogAdapter();
        logListView.setAdapter(logAdapter);

        connectState = BleService.getInstance().getConnectState();
        discoverState = BleService.getInstance().getDiscoverState();
        updateUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseController.ACTION_CONNECTION_STATE);
        filter.addAction(BaseController.ACTION_SERVICE_DISCOVERED);
        filter.addAction(BaseController.ACTION_DISCOVER_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        if(BleService.getInstance() != null ) {
            BleService.getInstance().setOnBleService(this);

        }
        createDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUES_CODE_ADD && resultCode == RESULT_OK) {
            ArrayList<Command> list = data.getParcelableArrayListExtra("list");
            String mark = data.getStringExtra("mark");
            listCommand = list;
            ArrayList<Command> tpl = new ArrayList<>();
            tpl.addAll(list);
            commandList = new CommandList(java.util.Calendar.getInstance().getTimeInMillis(), mark, tpl);
            myAdapter.notifyDataSetChanged();
            saveCommandList(commandList);
        } else if (requestCode == REQUES_CODE_HIST && resultCode == RESULT_OK) {
            ArrayList<Command> list = data.getParcelableArrayListExtra("list");
            String mark = data.getStringExtra("mark");
            long time = data.getLongExtra("time", java.util.Calendar.getInstance().getTimeInMillis());
            listCommand = list;
            ArrayList<Command> tpl = new ArrayList<>();
            tpl.addAll(list);
            commandList = new CommandList(time, mark, tpl);
            myAdapter.notifyDataSetChanged();
        }
    }

    public void saveCommandList(CommandList list) {
        DbCommand.getInstance().saveOrUpdate(list);
    }

    public void updateUI() {
        if(menu != null)
            menu.findItem(R.id.menu_write_connect).setTitle(connectState == BaseController.STATE_CONNECTED?getString(R.string.disconnected):getString(R.string.connected));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isWriteCmd) {
            Toast.makeText(this, "Is Writing command!", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
        Date date = new Date();
        Intent intent = null;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        switch (item.getItemId()) {
            case R.id.menu_write_add:
                intent = new Intent(this, AddCmdActivity.class);
                intent.putExtra("characteristicuuid", characUUID);
                intent.putExtra("serviceuuid", serviceUUID);
                startActivityForResult(intent, REQUES_CODE_ADD);
                break;
            case R.id.menu_write_cleaer:
                if (logList == null)
                    logList = Collections.synchronizedList(new ArrayList<SpannableString>());
                logList.clear();
                logAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_write_hist:
                intent = new Intent(this, HistoryCmdActivity.class);
                startActivityForResult(intent, REQUES_CODE_HIST);
                break;
            case R.id.menu_write_write:

                if (listCommand == null || listCommand.size() == 0) {
                    Toast.makeText(this, "Add command first!", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (BleService.getInstance() != null && connectState != BaseController.STATE_CONNECTED) {
                    Toast.makeText(this, "Connect first!", Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                isWriteCmd = true;
                StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date,"HH:mm:ss")).append(".").append(date.getTime()%1000).append(" ");
                boolean b = BleService.getInstance().write(UUID.fromString(serviceUUID), UUID.fromString(characUUID), listCommand.get(0).getCmd());
                if (!b) {
                    builder.append(characUUID.substring(4, 8)).append(" write failed ").append(MainActivity.bytes2String(listCommand.get(0).getCmd(), 1));
                    listCommand.remove(0);
                    commandHandler.sendEmptyMessageDelayed(0x01, 150);
                    setLogTV(builder, Logger.LOG_E);
                } else {
                    builder.append(characUUID.substring(4, 8)).append(" isWriting ").append(MainActivity.bytes2String(listCommand.get(0).getCmd(), 1));
                    setLogTV(builder, Logger.LOG_V);
                }
                break;
            case R.id.menu_write_connect:
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLogTV(StringBuilder builder, int level) {
        sendMessage(level, builder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Date date = new Date();
            StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date,"HH:mm:ss")).append(".").append(date.getTime()%1000).append(" ");
            if (action.equals(BaseController.ACTION_CONNECTION_STATE)) {
                connectState = intent.getIntExtra(BaseController.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (connectState == BaseController.STATE_DISCONNECTED) {
                    builder.append(" disconnected ");
                    Toast.makeText(context, "Please connect to device first!", Toast.LENGTH_SHORT).show();
                }else if(connectState == BaseController.STATE_CONNECTED) {
                    builder.append(" connected ");
                }else if(connectState == BaseController.STATE_DISCONNECTING) {
                    builder.append(" disconnectiong");
                }else if(connectState == BaseController.STATE_CONNECTING) {
                    builder.append(" connecting");
                }
                if(BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
                sendMessage(Logger.LOG_W, builder);

            }else if(action.equals(BaseController.ACTION_SERVICE_DISCOVERED)) {
                if(BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
            }else if(action.equals(BaseController.ACTION_DISCOVER_STATE)) {
                discoverState = intent.getIntExtra(BaseController.EXTRA_DISCOVER_STATE,BaseController.STATE_DISCOVERED);
            }
            updateUI();
        }
    };

    private void sendMessage(int arg1, Object object) {
        Message message = Message.obtain();
        message.arg1 = arg1;
        message.obj = object;
        message.what = 0x02;
        commandHandler.sendMessage(message);
    }

    private Handler commandHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    Date date = new Date();
                    if (listCommand != null && listCommand.size() > 0) {
                        StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date,"HH:mm:ss")).append(".").append(date.getTime()%1000).append(" ");
                        boolean b = BleService.getInstance().write(UUID.fromString(serviceUUID), UUID.fromString(characUUID), listCommand.get(0).getCmd());
                        if (!b) {
                            builder.append(characUUID.substring(4,8)).append(" write failed ").append(MainActivity.bytes2String(listCommand.get(0).getCmd(), 1));
                            setLogTV(builder, Logger.LOG_E);
                            listCommand.remove(0);
                            commandHandler.sendEmptyMessageDelayed(0x01, 150);
                        } else {
                            builder.append(characUUID.substring(4, 8)).append(" isWriting ").append(MainActivity.bytes2String(listCommand.get(0).getCmd(), 1));
                            setLogTV(builder, Logger.LOG_V);
                        }
                    } else {
                        if (commandList != null && commandList.getCommandList() != null) {
                            if (listCommand == null || listCommand.size() == 0) {
                                listCommand = new ArrayList<>();
                                listCommand.addAll(commandList.getCommandList());
                            }
                        }
                        isWriteCmd = false;
                        Toast.makeText(WriteActivity.this, "Write Finished!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0x02:
                    int level = msg.arg1;
                    StringBuilder builder = (StringBuilder) msg.obj;
                    if(logList == null)
                        logList = Collections.synchronizedList(new ArrayList<SpannableString>());
                    ///控制log输出量
                    if(logList.size()>=MAX_LOG_SIZE) {
                        logList.remove(0);
                    }
                    logList.add(Logger.log(builder, level));
                    logAdapter.notifyDataSetChanged();
                    logListView.setSelection(logListView.getBottom());
                    ///tvLog.setText(logBuilder.toString());
                    ///scrollView.fullScroll(View.FOCUS_DOWN);
                    break;
            }
        }
    };

    private byte[] string2byte(String cmd) {
        if(cmd == null || cmd.equals(""))
            return null;
        int i = 0;
        int len = cmd.length();
        byte[] bs = new byte[len/2];
        while (i<len/2) {
            bs[i] = (byte)(Integer.valueOf(cmd.substring(i*2,i*2+2),16).intValue());
            i++;
        }
        return bs;
    }

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_addcmd,null);
        etCmd = (EditText) view.findViewById(R.id.dialog_addcmd_command);
        etMark = (EditText) view.findViewById(R.id.dialog_addcmd_remark);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String cmd = etCmd.getText().toString();

                if(!cmd.equals("") && cmd.length()%2 == 0) {
                    Pattern pattern = Pattern.compile("[0-9A-Fa-f]{" + cmd.length() + "}");
                    Matcher matcher = pattern.matcher(cmd);
                    if (matcher.find()) {
                        Command command = new Command(characUUID, serviceUUID, string2byte(cmd),etMark.getText().toString(),false);
                        if(currentIndex == -1) {
                            listCommand.add(command);
                        }else {
                            Command command1 = listCommand.get(currentIndex);
                            listCommand.remove(currentIndex);
                            listCommand.add(currentIndex,command);
                        }
                        ArrayList<Command> tpl = new ArrayList<Command>();
                        tpl.addAll(listCommand);
                        commandList = new CommandList(commandList.getCreateTime(), commandList.getRemark(), tpl);
                        DbCommand.getInstance().update(commandList);
                        myAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(WriteActivity.this, "Please input Hex number",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(WriteActivity.this, "Please input Hex number",Toast.LENGTH_LONG).show();
                }
                etMark.setText("");
                etCmd.setText("");
                currentIndex = -1;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(view);
        alertDialog =builder.create();
    }

    //OnBleService Begin
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Date date = new Date();
        StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date,"HH:mm:ss")).append(".").append(date.getTime()%1000).append(" ").append(characteristic.getUuid().toString().substring(4, 8));
        if (status == BluetoothGatt.GATT_SUCCESS) {
            builder.append(" write success ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
            setLogTV(builder, Logger.LOG_V);
        } else {
            builder.append(" write failure ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
            setLogTV(builder, Logger.LOG_E);
        }

        if (commandList != null && commandList.getCommandList() != null && commandList.getCommandList().size() > 0) {
            if (listCommand != null && listCommand.size() > 0) {
                listCommand.remove(0);
            }
        } else {
            //指令发送完成
        }
        commandHandler.sendEmptyMessageDelayed(0x01, 150);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Date date = new Date();
        StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date,"HH:mm:ss")).append(".").append(date.getTime()%1000).append(" ")
                .append(characteristic.getUuid().toString().substring(4,8));
        builder.append(" received ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
        setLogTV(builder,Logger.LOG_W);
    }
    //OnBleService End

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return commandList == null ? 0 : (commandList.getCommandList() == null ? 0 : commandList.getCommandList().size());
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
            if (convertView == null) {
                convertView = LayoutInflater.from(WriteActivity.this).inflate(R.layout.item_command, null);
                holder = new Holder();
                holder.tvCommand = (TextView) convertView.findViewById(R.id.item_tv_command);
                holder.tvRemark = (TextView) convertView.findViewById(R.id.item_tv_remark);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            Command cmd = commandList.getCommandList().get(position);
            holder.tvCommand.setText(MainActivity.bytes2String(cmd.getCmd(), 1));
            if (cmd.getRemark() == null || cmd.getRemark().equals("")) {
                holder.tvRemark.setVisibility(View.GONE);
            } else {
                holder.tvRemark.setVisibility(View.VISIBLE);
            }
            holder.tvRemark.setText(cmd.getRemark() == null ? "" : cmd.getRemark());
            return convertView;
        }
    }

    private class Holder {
        TextView tvCommand;
        TextView tvRemark;
    }

    private class LogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return logList.size();
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
            if (convertView == null) {
                TextView textView = new TextView(WriteActivity.this);
                AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setLayoutParams(layoutParams);
                convertView = textView;
            }
            TextView tv = (TextView) convertView;
            tv.setText("");
            tv.append(logList.get(position));
            return convertView;
        }
    }
}

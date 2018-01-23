package roc.cjm.bleconnect.activitys.w311test;

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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.DetailActivity;
import roc.cjm.bleconnect.activitys.LoadHistTableActivity;
import roc.cjm.bleconnect.activitys.MainActivity;
import roc.cjm.bleconnect.activitys.cmds.databases.DbTable;
import roc.cjm.bleconnect.activitys.cmds.entry.Command;
import roc.cjm.bleconnect.activitys.cmds.Config;
import roc.cjm.bleconnect.activitys.cmds.McConfig;
import roc.cjm.bleconnect.activitys.cmds.databases.DbMcCommand;
import roc.cjm.bleconnect.activitys.cmds.entry.Table;
import roc.cjm.bleconnect.logs.Logger;
import roc.cjm.bleconnect.services.BleService;
import roc.cjm.bleconnect.services.commands.BaseController;
import roc.cjm.bleconnect.utils.DateUtil;
import roc.cjm.bleconnect.utils.DeviceConfiger;
import roc.cjm.bleconnect.utils.Util;
import roc.cjm.bleconnect.views.SlideListView2;
import roc.cjm.bleconnect.views.SlideView2;

/**
 * Created by Marcos on 2017/10/23.
 */

public class McWriteActivity extends AppCompatActivity implements BleService.OnBleService , View.OnClickListener{
    private String TAG = "McWriteActivity";

    public static final int OPERATE_MODIFY = 0x01;
    public static final int OPERATE_SAVEAS = 0x02;

    private int MAX_LOG_SIZE = 1000;
    private List<SpannableString> logList = Collections.synchronizedList(new ArrayList<SpannableString>());
    private boolean isWriteCmd = false;
    private ListView logListView;
    private LogAdapter logAdapter;

    private Menu menu;
    private int type;
    private UUID serviceUUID;
    private UUID characUUID;
    private BluetoothDevice device;
    private int connectState, discoverState;
    private TextView tvConnectState;
    List<List<Command>> cmdList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecycleAdapter recycleAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SlideListView2 slideListView2;
    private SlideAdapter slideAdapter;

    private EditText etCmd;
    private AlertDialog alertDialog;
    private int gId = 0, cId = 0;
    private List<Table> tableList = new ArrayList<>();
    private int operateType = 0;
    private String[] titles = new String[0];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra(DetailActivity.PROFILE_TYPE, Config.PROFILE_MC);
        serviceUUID = UUID.fromString(getIntent().getStringExtra("serviceuuid"));
        characUUID = UUID.fromString(getIntent().getStringExtra("characteristicuuid"));
        device = getIntent().getParcelableExtra("device");
        if(device.getName() != null) {
            setTitle(device.getName());
        }
        setContentView(R.layout.activity_mc_write);
        tvConnectState = (TextView) findViewById(R.id.tv_state);
        logListView = (ListView) findViewById(R.id.write_log_listview);
        logAdapter = new LogAdapter();
        logListView.setAdapter(logAdapter);

        int wid = getResources().getDisplayMetrics().widthPixels;
        slideListView2 = (SlideListView2) findViewById(R.id.write_list_2);
        slideAdapter = new SlideAdapter();
        slideListView2.setAdapter(slideAdapter);
        slideListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(BleService.getInstance() != null && BleService.getInstance().getConnectState() == BaseController.STATE_CONNECTED && isWriteCmd == false) {
                    BleService.getInstance().write(serviceUUID , characUUID, cmdList.get(gId).get(position).getCommand());
                    isWriteCmd = true;
                }else if(isWriteCmd){
                    Toast.makeText(McWriteActivity.this, "Is Writing now!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(McWriteActivity.this, "Connect First!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.mc_horizon_lisview);
        //设置布局管理器
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        titles = new String[0];
        recycleAdapter = new RecycleAdapter();
        recyclerView.setAdapter(recycleAdapter);


        connectState = BleService.getInstance().getConnectState();
        discoverState = BleService.getInstance().getDiscoverState();
        updateUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseController.ACTION_CONNECTION_STATE);
        filter.addAction(BaseController.ACTION_SERVICE_DISCOVERED);
        filter.addAction(BaseController.ACTION_DISCOVER_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        if (BleService.getInstance() != null) {
            BleService.getInstance().setOnBleService(this);

        }
        createDialog();
        initDb();

    }

    public void initDb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                tableList = DbTable.getInstance().findAll(DbTable.COLUMN_PROFILE + "=?", new String[]{type + ""});
                if (tableList == null || tableList.size() == 0) {
                    Table table = new Table(type, DbMcCommand.DEFAULT_TABLE, "默认", Calendar.getInstance().getTimeInMillis());
                    DbMcCommand.getInstance().createTable(table);
                    McConfig.getInstance().reset();
                } else {
                    int profile = Config.getInt(McWriteActivity.this.getApplicationContext(), Config.KEY_PROFILE, Config.PROFILE_MC);
                    String tableName = Config.getString(McWriteActivity.this.getApplicationContext(), Config.KEY_TABLENAME, DbMcCommand.DEFAULT_TABLE);
                    if(profile != type) {
                        profile = type;
                    }
                    List<Table> tpL = DbTable.getInstance().findAll(DbTable.COLUMN_PROFILE + "=? and " + DbTable.COLUMN_NAME + "=?", new String[]{type + "", tableName});
                    if (tpL == null || tpL.size() == 0) {
                        if (!tableName.equals(DbMcCommand.DEFAULT_TABLE)) {
                            tableName = DbMcCommand.DEFAULT_TABLE;
                            DbMcCommand.getInstance().createTable(new Table(type, DbMcCommand.DEFAULT_TABLE, "默认", Calendar.getInstance().getTimeInMillis()));
                            McConfig.getInstance().reset();
                        }
                    }else {
                        DbMcCommand.getInstance().setTable(tpL.get(0));
                    }


                }
                load();
            }
        }).start();
    }

    public void load() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Command> list = DbMcCommand.getInstance().query(new String[]{DbMcCommand.COLUMN_CMD, DbMcCommand.COLUMN_DEFAULT, DbMcCommand.COLUMN_INDEX, DbMcCommand.COLUMN_PROFILE,
                        DbMcCommand.COLUMN_REMARK, DbMcCommand.COLUMN_TYPE}, DbMcCommand.COLUMN_PROFILE + "= ?", new String[]{type + ""}, DbMcCommand.COLUMN_TYPE, DbMcCommand.COLUMN_PROFILE + " , " + DbMcCommand.COLUMN_TYPE + " , " +
                        DbMcCommand.COLUMN_INDEX + " ASC");
                List<Command> commandList = DbMcCommand.getInstance().query(new String[]{DbMcCommand.COLUMN_CMD, DbMcCommand.COLUMN_DEFAULT, DbMcCommand.COLUMN_INDEX, DbMcCommand.COLUMN_PROFILE,
                                DbMcCommand.COLUMN_REMARK, DbMcCommand.COLUMN_TYPE}, DbMcCommand.COLUMN_PROFILE + "= ?", new String[]{type + ""},
                        null, DbMcCommand.COLUMN_PROFILE + " , " + DbMcCommand.COLUMN_TYPE + " , " +
                                DbMcCommand.COLUMN_INDEX + " ASC");
                if (list != null && list.size() > 0) {
                    if (cmdList != null) {
                        cmdList.clear();
                    } else {
                        cmdList = new ArrayList<List<Command>>();
                    }

                    for (int i = 0; i < list.size(); i++) {
                        cmdList.add(new ArrayList<Command>());
                    }
                    titles = new String[cmdList.size()*2];
                    for (int i = 0; i < commandList.size(); i++) {
                        Command cmd = commandList.get(i);
                        cmdList.get(cmd.getType() - 1).add(cmd);
                    }

                    if(type == Config.PROFILE_MC) {
                        for (int i=0;i<cmdList.size();i++){
                            List<Command> tpp = cmdList.get(i);
                            Command ccc = tpp.get(0);
                            switch (ccc.getType()) {
                                case McConfig.TYPE_CONTROL:
                                    titles[i] = "控制指令";
                                    break;
                                case McConfig.TYPE_DATA:
                                    titles[i] = "数据指令";
                                    break;
                                case McConfig.TYPE_SETTING:
                                    titles[i] = "设置指令";
                                    break;
                            }
                        }
                    }


                } else {
                    cmdList.clear();
                    titles = new String[0];
                }
                commandHandler.sendEmptyMessage(0x03);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mc, menu);
        this.menu = menu;
        updateUI();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_write_connect:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.no_access_to_location), Toast.LENGTH_LONG).show();
                    return true;
                }
                if (adapter == null || !adapter.isEnabled()) {
                    Toast.makeText(this, getString(R.string.open_ble_first), Toast.LENGTH_LONG).show();
                    return true;
                }
                if (BleService.getInstance() != null && connectState != BaseController.STATE_CONNECTED) {
                    BleService.getInstance().connect(device);
                } else {
                    BleService.getInstance().disconnect();
                }
                break;
            case R.id.menu_write_cleaer:
                if (logList == null)
                    logList = Collections.synchronizedList(new ArrayList<SpannableString>());
                logList.clear();
                logAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_write_reset:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        McConfig.getInstance().reset();
                        load();
                    }
                }).start();
                break;
            case R.id.menu_write_load:
                intent = new Intent(this , LoadHistTableActivity.class);
                intent.putExtra(DetailActivity.PROFILE_TYPE, type);
                startActivityForResult(intent, 1);
                break;
            case R.id.menu_write_saveas:
                operateType = OPERATE_SAVEAS;
                alertDialog.setTitle("Save Modify");
                etCmd.setHint("输入备注（不能重复）");
                alertDialog.show();
                break;
            case R.id.menu_write_reload:
                load();
                break;
            case R.id.menu_write_save:
                if(cmdList != null && cmdList.size()>0) {
                    ArrayList<Command> aryL = new ArrayList<>();
                    for (int i=0;i<cmdList.size();i++) {
                        aryL.addAll(cmdList.get(i));
                    }
                    DbMcCommand.getInstance().replace(aryL);
                    //load();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        if (menu != null)
            menu.findItem(R.id.menu_write_connect).setTitle(connectState == BaseController.STATE_CONNECTED ? getString(R.string.disconnected) : getString(R.string.connected));
        if (tvConnectState != null) {
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
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Date date = new Date();
        StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date, "HH:mm:ss")).append(".").append(date.getTime() % 1000).append(" ").append(characteristic.getUuid().toString().substring(4, 8));
        if (status == BluetoothGatt.GATT_SUCCESS) {
            builder.append(" W S ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
            setLogTV(builder, Logger.LOG_V);
        } else {
            builder.append(" W F ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
            setLogTV(builder, Logger.LOG_E);
        }
        isWriteCmd = false;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Date date = new Date();
        StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date, "HH:mm:ss")).append(".").append(date.getTime() % 1000).append(" ")
                .append(characteristic.getUuid().toString().substring(4, 8));
        builder.append(" R ").append(MainActivity.bytes2String(characteristic.getValue(), 1));
        setLogTV(builder, Logger.LOG_W);
    }

    public void setLogTV(StringBuilder builder, int level) {
        sendMessage(level, builder);
    }

    private Handler commandHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    Date date = new Date();

                    break;
                case 0x02:
                    int level = msg.arg1;
                    StringBuilder builder = (StringBuilder) msg.obj;
                    if (logList == null)
                        logList = Collections.synchronizedList(new ArrayList<SpannableString>());
                    ///控制log输出量
                    if (logList.size() >= MAX_LOG_SIZE) {
                        logList.remove(0);
                    }
                    logList.add(Logger.log(builder, level));
                    logAdapter.notifyDataSetChanged();
                    logListView.setSelection(logListView.getBottom());
                    ///tvLog.setText(logBuilder.toString());
                    ///scrollView.fullScroll(View.FOCUS_DOWN);
                    break;
                case 0x03:
                    slideAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void sendMessage(int arg1, Object object) {
        Message message = Message.obtain();
        message.arg1 = arg1;
        message.obj = object;
        message.what = 0x02;
        commandHandler.sendMessage(message);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Date date = new Date();
            StringBuilder builder = new StringBuilder().append(DateUtil.dataToString(date, "HH:mm:ss")).append(".").append(date.getTime() % 1000).append(" ");
            if (action.equals(BaseController.ACTION_CONNECTION_STATE)) {
                connectState = intent.getIntExtra(BaseController.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (connectState == BaseController.STATE_DISCONNECTED) {
                    builder.append(" disconnected ");
                    Toast.makeText(context, "Please connect to device first!", Toast.LENGTH_SHORT).show();
                } else if (connectState == BaseController.STATE_CONNECTED) {
                    builder.append(" connected ");
                } else if (connectState == BaseController.STATE_DISCONNECTING) {
                    builder.append(" disconnectiong");
                } else if (connectState == BaseController.STATE_CONNECTING) {
                    builder.append(" connecting");
                }
                if (BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
                sendMessage(Logger.LOG_W, builder);

            } else if (action.equals(BaseController.ACTION_SERVICE_DISCOVERED)) {
                if (BleService.getInstance() != null) {
                    discoverState = BleService.getInstance().getDiscoverState();
                }
            } else if (action.equals(BaseController.ACTION_DISCOVER_STATE)) {
                discoverState = intent.getIntExtra(BaseController.EXTRA_DISCOVER_STATE, BaseController.STATE_DISCOVERED);
            }
            updateUI();
        }
    };


    public void onClick(View v) {
        gId = recyclerView.getChildAdapterPosition(v);
        recyclerView.getAdapter().notifyDataSetChanged();
        linearLayoutManager.scrollToPositionWithOffset(gId, 0);
        linearLayoutManager.setStackFromEnd(true);
        slideAdapter.notifyDataSetChanged();

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
                TextView textView = new TextView(McWriteActivity.this);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
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

    private class SlideAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return (cmdList == null || cmdList.size() == 0)?0:(cmdList.get(gId).size());
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {

                SlideView2 slideView = (SlideView2) LayoutInflater.from(McWriteActivity.this).inflate(R.layout.item_slideview, null);
                View view = LayoutInflater.from(McWriteActivity.this).inflate(R.layout.item_command, null);
                view.setPadding(DeviceConfiger.dp2px(20),0,0,0);
                slideView.setContentView(view);

                View mergeView = LayoutInflater.from(McWriteActivity.this).inflate(R.layout.slide_view_merge, null);
                mergeView.findViewById(R.id.delete).setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Util.dp2px(McWriteActivity.this, 50), ViewGroup.LayoutParams.MATCH_PARENT);
                mergeView.setLayoutParams(params);
                slideView.setMergeView(mergeView);

                convertView = slideView;

                holder = new Holder();
                holder.tvCommand = (TextView) convertView.findViewById(R.id.item_tv_command);
                holder.tvRemark = (TextView) convertView.findViewById(R.id.item_tv_remark);
                holder.tvModify = (TextView) convertView.findViewById(R.id.modify);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            Command cmd = cmdList.get(gId).get(position);
            holder.tvCommand.setText(MainActivity.bytes2String(cmd.getCommand(), 1));
            if (cmd.getRemark() == null || cmd.getRemark().equals("")) {
                holder.tvRemark.setVisibility(View.GONE);
            } else {
                holder.tvRemark.setVisibility(View.VISIBLE);
            }
            holder.tvRemark.setText(cmd.getRemark() == null ? "" : cmd.getRemark());
            holder.tvModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cId = position;
                    Command command = cmdList.get(gId).get(cId);
                    etCmd.setText(MainActivity.bytes2String(command.getCommand()));
                    operateType = OPERATE_MODIFY;
                    alertDialog.setTitle("Modify");
                    etCmd.setHint("input command");
                    alertDialog.show();
                }
            });
            return convertView;
        }
    }

    private class Holder {
        TextView tvCommand;
        TextView tvRemark;
        TextView tvModify;
    }

    private byte[] string2byte(String cmd) {
        if (cmd == null || cmd.equals(""))
            return null;
        int i = 0;
        int len = cmd.length();
        byte[] bs = new byte[len / 2];
        while (i < len / 2) {
            bs[i] = (byte) (Integer.valueOf(cmd.substring(i * 2, i * 2 + 2), 16).intValue());
            i++;
        }
        return bs;
    }

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_addcmd, null);
        etCmd = (EditText) view.findViewById(R.id.dialog_addcmd_command);
        view.findViewById(R.id.dialog_addcmd_remark).setVisibility(View.GONE);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (operateType == OPERATE_MODIFY) {
                    String cmd = etCmd.getText().toString();

                    if (!cmd.equals("") && cmd.length() % 2 == 0) {
                        Pattern pattern = Pattern.compile("[0-9A-Fa-f]{" + cmd.length() + "}");
                        Matcher matcher = pattern.matcher(cmd);
                        if (matcher.find()) {
                            cmdList.get(gId).get(cId).setCommand(string2byte(cmd));
                            //expandableAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(McWriteActivity.this, "Please input Hex number", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(McWriteActivity.this, "Please input Hex number", Toast.LENGTH_LONG).show();
                    }
                }else if(operateType == OPERATE_SAVEAS) {
                    final String remark = etCmd.getText().toString();
                    List<Table> tpllll = DbTable.getInstance().findAll(DbTable.COLUMN_REMARK+"=? and " +DbTable.COLUMN_PROFILE + "=?" , new String[]{remark, type+""});
                    if(tpllll != null && tpllll.size()>0) {
                        Toast.makeText(McWriteActivity.this, "备注重复，请重新保存", Toast.LENGTH_LONG).show();
                    }else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String tableName = "table_" + type + "_" + Calendar.getInstance().getTimeInMillis();
                                Table table = new Table(type, tableName, remark, Calendar.getInstance().getTimeInMillis());
                                Config.putInt(McWriteActivity.this, Config.KEY_PROFILE, type);
                                Config.putString(McWriteActivity.this, Config.KEY_TABLENAME, tableName);
                                DbMcCommand.getInstance().createTable(table);
                                if(cmdList != null && cmdList.size()>0) {
                                    for (int i=0;i<cmdList.size();i++) {
                                        DbMcCommand.getInstance().replace(cmdList.get(i));
                                    }

                                }
                                load();
                            }
                        }).start();

                    }

                }
                etCmd.setText("");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(view);
        alertDialog = builder.create();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            Table tb = data.getParcelableExtra("table");
            Config.putInt(McWriteActivity.this, Config.KEY_PROFILE, type);
            Config.putString(McWriteActivity.this, Config.KEY_TABLENAME, tb.getTableName());
            DbMcCommand.getInstance().setTable(tb);
            load();
        }else if(requestCode == 1 && resultCode == RESULT_CANCELED) {
            int profilt = Config.getInt(this, Config.KEY_PROFILE, type);
            String tableName = Config.getString(this, Config.KEY_TABLENAME, DbMcCommand.DEFAULT_TABLE);
            List<Table> ttt = DbTable.getInstance().findAll(DbTable.COLUMN_PROFILE +"=? and "+DbTable.COLUMN_NAME+"=?", new String[]{profilt +"" , tableName});
            if(ttt == null || ttt.size() == 0) {
                Config.putString(this, Config.KEY_TABLENAME, DbMcCommand.DEFAULT_TABLE);
                Config.putInt(this, Config.KEY_PROFILE, type);
                initDb();
            }
        }
    }

    class RecycleAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(McWriteActivity.this);
            textView.setBackgroundResource(R.drawable.selector_mc_item);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(DeviceConfiger.dp2px(ViewGroup.LayoutParams.WRAP_CONTENT), ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setLayoutParams(layoutParams);
            textView.setPadding(DeviceConfiger.dp2px(20), DeviceConfiger.dp2px(10), DeviceConfiger.dp2px(20), DeviceConfiger.dp2px(10));
            textView.setClickable(true);
            textView.setOnClickListener(McWriteActivity.this);
            RecycleHolder holder = new RecycleHolder(textView);
            holder.tvTitle = textView;
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            holder.itemView.setSelected(gId == position);
            holder.itemView.setBackgroundResource(R.drawable.selector_mc_item);
            ((RecycleHolder)holder).tvTitle.setText(titles[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return titles == null?0:titles.length;
        }

    }
    class RecycleHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;
        public RecycleHolder(View itemView) {
            super(itemView);
        }
    }
}

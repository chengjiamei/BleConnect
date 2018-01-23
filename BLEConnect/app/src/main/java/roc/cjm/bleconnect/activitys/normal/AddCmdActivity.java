package roc.cjm.bleconnect.activitys.normal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.BaseActivity;
import roc.cjm.bleconnect.activitys.MainActivity;
import roc.cjm.bleconnect.activitys.normal.entry.Command;

/**
 * Created by Administrator on 2017/8/25.
 */

public class AddCmdActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener{

    public static int TYPE_ADD = 0x01;
    public static int TYPE_MODIFY = 0x02;

    private AlertDialog alertDialog;
    private EditText etCmd;
    private EditText etMark;
    private EditText etCommandMark;
    private ArrayList<Command> listCommand;
    private MyAdapter myAdapter;
    private String serviceUUID;
    private String characUUID;
    private int currentIndex = -1;
    private int type;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_addcmd);
        createDialog();
        int type = getIntent().getIntExtra("type", TYPE_ADD);
        if(type == TYPE_ADD) {
            setTitle("Add Command");
            serviceUUID = getIntent().getStringExtra("serviceuuid");
            characUUID = getIntent().getStringExtra("characteristicuuid");
            listCommand = new ArrayList<>();
        }else {
            setTitle("Modify Command");
            listCommand = getIntent().getParcelableArrayListExtra("list");
            serviceUUID = getIntent().getStringExtra(listCommand.get(0).getServiceUUID());
            characUUID = getIntent().getStringExtra(listCommand.get(0).getCharacterUUID());
        }

        etCommandMark = (EditText) findViewById(R.id.command_remark);
        ListView listView = (ListView) findViewById(R.id.addcmd_listview);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);


    }

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
                            listCommand.remove(currentIndex);
                            listCommand.add(currentIndex,command);
                        }
                        myAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(AddCmdActivity.this, "Please input Hex number",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(AddCmdActivity.this, "Please input Hex number",Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addcmd, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addcmd_add:
                alertDialog.setMessage("Add command");
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentIndex = position;
        alertDialog.setMessage("Modify");
        etCmd.setText(MainActivity.bytes2String(listCommand.get(position).getCmd()));
        alertDialog.show();

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if(listCommand != null && listCommand.size()>0) {
            intent.putExtra("type", type);
            intent.putExtra("list", listCommand);
            intent.putExtra("mark", etCommandMark.getText().toString());
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listCommand.size();
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
                convertView = LayoutInflater.from(AddCmdActivity.this).inflate(R.layout.item_command,null);
                holder = new Holder();
                holder.tvCommand = (TextView) convertView.findViewById(R.id.item_tv_command);
                holder.tvRemark = (TextView) convertView.findViewById(R.id.item_tv_remark);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            Command cmd = listCommand.get(position);
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

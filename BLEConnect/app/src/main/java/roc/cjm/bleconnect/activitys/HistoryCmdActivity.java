package roc.cjm.bleconnect.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.databases.DbCommand;
import roc.cjm.bleconnect.services.entry.Command;
import roc.cjm.bleconnect.services.entry.CommandList;
import roc.cjm.bleconnect.utils.DateUtil;

/**
 * Created by Administrator on 2017/8/26.
 */

public class HistoryCmdActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private List<CommandList> commandLists;
    private ListView listView;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_histcmd);

        commandLists = new ArrayList<>();
        listView = (ListView) findViewById(R.id.hist_listview);
        listView.setOnItemClickListener(this);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                commandLists = DbCommand.getInstance().findAll(null, null,DbCommand.COLUMN_TIME + " DESC ");
                if(commandLists == null) {
                    commandLists = new ArrayList<CommandList>();
                }
                handler.sendEmptyMessage(0x01);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_histcmd, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_hist_delete:
                DbCommand.getInstance().delete(null, null);
                handler.sendEmptyMessageDelayed(0x02,300);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("list", commandLists.get(position).getCommandList());
        intent.putExtra("mark", commandLists.get(position).getRemark());
        setResult(RESULT_OK, intent);
        finish();
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return commandLists == null?0:commandLists.size();
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
                convertView = LayoutInflater.from(HistoryCmdActivity.this).inflate(R.layout.item_hist,null);
                holder = new Holder();
                holder.tvCommand = (TextView) convertView.findViewById(R.id.item_hist_cmd);
                holder.tvRemark = (TextView) convertView.findViewById(R.id.item_hist_remark);
                holder.tvTime = (TextView) convertView.findViewById(R.id.item_hist_time);
                holder.btnModify = (Button) convertView.findViewById(R.id.item_hist_modify);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            CommandList cmd = commandLists.get(position);
            List<Command> commands = cmd.getCommandList();
            holder.tvCommand.setText(MainActivity.bytes2String(commands.get(0).getCmd(), 1));
            if(cmd.getRemark() == null || cmd.getRemark().equals("")){
                holder.tvRemark.setVisibility(View.GONE);
            }else {
                holder.tvRemark.setVisibility(View.VISIBLE);
            }
            holder.tvTime.setText(DateUtil.dataToString(DateUtil.longToDate(cmd.getCreateTime()),"yyyy-MM-dd HH:mm:ss"));
            holder.tvRemark.setText(cmd.getRemark() == null?"":cmd.getRemark());
            holder.btnModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class Holder {
        TextView tvCommand;
        TextView tvRemark;
        TextView tvTime;
        Button btnModify;

    }
}

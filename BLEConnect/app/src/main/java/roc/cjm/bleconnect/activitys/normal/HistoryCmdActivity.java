package roc.cjm.bleconnect.activitys.normal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.BaseActivity;
import roc.cjm.bleconnect.activitys.MainActivity;
import roc.cjm.bleconnect.databases.DbCommand;
import roc.cjm.bleconnect.activitys.normal.entry.Command;
import roc.cjm.bleconnect.activitys.normal.entry.CommandList;
import roc.cjm.bleconnect.utils.DateUtil;
import roc.cjm.bleconnect.utils.Util;
import roc.cjm.bleconnect.views.SlideListView2;
import roc.cjm.bleconnect.views.SlideView2;

/**
 * Created by Administrator on 2017/8/26.
 */

public class HistoryCmdActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private String TAG = "HistoryCmdActivity";

    private int REQUEST_CODE = 0x01;
    private List<String> listContent;
    private SlideListView2 listView;
    private MyAdapter myAdapter;
    private List<CommandList> listCommand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        listContent = new ArrayList<>();
        Random random = new Random();
        for (int i=0;i<100;i++) {
            listContent.add((random.nextInt()*100)+"");
        }

        listCommand = new ArrayList<>();

        listView = (SlideListView2) findViewById(R.id.listview);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);

        updateData();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myAdapter.notifyDataSetChanged();
        }
    };

    public void updateData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                listCommand = DbCommand.getInstance().findAll(null, null , DbCommand.COLUMN_TIME+" DESC");
                if(listCommand == null) {
                    listCommand = new ArrayList<CommandList>();
                }
                handler.sendEmptyMessage(0x01);
            }
        }).start();
    }

    private int curremtIndex;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e(TAG , "onItemClick");
        CommandList list = listCommand.get(position);
        Intent intent = new Intent();
        intent.putExtra("list" , list.getCommandList());
        intent.putExtra("mark", list.getRemark());
        intent.putExtra("time", list.getCreateTime());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            /*intent.putExtra("type", type);
            intent.putExtra("list", listCommand);
            intent.putExtra("mark", etCommandMark.getText().toString());*/
            ArrayList<Command> list = data.getParcelableArrayListExtra("list");
            String mark = data.getStringExtra("mark");
            CommandList commandList = listCommand.get(curremtIndex);
            CommandList commandList1 = new CommandList(commandList.getCreateTime(), mark, list);
            DbCommand.getInstance().update(commandList1);
            listCommand.remove(curremtIndex);
            listCommand.add(curremtIndex, commandList1);
            myAdapter.notifyDataSetChanged();
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listCommand == null?0:listCommand.size();
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
            if(convertView == null) {
                SlideView2 slideView = (SlideView2) LayoutInflater.from(HistoryCmdActivity.this).inflate(R.layout.item_slideview, null);
                View cView =  LayoutInflater.from(HistoryCmdActivity.this).inflate(R.layout.item_hist, null);
                slideView.setContentView(cView);
                View mergeView = LayoutInflater.from(HistoryCmdActivity.this).inflate(R.layout.slide_view_merge, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Util.dp2px(HistoryCmdActivity.this,100), ViewGroup.LayoutParams.MATCH_PARENT);
                mergeView.setLayoutParams(params);
                slideView.setMergeView(mergeView);
                convertView = slideView;
                holder = new Holder();
                holder.tvModify = (TextView) mergeView.findViewById(R.id.modify);
                holder.tvDelete = (TextView) mergeView.findViewById(R.id.delete);
                holder.tvCmd = (TextView) cView.findViewById(R.id.item_hist_cmd);
                holder.tvMark = (TextView) cView.findViewById(R.id.item_hist_remark);
                holder.tvTime = (TextView) cView.findViewById(R.id.item_hist_time);
                convertView.setTag(holder);
            }
            final CommandList command = listCommand.get(position);
            holder = (Holder) convertView.getTag();

            holder.tvCmd.setText(MainActivity.bytes2String(command.getCommandList().get(0).getCmd(),1));
            holder.tvMark.setText(command.getRemark());
            holder.tvTime.setText(DateUtil.dataToString(DateUtil.longToDate(command.getCreateTime()),"yyyy-MM-dd HH:mm:ss"));

            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = DbCommand.getInstance().delete(DbCommand.COLUMN_TIME+"=? and "+DbCommand.COLUMN_MARK+"=? ",
                            new String[]{command.getCreateTime()+"", command.getRemark()});
                    updateData();
                    curremtIndex = position;
                }
            });

            holder.tvModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryCmdActivity.this, AddCmdActivity.class);
                    intent.putExtra("type", AddCmdActivity.TYPE_MODIFY);
                    intent.putExtra("list", command.getCommandList());
                    startActivityForResult(intent, REQUEST_CODE);
                    curremtIndex = position;
                }
            });

            return convertView;
        }
    }

    private class Holder {
        TextView tvCmd;
        TextView tvMark;
        TextView tvTime;
        TextView tvDelete;
        TextView tvModify;
    }

}

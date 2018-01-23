package roc.cjm.bleconnect.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import roc.cjm.bleconnect.R;
import roc.cjm.bleconnect.activitys.cmds.Config;
import roc.cjm.bleconnect.activitys.cmds.databases.DbMcCommand;
import roc.cjm.bleconnect.activitys.cmds.databases.DbTable;
import roc.cjm.bleconnect.activitys.cmds.entry.Table;
import roc.cjm.bleconnect.activitys.w311test.McWriteActivity;
import roc.cjm.bleconnect.utils.DateUtil;
import roc.cjm.bleconnect.utils.DeviceConfiger;
import roc.cjm.bleconnect.utils.Util;
import roc.cjm.bleconnect.views.SlideListView2;
import roc.cjm.bleconnect.views.SlideView2;

/**
 * Created by Marcos on 2017/10/24.
 */

/*
*   显示历史记录
**/
public class LoadHistTableActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private SlideListView2 slideListView2;
    private List<Table> tableList;
    private List<Boolean> stateList;
    private MyAdapter myAdapter;
    private int profileType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadhist);

        profileType = getIntent().getIntExtra(DetailActivity.PROFILE_TYPE, Config.PROFILE_MC);

        slideListView2 = (SlideListView2) findViewById(R.id.loadhist_listview);
        tableList = new ArrayList<>();
        stateList = new ArrayList<>();
        myAdapter = new MyAdapter();
        slideListView2.setAdapter(myAdapter);
        slideListView2.setOnItemClickListener(this);

        loadData();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myAdapter.notifyDataSetChanged();
        }
    };

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                tableList = DbTable.getInstance().findAll(DbTable.COLUMN_PROFILE + "=?", new String[]{profileType + ""});
                if (tableList == null) {
                    tableList = new ArrayList<Table>();
                }
                stateList.clear();
                for (int i = 0; i < tableList.size(); i++) {
                    stateList.add(false);
                }
                handler.sendEmptyMessage(0x01);
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("table" , tableList.get(position));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setResult(RESULT_CANCELED);
        finish();
        return super.onKeyDown(keyCode, event);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return (tableList == null ? 0 : tableList.size());
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

                SlideView2 slideView = (SlideView2) LayoutInflater.from(LoadHistTableActivity.this).inflate(R.layout.item_slideview, null);

                TextView textView = new TextView(LoadHistTableActivity.this);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setLayoutParams(layoutParams);
                textView.setPadding(DeviceConfiger.dp2px(20), DeviceConfiger.dp2px(10), DeviceConfiger.dp2px(10), DeviceConfiger.dp2px(10));
                slideView.setContentView(textView);

                View mergeView = LayoutInflater.from(LoadHistTableActivity.this).inflate(R.layout.slide_view_merge, null);
                mergeView.findViewById(R.id.modify).setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Util.dp2px(LoadHistTableActivity.this, 50), ViewGroup.LayoutParams.MATCH_PARENT);
                mergeView.setLayoutParams(params);
                slideView.setMergeView(mergeView);

                convertView = slideView;
                holder = new Holder();
                holder.tvContent = textView;
                holder.tvDelete = (TextView) mergeView.findViewById(R.id.delete);
                holder.mergeView = mergeView;
                convertView.setTag(holder);

            }
            holder = (Holder) convertView.getTag();
            final Table table = tableList.get(position);
            holder.mergeView.setVisibility((table.getTableName().equals(DbMcCommand.DEFAULT_TABLE)?View.GONE:View.VISIBLE));
            holder.tvContent.setText(table.getTableMark() + "\r\n" + DateUtil.dataToString(DateUtil.longToDate(table.getDateTime()), "yyyy-MM-dd HH:mm:ss"));
            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DbTable.getInstance().delete(table);
                    loadData();
                }
            });
            return convertView;
        }
    }

    class Holder {
        TextView tvContent;
        TextView tvDelete;
        View mergeView;
    }
}


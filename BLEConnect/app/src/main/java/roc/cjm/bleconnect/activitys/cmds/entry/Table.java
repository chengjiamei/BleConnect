package roc.cjm.bleconnect.activitys.cmds.entry;

import android.app.ActionBar;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Marcos on 2017/10/24.
 */

public class Table implements Parcelable{
    public static final int TABLE_MAX = 20;///最多保存表的数量

    private int profileType;
    private int tableId;///表ID
    private String tableName;//表名
    private String tableMark;///备注，对表的说明
    private long dateTime;////创建表的时间

    public Table(int profileType, String tableName, String remark, long dateTime) {
        this.tableName = tableName;
        this.dateTime = dateTime;
        this.profileType = profileType;
        this.tableMark = remark;
    }

    public Table(int id, int profileType, String tableName, String remark, long dateTime) {
        this(profileType, tableName, remark, dateTime);
        this.tableId = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public String getTableMark() {
        return tableMark;
    }

    public void setTableMark(String tableMark) {
        this.tableMark = tableMark;
    }

    private Table(Parcel source){
        this.profileType = source.readInt();
        this.tableId = source.readInt();
        this.tableName = source.readString();
        this.tableMark = source.readString();
        this.dateTime = source.readLong();
    }

    public static final Creator<Table> CREATOR = new Creator<Table>(){

        @Override
        public Table createFromParcel(Parcel source) {
            return new Table(source);
        }

        @Override
        public Table[] newArray(int size) {
            return new Table[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(profileType);
        dest.writeInt(tableId);
        dest.writeString(tableName);
        dest.writeString(tableMark);
        dest.writeLong(dateTime);
    }
}

package roc.cjm.bleconnect.activitys.normal.entry;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/25.
 */

public class Command implements Serializable, Parcelable {

    private String characterUUID;//
    private String serviceUUID;
    private byte[] cmd;
    private String remark;//备注
    private boolean isExcuted;//是否已执行

    private Command(Parcel in) {
        readFromParcel(in);
    }

    public Command() {
        characterUUID = "";
        serviceUUID = "";
        remark = "";
        isExcuted = false;
    }

    public Command(String characterUUID, String serviceUUID, byte[] cmd, String remark, boolean isExcuted) {
        this.characterUUID = characterUUID;
        this.serviceUUID = serviceUUID;
        this.cmd = cmd;
        this.remark = remark;
        this.isExcuted = isExcuted;
    }

    public String getCharacterUUID() {
        return characterUUID;
    }

    public void setCharacterUUID(String characterUUID) {
        this.characterUUID = characterUUID;
    }

    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public byte[] getCmd() {
        return cmd;
    }

    public void setCmd(byte[] cmd) {
        this.cmd = cmd;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isExcuted() {
        return isExcuted;
    }

    public void setExcuted(boolean excuted) {
        isExcuted = excuted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serviceUUID);
        dest.writeString(this.characterUUID);
        dest.writeString(this.remark);
        dest.writeByteArray(cmd);
        dest.writeBooleanArray(new boolean[]{isExcuted});
    }

    private void readFromParcel(Parcel in) {
        this.serviceUUID = in.readString();
        this.characterUUID = in.readString();
        this.remark = in.readString();
        this.cmd = in.createByteArray();
        boolean[] isE = in.createBooleanArray();
        this.isExcuted = isE[0];
    }

    public final static Creator<Command> CREATOR = new Creator<Command>() {
        @Override
        public Command createFromParcel(Parcel source) {
            return new Command(source);
        }

        @Override
        public Command[] newArray(int size) {
            return new Command[size];
        }
    };
}

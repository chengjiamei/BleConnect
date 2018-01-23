package roc.cjm.bleconnect.activitys.cmds.entry;

/**
 * Created by Marcos on 2017/10/23.
 */

public class Command {

    private int index;
    private int profileType;
    private int type;///指令类别
    private byte[] command;///指令
    private byte[] defaultCmd;///默认指令
    private String remark;///备注


    public Command(int profileType, int type, int index , byte[] defaultCmd, byte[] command,  String remark) {
        this.type = type;
        this.command = command;
        this.remark = remark;
        this.profileType = profileType;
        this.defaultCmd = defaultCmd;
        this.index = index;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getProfileType() {
        return profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }

    public byte[] getDefaultCmd() {
        return defaultCmd;
    }

    public void setDefaultCmd(byte[] defaultCmd) {
        this.defaultCmd = defaultCmd;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}

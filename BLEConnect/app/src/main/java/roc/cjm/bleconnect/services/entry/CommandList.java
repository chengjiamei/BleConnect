package roc.cjm.bleconnect.services.entry;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/25.
 */

public class CommandList {

    private long createTime;//创建时间
    private String remark;///备注
    private ArrayList<Command> commandList;

    public CommandList(long createTime, String remark, ArrayList<Command> commandList) {
        this.createTime = createTime;
        this.remark = remark;
        this.commandList = commandList;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public ArrayList<Command> getCommandList() {
        return commandList;
    }

    public void setCommandList(ArrayList<Command> commandList) {
        this.commandList = commandList;
    }
}

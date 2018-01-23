package roc.cjm.bleconnect.activitys.cmds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import roc.cjm.bleconnect.activitys.cmds.databases.DbMcCommand;
import roc.cjm.bleconnect.activitys.cmds.entry.Command;
import roc.cjm.bleconnect.utils.DateUtil;

/**
 * Created by Marcos on 2017/10/23.
 */

public class McConfig {

    public static final int TYPE_SETTING = 0x01;//设置信息指令
    public static final int TYPE_DATA = 0x02;////数据操作指令
    public static final int TYPE_CONTROL = 0x03; ////控制指令

    private static volatile McConfig instance;

    private McConfig() {
       /*// new Thread(new Runnable() {
            @Override
            public void run() {
                initConfig();
            }
        }).start();*/
    }

    public static McConfig getInstance() {
        if(instance == null) {
            synchronized (McConfig.class) {
                if(instance == null) {
                    instance = new McConfig();
                }
            }
        }
        return instance;
    }

    public void reset() {
        DbMcCommand.getInstance().replace(initCommandList());
    }

    private List<Command> initCommandList() {
        List<Command> list = new ArrayList<>();
        list.add(setTimeCommand());
        list.add(setDisplay());
        list.add(setUserInfo());
        list.add(getUserInfoFromDevice());
        list.add(setScreenProtect());
        list.add(setAutoSleep());
        list.add(setDisplay());
        list.add(setAlarm());
        list.add(setWristeMode());
        list.add(setSedentary());
        list.add(setTimingTest());
        list.add(setHealthRemind());
        list.add(getDataRange());
        list.add(deleteData());
        list.add(setRealData());
        list.add(setHistoryDataRange());
        list.add(setCurrentData());
        list.add(setCallContact());
        list.add(setCallNumber());
        list.add(setMissedCallNumber());
        list.add(setMissedMsgNumber());
        list.add(setDeviceInfo());
        list.add(setAncs());
        list.add(setOpenAntiLost());
        list.add(setCloseAntiLost());
        list.add(findDevice());
        list.add(modifyBroadcastingName());
        list.add(sendNotiFirst());
        list.add(sendNotiSecond());
        list.add(sendNotiThird());
        list.add(sendNotiForth());
        list.add(setOTA());

        return list;
    }






    /**
     * 每次初始化的时候，更新数据库中的值
     * @return
     */
    private Command setTimeCommand() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconde = calendar.get(Calendar.SECOND);
        int timezone = DateUtil.getTimeZone();
        byte activeTimeZone = (byte) (timezone < 0 ? Math.abs(timezone) * 2 + 0x80 : Math.abs(timezone) * 2);
        byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x01 , (byte) 0xfe, 0, 1, activeTimeZone, activeTimeZone,
                (byte) ((year>>8) & 0xff), (byte) (year & 0xff), (byte) month, (byte) day, (byte) week, (byte) hour,(byte)  minute,(byte)  seconde};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 1, cmds, cmds,  "手机发送基准时间给设备");

    }

    /**
     * 设置显示时间
     * @return
     */
    private Command setDisplayTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconde = calendar.get(Calendar.SECOND);
        int timezone = DateUtil.getTimeZone();
        byte activeTimeZone = (byte) (timezone < 0 ? Math.abs(timezone) * 2 + 0x80 : Math.abs(timezone) * 2);
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x02, (byte) 0xfe, (byte) ((year>>8) & 0xff), (byte) (year & 0xff), (byte) month, (byte) day,
                (byte) week, activeTimeZone, (byte) hour, (byte) minute, (byte) seconde, 1};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 2, cmds, cmds, "设置手环显示时间");
    }

    /**
     * 设置用户信息
     * @return
     */
    private Command setUserInfo() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x03, (byte) 0xfe, (byte)((1990 >> 8) & 0xff) , (byte) (1990 & 0xff), 0x04, 0x06 ,
                (byte)((65>>8) & 0xff),(byte) ((65 & 0xff)), (byte)((10000 >> 16) & 0xff), (byte)((10000 >> 8) & 0xff), (byte)((10000 & 0xff)),
                (byte)((75 >> 8) & 0xff), (75 & 0xff), 8, 0};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 3, cmds, cmds, "设置用户基本参数");
    }

    /**
     * 获取设备保存的用户信息
     * @return
     */
    private Command getUserInfoFromDevice() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x03, (byte) 0xed};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 4, cmds, cmds, "获取用户信息");
    }

    /**
     * 设置屏幕待机与黑白屏
     * @return
     */
    private Command setScreenProtect() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x04, (byte) 0xfe, 0x01 , 0x01};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 5, cmds, cmds, "设置黑白屏与屏幕显示（W194）");
    }

    /**
     * 设置睡眠提醒
     * @return
     */
    private Command setAutoSleep() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x07, (byte) 0xfe, 0, 22, 0, 21, 45, 6, 0, 13, 0, 14, 0, 12, 45};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 6, cmds, cmds, "设置睡眠提醒");
    }

    /**
     * 设置屏幕显示和功能
     * @return
     */
    private Command setDisplay() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x08, (byte) 0xfe, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,(byte)  0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,(byte)  0xff
                , (byte) 0xff,(byte)  0xff,(byte)  0xff,(byte)  0xff,(byte)  0xff,(byte)  0xff,(byte)  0xff};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 7, cmds, cmds, "设置屏幕显示 和功能");
    }

    /**
     * 设置闹钟
     * @return
     */
    private Command setAlarm() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x09, (byte) 0xfe, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 8, cmds, cmds, "设置闹钟");
    }

    /**
     * 设置左右手
     * @return
     */
    private Command setWristeMode() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x0b, (byte) 0xfe, 0x00};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 9, cmds, cmds, "设置左右手");
    }

    /**
     * 设置久坐提醒
     * @return
     */
    private Command setSedentary() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x0c, (byte) 0xfe, 0x00, 0x00 , 0x00 , 0x00, 0x00 , 0x00 , 0x00, 0x00 , 0x00 , 0x00
                , 0x00 , 0x00 , 0x00, 0x00 , 0x00};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 10, cmds, cmds, "设置久坐提醒");
    }

    /**
     * 设置自动测试心率
     * @return
     */
    private Command setTimingTest(){
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x15, (byte) 0xfe, 0x00, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 11, cmds, cmds, "设置自动测试心率");
    }

    /**
     * 设置健康提醒
     * @return
     */
    private Command setHealthRemind() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x01, 0x16, (byte) 0xfe, 0x00, (byte) 0x00 , (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff , (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_SETTING, 12, cmds, cmds, "设置健康提醒");
    }


    /**
     * 获取某天的历史数据
     */
    private Command getDataRange() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        byte[] cmds = new byte[] {(byte) 0xbe, 0x02, 0x01, (byte) 0xfe, (byte) ((year >> 8) & 0xff), (byte) (year & 0xff),
                (byte) month, (byte) day, 0};
        return new Command(Config.PROFILE_MC, TYPE_DATA, 1, cmds, cmds, "请求某日期的历史数据");
    }

    /**
     * 删除从某个日期的开始的运动睡眠数据
     * @return
     */
    private Command deleteData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        byte[] cmds = new byte[] {(byte) 0xbe, 0x02, 0x02, (byte) 0xfe, (byte) ((year >> 8) & 0xff), (byte) (year & 0xff),
                (byte) month, (byte) day, 0};
        return new Command(Config.PROFILE_MC, TYPE_DATA, 2, cmds, cmds, "删除从某个日期开始的运动睡眠数据");
    }

    /**
     * 开启实时传输数据
     * @return
     */
    private Command setRealData() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x02, 0x03, (byte) 0xed};
        return new Command(Config.PROFILE_MC, TYPE_DATA, 3, cmds, cmds, "开启实时传输数据");
    }

    /**
     * 获取历史数据时间段
     * @return
     */
    private Command setHistoryDataRange() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x02, 0x05, (byte) 0xed};
        return new Command(Config.PROFILE_MC, TYPE_DATA, 4, cmds, cmds, "获取历史数据时间段");
    }

    /**
     * 获取当天数据
     * @return
     */
    private Command setCurrentData() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x02, 0x06, (byte) 0xfe, 0x01, (byte) 0xed};
        return new Command(Config.PROFILE_MC, TYPE_DATA, 5, cmds, cmds, "获取当天运动数据");
    }


    /**
     * 发送手机来电联系人的名字
     * @return
     */
    private Command setCallContact() {
        byte[] name = "chengjiamei".getBytes();
        byte[] cmds = new byte[20];
        cmds[0] = (byte) 0xbe;
        cmds[1] = 0x06;
        cmds[2] = 0x01;
        cmds[3] = (byte) 0xfe;
        cmds[4] = (byte) name.length;
        System.arraycopy(name, 0, cmds,5, name.length);
        for (int i = 5 + name.length;i<20;i++) {
            cmds[i] = (byte) 0xff;
        }
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 1, cmds, cmds, "发送手机来电联系人的名字");
    }

    /**
     * 发送手机来联系人的电话号码
     * @return
     */
    private Command setCallNumber() {
        byte[] name = "13824350547".getBytes();
        byte[] cmds = new byte[20];
        cmds[0] = (byte) 0xbe;
        cmds[1] = 0x06;
        cmds[2] = 0x02;
        cmds[3] = (byte) 0xfe;
        cmds[4] = (byte) name.length;
        System.arraycopy(name, 0, cmds,5, name.length);
        for (int i = 5 + name.length;i<20;i++) {
            cmds[i] = (byte) 0xff;
        }
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 2, cmds, cmds, "发送手机来联系人的电话号码");
    }

    /**
     * 发送未接来电数量
     * @return
     */
    private Command setMissedCallNumber() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x03, (byte) 0xfe, 00};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 3, cmds, cmds, "发送未接来电数量");
    }

    /**
     * 发送未读短信数量
     * @return
     */
    private Command setMissedMsgNumber() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x04, (byte) 0xfe, 00};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 4, cmds, cmds, "发送未读短信数量");
    }

    /**
     * 获取设备信息
     * @return
     */
    private Command setDeviceInfo() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x09, (byte) 0xfb};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 5, cmds, cmds, "获取设备信息");
    }

    /**
     * 启动ANCS
     * @return
     */
    private Command setAncs() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x0B, (byte) 0xED};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 6, cmds, cmds, "启动ANCS");
    }

    /**
     * 开启防丢提醒
     * @return
     */
    private Command setOpenAntiLost(){
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x0D, (byte) 0xED};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 7, cmds, cmds, "开启防丢提醒");
    }

    /**
     * 关闭防丢提醒
     * @return
     */
    private Command setCloseAntiLost(){
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x0E, (byte) 0xED};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 8, cmds, cmds, "关闭防丢提醒");
    }

    /**
     * 查找设备
     * @return
     */
    private Command findDevice() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x0F, (byte) 0xED};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 9, cmds, cmds, "查找设备");
    }

    /**
     * 修改广播名和自检显示的名字
     * @return
     */
    private Command modifyBroadcastingName() {
        byte[] name = "W311N".getBytes();
        byte[] cmds = new byte[20];
        cmds[0] = (byte) 0xbe;
        cmds[1] = 0x06;
        cmds[2] = 0x11;
        cmds[3] = (byte) 0xfe;
        cmds[4] = (byte) 0;
        System.arraycopy(name, 0, cmds,5, name.length);
        for (int i = 5 + name.length;i<20;i++) {
            cmds[i] = (byte) 0xff;
        }
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 10, cmds, cmds, "修改广播名和自检显示的名字");
    }

    /**
     * 消息推送第一个数据包
     * @return
     */
    private Command sendNotiFirst() {
        byte[] name = "Hello World".getBytes();
        byte[] cmds = new byte[]{(byte) 0xbe , 0x06, 0x12,  (byte) 0xfe, 0x01, 0x4D, 0x61 , 0x72 , 0x63 , 0x6F , 0x73 ,
                (byte) 0xFF,  (byte) 0xFF ,  (byte) 0xFF ,  (byte) 0xFF , (byte)  0xFF ,  (byte) 0xFF,  (byte) 0xFF ,  (byte) 0xFF , (byte) 0xFF};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 11, cmds, cmds, " 消息推送第一个数据包");
    }

    /**
     * 消息推送第二个数据包
     * @return
     */
    private Command sendNotiSecond() {//BE 06 12 FE 01 4D 61 72 63 6F 73 FF FF FF FF FF FF FF FF FF
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x12, (byte) 0xfe, 0x02, (byte) 0xff , (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff , (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 12, cmds, cmds, "消息推送第二个数据包");
    }

    /**
     * 消息推送第三个数据包
     * @return
     */
    private Command sendNotiThird() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x12, (byte) 0xfe, 0x03, (byte) 0xff , (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff , (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 13, cmds, cmds, "消息推送第三个数据包");
    }

    /**
     * 消息推送第四个数据包
     * @return
     */
    private Command sendNotiForth() {
        byte[] cmds = new byte[] {(byte) 0xbe, 0x06, 0x12, (byte) 0xfe, 0x04, (byte) 0xff , (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff , (byte) 0xff, (byte) 0xff
                , (byte) 0xff , (byte) 0xff, (byte) 0xff};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 14, cmds, cmds, "消息推送第四个数据包");
    }

    /**
     * 设置OTA模式
     * @return
     */
    private Command setOTA() {
        byte[] cmds = new byte[]{(byte) 0xbe, (byte) 0xfe, 0x44, 0x46, 0x55, 0x01, 0x02, 0x00, (byte) 0xf0, 0x02};
        return new Command(Config.PROFILE_MC, TYPE_CONTROL, 14, cmds, cmds, "设置OTA模式");
    }


}

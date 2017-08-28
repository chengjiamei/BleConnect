package roc.cjm.bleconnect.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

	public static String getTodayStr(String format){
		Calendar calendar = Calendar.getInstance();
		return dataToString(calendar.getTime(),format);
	}

	public static String dataToString(Date date, String format){
		SimpleDateFormat format1 = new SimpleDateFormat(format);
		return format1.format(date);
	}

	public static Date stringToDate(String date, String format){
		SimpleDateFormat format1 = new SimpleDateFormat(format);
		try {
			return format1.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date longToDate(long time){
		return new Date(time);
	}

	public static String toString(int number){
		return number<10?"0"+number:""+number;
	}

	public static boolean is24Hour(Context context) {
		String timeFormat = android.provider.Settings.System.getString(context.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		return DateFormat.is24HourFormat(context);
/*
		if(timeFormat.equals("24")){
			return true;
		}
		return false;*/
	}

	public static int getTimeZone(){
		TimeZone timezone = TimeZone.getDefault();
		int rawOffSet = timezone.getRawOffset();
		int offset = rawOffSet/3600000;
		return offset;
	}

	public static int getWeek(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static int getYear(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static int getDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public static Calendar getCurrentCalendar(){
		return Calendar.getInstance();
	}

	public static Date getCurrentDate(){
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

}
package hz.dodo.data;

import java.util.Calendar;
import java.util.Locale;

public class DTimer
{
	protected
	String[]
			sArrWeeks;

	public DTimer()
	{
		sArrWeeks = new String[]{ "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
	}
	
	public String[] decode(final long milliseconds)
	{
		Calendar cal = Calendar.getInstance(Locale.CHINESE);
		cal.setTimeInMillis(milliseconds);
		return decode(cal);
	}

	public String[] decode(final Calendar cal)
	{
		String[] sArr = new String[10];
		int
			month,
			day,
			week;
		
		// 年/月/日
		sArr[0] = "" + cal.get(Calendar.YEAR);
		sArr[1] = String.format("%02d", month = (cal.get(Calendar.MONTH) + 1));
		sArr[2] = String.format("%02d", day = cal.get(Calendar.DAY_OF_MONTH));
		
		// 时/分/秒
		sArr[3] = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
		sArr[4] = String.format("%02d", cal.get(Calendar.MINUTE));
		sArr[5] = String.format("%02d", cal.get(Calendar.SECOND));

		sArr[6] = "" + cal.get(Calendar.DAY_OF_YEAR); // 一年当中第?天
		sArr[7] = "" + (week = (cal.get(Calendar.DAY_OF_WEEK) - 1)); // 阿拉伯数字,周几(0:周日, 1:周一 ... 依次类推)
		if(week >= 0 && week < 7)
		{
			sArr[8] = sArrWeeks[week]; // 中文 周?
		}
		else
		{
			sArr[8] = "";
		}
		
		if(month == 1 && day == 1)
		{
			sArr[9] = "元旦";
		}
		else if(month == 2 && day == 14)
		{
			sArr[9] = "情人节";
		}
		else if(month == 3 && day == 8)
		{
			sArr[9] = "妇女节";
		}
		else if(month == 3 && day == 12)
		{
			sArr[9] = "植树节";
		}
		else if(month == 3 && day == 15)
		{
			sArr[9] = "维权日";
		}
		else if(month == 4 && day == 1)
		{
			sArr[9] = "愚人节";
		}
		else if(month == 5 && day == 1)
		{
			sArr[9] = "劳动节";
		}
		else if(month == 5 && day == 4)
		{
			sArr[9] = "青年节";
		}
		else if(month == 6 && day == 1)
		{
			sArr[9] = "儿童节";
		}
		else if(month == 7 && day == 1)
		{
			sArr[9] = "建党节";
		}
		else if(month == 8 && day == 1)
		{
			sArr[9] = "建军节";
		}
		else if(month == 9 && day == 10)
		{
			sArr[9] = "教师节";
		}
		else if(month == 10 && day == 1)
		{
			sArr[9] = "国庆节";
		}
		else if(month == 11 && day == 11)
		{
			sArr[9] = "光棍节";
		}
		else if(month == 12 && day == 24)
		{
			sArr[9] = "平安夜";
		}
		else if(month == 12 && day == 25)
		{
			sArr[9] = "圣诞节";
		}
		else
		{
			sArr[9] = "";
		}
		
		return sArr;
	}
}
package hz.dodo;

import hz.dodo.data.Empty;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.EncodingUtils;

import android.graphics.Paint;

@SuppressWarnings ("deprecation")
public class StrUtil
{
	// 转换文件大小
	static public String formatSize(final double size)
	{
		if(size < 0) return "0";
		
		String fileSizeString = "";
		if (size < FileUtil.KB) fileSizeString = (double) (Math.round(size * 10) / 10.0) + "B";
		else if (size < FileUtil.MB) fileSizeString = (double) (Math.round((size / FileUtil.KB) * 10) / 10.0) + "K";
		else if (size < FileUtil.GB) fileSizeString = (double) (Math.round((size / FileUtil.MB) * 10) / 10.0) + "M";
		else fileSizeString = (double) (Math.round((size / FileUtil.GB) * 10) / 10.0) + "G";
		return fileSizeString;
	}
	static public String formatSize2(final double size)
	{
		if(size <= 0) return "0B";
		
		String fileSizeString = "";
		if (size < FileUtil.KB)
		{
			fileSizeString = (long)(size * 100) / 100.0 + "B";
		}
		else if (size < FileUtil.MB)
		{
			fileSizeString = (long)(size / FileUtil.KB * 100) / 100.0 + "K";
			
		}
		else if (size < FileUtil.GB)
		{
			fileSizeString = (long)(size / FileUtil.MB * 100) / 100.0 + "M";
		}
		else
		{
			fileSizeString = (long)(size / FileUtil.GB * 100) / 100.0 + "G";
		}
		return fileSizeString;
	}
	
	// 获取指定宽度字符数组
	static public String[] getDesArray(final String res, final int maxwidth, final Paint paint)
	{
		try
		{
			if(res == null || res.length() <= 0 || maxwidth <= 0 || paint == null) return null;

			List<String> list = new ArrayList<String>();
			
			String str = res.trim();
			String si1;
			int breakCount, stri = 0, i1 = 0;
			
			while(true)
			{
				si1 = str.substring(stri);
				if(si1 == null || si1.length() <= 0) break;
				breakCount = paint.breakText(si1, true, maxwidth, null);
				list.add(si1.substring(0, breakCount));
				stri += breakCount;
			}

			String[] rsts = new String[list.size()];
			i1 = 0;
			while(i1 < list.size())
			{
				rsts[i1] = list.get(i1);
				i1++;
			}
			return rsts;
		}
		catch(Exception e1)
		{
			System.out.println("StrUtil getDesArray error: " + e1.toString());
		}
		return null;
	}
	
	// 获取指定宽度字符数组
	static public String[] getDesArray(final String res, final int maxwidth, final Paint paint, boolean trim)
	{
		try
		{
			if(res == null || res.length() <= 0 || maxwidth <= 0 || paint == null) return null;

			List<String> list = new ArrayList<String>();
			
			String str;
			if(trim)str = res.trim();
			else str = res;
			String si1;
			int breakCount, stri = 0, i1 = 0;
			
			while(true)
			{
				si1 = str.substring(stri);
				if(si1 == null || si1.length() <= 0) break;
				breakCount = paint.breakText(si1, true, maxwidth, null);
				list.add(si1.substring(0, breakCount));
				stri += breakCount;
			}

			String[] rsts = new String[list.size()];
			i1 = 0;
			while(i1 < list.size())
			{
				rsts[i1] = list.get(i1);
				i1++;
			}
			return rsts;
		}
		catch(Exception e1)
		{
			System.out.println("StrUtil getDesArray error: " + e1.toString());
		}
		return null;
	}
	// 如果有回车符,则按回车符处理
	static public String[] getArr(final String res, final int maxwidth, final Paint paint, boolean trim)
	{
		try
		{
			if(res == null || res.length() <= 0 || maxwidth <= 0 || paint == null) return null;
			
			String str = trim ? res.trim() : res;
			
			String[] sArr = str.split("\n");
			if(sArr != null)
			{
				List<String> lt = new ArrayList<String>();
				String[] sArrTmp;
				int
					i1 = 0,
					i2 = 0;
				while(i1 < sArr.length)
				{
					if(null != (sArrTmp = getDesArray(sArr[i1], maxwidth, paint, trim)))
					{
						i2 = 0;
						while(i2 < sArrTmp.length)
						{
							lt.add(sArrTmp[i2]);
							++i2;
						}
					}
					++i1;
				}
				
				i2 = lt.size();
				if(i2 > 0)
				{
					String[] sArrRst = new String[i2];
					i1 = 0;
					while(i1 < i2)
					{
						sArrRst[i1] = lt.get(i1);
						++i1;
					}
					return sArrRst;
				}
			}
			
		}
		catch(Exception ext)
		{
			Logger.e("getArr()" + ext.toString());
		}
		return null;
	}
	// 截取字符串
	static public String breakText(final String res, final int maxwidth, final Paint paint)
	{
		if(res == null || res.length() <= 0 || maxwidth <= 0 || paint == null) return "";
		
		try
		{
			if (paint.measureText(res) <= maxwidth) return res;

			int count = paint.breakText(res, true, maxwidth, null); // 第二个参??true从头????, false从尾????
			return res.substring(0, count-1) + "…";
		}
		catch(Exception e1)
		{
			Logger.e("breakText()=" + e1.toString());
		}
		return "";
	}
	// 回返PIX
	public static int pix(final Paint paint, final String str)
	{
		if(paint == null || Empty.isEmpty(str)) return 0;
		return (int) paint.measureText(str);
	}
	public static int pixHalf(final Paint paint, final String str)
	{
		return pix(paint, str)/2;
	}
	public static byte[] getByte(InputStream is)
	{
		try
		{
			ByteArrayOutputStream babuf = new ByteArrayOutputStream();
			byte[] buf = new byte[5120];
			int reading = 0;
			
			while ((reading = is.read(buf)) != -1)
			{
				babuf.write(buf, 0, reading);
			}
			return babuf.toByteArray();
		}
		catch(Exception e1)
		{
			Logger.e("net work get byte[] error: " + e1.toString());
		}
		
		return null;
	}
	
	public static String getString(InputStream is)
	{
		try
		{
			return new String(getByte(is), "UTF-8");
		}
		catch(Exception e1)
		{
			Logger.e("getString()=" + e1.toString());
		}
		return null;
	}
	
	public static String zh2url(final String zh)
	{
		if(zh == null || zh.length() <= 0) return "";
		try
		{
			return URLEncoder.encode(zh, "UTF-8");
		}
		catch(Exception e1)
		{
			Logger.e("zh2url()" + e1.toString());
		}
		return "";
	}
	
	public static String url2zh(final String urlzh)
	{
		if(urlzh == null || urlzh.length() <= 0) return "";
		try
		{
			return URLDecoder.decode(urlzh, "UTF-8");
		}
		catch(Exception e1)
		{
			Logger.e("url2zh()" + e1.toString());
		}
		return "";
	}
	
	// "UTF-8" / "unicode" / "gbk"
	public static String getString(final byte[] buffer, final String code)
	{
		return EncodingUtils.getString(buffer, code);
	}
	
	public static String formatTimer(String template, long ms)
	{
		return new SimpleDateFormat(template, Locale.CHINESE).format(new Date(ms));
	}
	
	public static String formatTime1(long ms)
	{
		 return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(new Date(ms));
	}
	
	public static String formatTime2(long ms)
	{
		 return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date(ms));
	}
	public static String formatTimer3(long ms)
	{
		 return new SimpleDateFormat("MM月dd日 HH时mm分", Locale.CHINESE).format(new Date(ms));
	}
	
	public static String formatTimer4(long ms)
	{
		try
		{
			if(ms <= 0) return "0秒";
			long mill = ms / 1000;
			long hour = mill / (60 * 60); // 获得小时
			mill = mill % (60 * 60); // 除去小时
			long min = mill / 60; // 获得分钟
//			long sec = mill % 60; // 除去分钟,获得秒
			
			if(hour > 0)
			{
				return hour + "小时" + min + "分钟";
			}
			else
			{
				return min + "分钟";
			}
		}
		catch(Exception e1)
		{
			Logger.e("formatTimer4()=" + e1.toString());
		}
		return "";
	}
	
	public static String formatTimer5(long ms)
	{
		return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE).format(new Date(ms));
	}
	
	public static String formatTimer6(long ms)
	{
		return new SimpleDateFormat("HH:mm", Locale.CHINESE).format(new Date(ms));
	}
	
	public static long formatTimer7(int year, int mouth, int day, int hour, int min, int sec)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0L);
		calendar.set(year, mouth-1, day, hour, min, sec);
		return calendar.getTimeInMillis();
	}
	public static int formatYear(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.YEAR);
	}
	public static int formatMonth(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.MONTH) + 1;
	}
	public static int formatDay(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	
	public static int formatHour(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int formatMin(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.MINUTE);
	}
	
	public static int formatWeek(long ms)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms);
		return calendar.get(Calendar.DAY_OF_WEEK) - 1;
	}
	
	public static String formatProgress(float progress)
	{
		Float p = Float.valueOf(progress * 100);
		return p.intValue() + "%" ;
	}
	
	public static String replaceBlank(String str)
	{
		String dest = "";
		if (str != null && str.length() > 0)
		{
			str = str.trim();
//			Pattern p = Pattern.compile("\\s*|\t|\r|\n"); // 去除字符串中的空格、回车、换行符、制表符
			Pattern p = Pattern.compile("\\s*");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}
    
//	Collections.reverse(Arrays.asList(array)); // 降序
//	Collections.sort(Arrays.asList(array)); // 升序
	
//	Sorting an Array
//    1. 数字排序  int[] intArray = new int[] { 4, 1, 3, -23 };
//Arrays.sort(intArray);
//输出： [-23, 1, 3, 4]
//2. 字符串排序，先大写后小写 String[] strArray = new String[] { "z", "a", "C" };
//Arrays.sort(strArray);
//输出： [C, a, z]
//3. 严格按字母表顺序排序，也就是忽略大小写排序 Case-insensitive sort
//Arrays.sort(strArray, String.CASE_INSENSITIVE_ORDER);
//输出： [a, C, z]
//4. 反向排序， Reverse-order sort
//Arrays.sort(strArray, Collections.reverseOrder());
//输出：[z, a, C]
//5. 忽略大小写反向排序 Case-insensitive reverse-order sort
//Arrays.sort(strArray, String.CASE_INSENSITIVE_ORDER);
//Collections.reverse(Arrays.asList(strArray));

//	public static Object[] listToArray(List<?> lt)
//	{
//		if(lt == null || lt.size() <= 0) return null;
//		return lt.toArray(new Object[lt.size()]);
//	}
//	
//	public static List<?> arrayToList(Object[] array)
//	{
//		if(array == null || array.length <= 0) return null;
//		return Arrays.asList(array);
//	}
	
	public static final String byte2hex(byte bts[])
	{
		if(bts == null) return null;
		
		try
		{
			String hs = "";
			String stmp = "";
			for (int n = 0; n < bts.length; n++)
			{
				stmp = Integer.toHexString(bts[n] & 0xff);
				if (stmp.length() == 1) hs = hs + "0" + stmp;
				else hs = hs + stmp;
			}
			return hs.toUpperCase(Locale.getDefault());
		}
		catch(Exception e1)
		{
			Logger.e("byte2hex()=" + e1.toString());
		}
		
		return null;
	}

	public static final byte[] hex2byte(String hex)
	{
		try
		{
			if (hex == null || hex.length() % 2 != 0) return null;
			
			char[] arr = hex.toCharArray();
			byte[] b = new byte[hex.length() / 2 + 1];
			for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++)
			{
				String swap = "" + arr[i++] + arr[i];
				int byteint = Integer.parseInt(swap, 16) & 0xFF;
//				b[j] = new Integer(byteint).byteValue();
				b[j] = Integer.valueOf(byteint).byteValue();
			}
			return b;
		}
		catch(Exception e1)
		{
			Logger.e("hex2byte()=" + e1.toString());
		}
		return null;
	}
	
	@Deprecated
	public static String unicodeToChinese(String str)
	{
		String[] strings = str.split(";");
		StringBuffer aStr = new StringBuffer();
		for (int i = 0; i < strings.length; i++)
		{
			String s = strings[i].replace("&#", "");
			aStr.append((char) Integer.parseInt(s));
		}
		return aStr.toString();
	}

	@Deprecated
	public static String chineseToUnicode(String str)
	{
		char[] arChar = str.toCharArray();
		int iValue = 0;
		StringBuffer uStr = new StringBuffer();
		for (int i = 0; i < arChar.length; i++)
		{
			iValue = (int) str.charAt(i);
			uStr.append("&#" + iValue + ";");
		}
		return uStr.toString();
	}
	
	@SuppressWarnings ("unused") // 遍历HashMap
	private void hashMapPrint()
	{
		HashMap<String, String> ht = new HashMap<String, String>();
		Iterator<Entry<String, String>> iter = ht.entrySet().iterator();
		while(iter.hasNext())
		{ 
			Entry<String, String> entry = (Entry<String, String>)iter.next();
			entry.getKey();
			entry.getValue();
		}
	}
	
	// 浮点型转String,参数为保留小数点后几位,会四舍五入
	public static String getStr(final double fval, final int ival)
	{
		if(ival <= 0)
		{
			DecimalFormat fnum = new DecimalFormat("##0");
			return fnum.format(fval);
		}

		String s1 = "";
		int i1 = 0;
		while(i1 < ival)
		{
			s1 += "0";
			++i1;
		}

		// 处理浮点型保留小数点后几位,会四舍五入
		DecimalFormat fnum = new DecimalFormat("##0." + s1);
		return fnum.format(fval);
	}
	// 浮点型转String,参数为保留小数点后几位,不四舍五入
	public static String getStr2(final double fval, final int ival)
	{
		String s1 = "" + fval;
		int E = s1.lastIndexOf("E-");
		int point = s1.indexOf(".");
		if(E > 0)
		{
			try
			{
				int i1 = Integer.parseInt(s1.substring(E + "E-".length()));
				while(i1 > 1)
				{
					s1 = "0" + s1;
					--i1;
				}
				int tmpPoint = s1.indexOf("."); 
				if(tmpPoint > 0 && tmpPoint < s1.length()-1)
				{
					s1 = "0." + s1.substring(0, tmpPoint) + s1.substring(tmpPoint + 1, s1.lastIndexOf("E-"));
				}
				point = s1.indexOf(".");
			}
			catch(Exception ext)
			{
				Logger.e("getStr2 " + fval + " " + ext.toString());
				s1 = "" + fval;
				point = s1.indexOf(".");
			}
		}
		while(true)
		{
			if(point + ival >= s1.length())
			{
				s1 += "0";
			}
			else
			{
				break;
			}
		}
		return s1.substring(0, point + ival + 1);
	}
	// 是否为半角 char cc = s1.charAt(i1);
	public static boolean isHalf(final char cc)
	{
		if (!(('\uFF61' <= cc) && (cc <= '\uFF9F')) && !(('\u0020' <= cc) && (cc <= '\u007E')))
		{
			return false;
		}
		return true;
	}
}

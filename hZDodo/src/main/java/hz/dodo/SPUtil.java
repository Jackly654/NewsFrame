package hz.dodo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPUtil
{
	// string
	public static String getString(final Context ctx, final String tableName, final String key, final String defaultValue)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		return sp.getString(key, defaultValue);
	}
	
	public static void saveString(final Context ctx, final String tableName, final String key, final String value)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putString(key, value);
		et.commit();
	}
	
	// int
	public static int getInt(final Context ctx, final String tableName, final String key, final int defaultValue)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		return sp.getInt(key, defaultValue);
	}
	
	public static void saveInt(final Context ctx, final String tableName, final String key, final int value)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(key, value);
		et.commit();
	}
	
	// long
	public static long getLong(final Context ctx, final String tableName, final String key, final long defaultValue)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		return sp.getLong(key, defaultValue);
	}
	
	public static void saveLong(final Context ctx, final String tableName, final String key, final long value)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putLong(key, value);
		et.commit();
	}
	
	// bool
	public static boolean getBool(final Context ctx, final String tableName, final String key, final boolean defaultValue)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		return sp.getBoolean(key, defaultValue);
	}
	
	public static void saveBool(final Context ctx, final String tableName, final String key, final boolean value)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putBoolean(key, value);
		et.commit();
	}
	
	// remove
	public static void remove(final Context ctx, final String tableName, final String key)
	{
		SharedPreferences sp = ctx.getSharedPreferences(tableName, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.remove(key);
		et.commit();
	}
}

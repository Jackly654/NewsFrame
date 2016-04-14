package hz.dodo.data;

import hz.dodo.FileUtil;
import hz.dodo.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;

// 各种开关
public class DSwitch
{
	public static final String ACTION_SWITCH_CHANGED = "hz.dodo.ACTION_SWITCH_CHANGED";
	public static final String ACTION_SWITCH_DEL = "hz.dodo.ACTION_SWITCH_DEL";
	
	final String FILE_NAME = "config";
	final String DIVIDER_KEY_VALUE = "<=>";
	
	Context ctx;
	FileUtil fu;
	HashMap<String, String> hmValue;
	
	public DSwitch(Context ctx)
	{
		this.ctx = ctx;
		fu = new FileUtil();
		hmValue = new HashMap<String, String>();
		init();
	}
	public boolean put(final String key, final String value)
	{
		if(key == null || value == null) return false;

		hmValue.put(key, value);
		
		Intent it = new Intent(ACTION_SWITCH_CHANGED);
		it.putExtra(key, value);
		ctx.sendBroadcast(it);
		
		return reWrite();
	}
	public void remove(final String key)
	{
		if(key == null) return;

		if(hmValue.remove(key) != null)
		{
			Intent it = new Intent(ACTION_SWITCH_DEL);
			it.putExtra(key, "null");
			ctx.sendBroadcast(it);
			
			reWrite();
		}
	}
	public String get(final String key)
	{
		if(key == null) return null;
		return hmValue.get(key);
	}
	public void destroy()
	{
		hmValue.clear();
	}
	private void init()
	{
		hmValue.clear();
		
		try
		{
			String read = fu.readPrivate(ctx, FILE_NAME);
			if(read != null)
			{
				read = read.trim();
				String[] sArr = read.split("\n");
				if(sArr != null)
				{
					String[] sArr2 = null;
					int i1 = 0;
					while(i1 < sArr.length)
					{
						if((null != (sArr2 = sArr[i1].split(DIVIDER_KEY_VALUE))) && sArr2.length == 2)
						{
							hmValue.put("" + sArr2[0], "" + sArr2[1]);
						}
						++i1;
					}
				}
			}
		}
		catch(Exception ext)
		{
			Logger.e("Switch::init() " + ext.toString());
		}
	}
	private boolean reWrite()
	{
		try
		{
			Iterator<Entry<String, String>> iter = hmValue.entrySet().iterator();
			if(iter != null)
			{
				Entry<String, String> entry = null;
				StringBuilder sb = new StringBuilder();
				String
					key = null,
					value = null;
				
				while(iter.hasNext())
				{ 
					if(null != (entry = (Entry<String, String>)iter.next()))
					{
						key = entry.getKey();
						value = entry.getValue();
						if(key != null && key.length() > 0 && value != null && value.length() > 0)
						{
							sb.append(key + DIVIDER_KEY_VALUE + value + "\n");
						}
					}
				}
				if(FileUtil.rst_success == fu.writePrivate(ctx, FILE_NAME, sb.toString()))
				{
					return true;
				}
			}
		}
		catch(Exception ext)
		{
			Logger.e("DSwitch::reWrite() " + ext.toString());
		}
		return false;
	}
	
	public void printValue()
	{
		Iterator<Entry<String, String>> iter = hmValue.entrySet().iterator();
		if(iter != null)
		{
			Entry<String, String> entry = null;
			while(iter.hasNext())
			{ 
				if(null != (entry = (Entry<String, String>)iter.next()))
				{
					Logger.i("key:" + entry.getKey() + ", value:" + entry.getValue());
				}
			}
		}
	}
}
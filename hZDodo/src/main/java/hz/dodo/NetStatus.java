package hz.dodo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class NetStatus
{
//	public interface CallBack
//	{
//		public void sendNetStatus(boolean connected, int type);
//	}
	
	public static final int NET_NA = -1;
	public static final int NET_WIFI = 1;
	public static final int NET_2G = 2;
	public static final int NET_3G = 3;
	public static final int NET_4G = 4;
	
	public static final String B_NET_CONNECTED = "B_NET_CONNECTED";
	public static final String B_NET_UNCONNECT = "B_NET_UNCONNECT";
	
	Context ctx;
//	CallBack callback;
//	long lastRe;
	String tag;
	int netType;
	
	public NetStatus(Context ctx/*, CallBack callback*/)
	{
		this.ctx = ctx;
		netType = NET_NA;
//		this.callback = callback;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        ctx.registerReceiver(netReceiver, filter);
	}

	private BroadcastReceiver netReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				if (intent == null) return;
					
				if ( (ConnectivityManager.CONNECTIVITY_ACTION).equals(intent.getAction()))
				{
//					long cur = System.currentTimeMillis();
//					if(cur - lastRe < 1000) return;
//					lastRe = cur;
					
					ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo info = cm.getActiveNetworkInfo();
					
					if (info != null && info.isAvailable() && info.isConnected())
					{
						String newTag = info.getState() + info.getTypeName() + "";
						if(!newTag.equals("" + tag))
						{
							tag = newTag;
							
							if (info.getState() == NetworkInfo.State.CONNECTED)
							{
								if (info.getTypeName().equals("WIFI"))
								{
									netType = NET_WIFI;
									
									Intent it = new Intent(B_NET_CONNECTED);
									it.putExtra("netType", NET_WIFI);
									ctx.sendBroadcast(it);
									
									Logger.i("网络已连接-WIFI");
									
//									if(callback != null) callback.sendNetStatus(true, NET_WIFI);
								}
								else
								{
									TelephonyManager telmng = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
									if (telmng.getDataState() == TelephonyManager.DATA_CONNECTED)
									{
										Logger.i("net work type: " + telmng.getNetworkType());
										Intent it = new Intent(B_NET_CONNECTED);
										switch (telmng.getNetworkType())
										{
											case TelephonyManager.NETWORK_TYPE_GPRS:
											case TelephonyManager.NETWORK_TYPE_EDGE:
											case TelephonyManager.NETWORK_TYPE_CDMA:
												Logger.i("网络已连接-2G");
												
												netType = NET_2G;
												it.putExtra("netType", NET_2G);
//												if(callback != null) callback.sendNetStatus(true, NET_2G);
												break;
											case TelephonyManager.NETWORK_TYPE_EVDO_0:
											case TelephonyManager.NETWORK_TYPE_EVDO_A:
											case TelephonyManager.NETWORK_TYPE_EVDO_B:
											case TelephonyManager.NETWORK_TYPE_UMTS:
											case TelephonyManager.NETWORK_TYPE_HSDPA:
											case TelephonyManager.NETWORK_TYPE_HSPA:
											case TelephonyManager.NETWORK_TYPE_HSPAP:
											case TelephonyManager.NETWORK_TYPE_HSUPA:
												Logger.i("网络已连接-3G");
												
												netType = NET_3G;
												it.putExtra("netType", NET_3G);
//												if(callback != null) callback.sendNetStatus(true, NET_3G);
												break;
											case TelephonyManager.NETWORK_TYPE_LTE:
												Logger.i("网络已连接-4G");
												netType = NET_4G;
												it.putExtra("netType", NET_4G);
												break;
										}
										ctx.sendBroadcast(it);
									}
								}
							}
						}
					}
					else
					{
						Logger.i("网络断开");
						if(tag != null)
						{
							tag = null;
							netType = NET_NA;
							ctx.sendBroadcast(new Intent(B_NET_UNCONNECT));
						}
					}

				}
			}
			catch (Exception e1)
			{
				Logger.e("netReceiver=" + e1.toString());
			}
		}
	};

//	static public int getNetStatus(Context ctx)
//	{
//		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//		if (info != null && info.isAvailable() && info.isConnected())
//		{
//			if(info.getType() == 0) // info.getTypeName() == mobile
//			{
//				if(info.getSubtype() == 2) // info.getSubtypeName() == EDGE
//				{
//					return NET_2G;
//				}
//				else
//				{
//					// info.getSubtype() == 10; info.getSubtypeName() == HSPA
//					return NET_3G;
//				}
//			}
//			else if(info.getType() == 1) // info.getTypeName() == WIFI
//			{
//				return NET_WIFI;
//			}
//		}
//		return NET_NA;
//	}
	
	public static boolean getNetStatus(Context ctx)
	{
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isAvailable() && info.isConnected())
		{
			return true;
		}
		return false;
	}
	
	// 通用获取IP的方法（判断手机是否联网的方法）
	public static String getLocalIpAddress()
	{
		try
		{
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while(en.hasMoreElements())
			{
				NetworkInterface intf = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
				while(enumIpAddr.hasMoreElements())
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					
//					Logger.i("Loop:" + inetAddress.isLoopbackAddress());
//					Logger.i("AnyL:" + inetAddress.isAnyLocalAddress());
//					Logger.i("Link:" + inetAddress.isLinkLocalAddress());
					
					/** 
					Loop:true
					AnyL:false
					Link:false
					getHostAddress:::1%1
					
					Loop:true
					AnyL:false
					Link:false
					getHostAddress:127.0.0.1
					
					Loop:false
					AnyL:false
					Link:true
					getHostAddress:fe80::50cc:f8ff:fec7:42e3%p2p0
					
					Loop:false
					AnyL:false
					Link:true
					getHostAddress:fe80::52cc:f8ff:fec7:42e3%wlan0
					
					Loop:false
					AnyL:false
					Link:false
					getHostAddress:192.168.66.146*/

					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
					{
//						Logger.i("getCanonicalHostName:" + inetAddress.getCanonicalHostName());
//						Logger.i("getHostAddress:" + inetAddress.getHostAddress());
//						Logger.i("getHostName:" + inetAddress.getHostName());
						return inetAddress.getHostAddress();
					}
				}
			}
		}
		catch (SocketException e1)
		{
			Logger.e("getLocalIpAddress() " + e1.toString());
		}
		return null;
	}
	
	public int getNetType()
	{
		return netType;
	}

	public void destory()
	{
		try{ctx.unregisterReceiver(netReceiver);}
		catch (Exception e1){Logger.e("NetStatus() destory()=" + e1.toString());}
	}
}

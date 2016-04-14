package hz.dodo;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class SIMMng
{
	public interface Callback
	{
		public void onNetWorkChanged(final int netType);
		public void onSimStatusChanged(final int status);
		
		/*
		 * signalStrength
		 * 4: >= 12
		 * 3: >= 8
		 * 2: >= 5
		 * 1: 
		 * 0: == 99 || <= 2
		 */
		public void onSignalChanged(final int status, final int signalStrength);
		
		/*
		 * camd1xSignalStrength
		 * 4: >= -75
		 * 3: >= -85
		 * 2: >= -95
		 * 1: >= -100
		 * 0: 
		 */
		/*
		 * evdoSignalStrength
		 * 4: >= -65
		 * 3: >= -75
		 * 2: >= -90
		 * 1: >= -105
		 * 0: 
		 */
		public void onCdmaSignalChanged(final int status, final int camd1xSignalStrength, final int evdoSignalStrength);
	}

	// SIM卡安装卸载
	@SuppressWarnings ("unused")
	private final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	
	// SIM status
    public static final int SIM_VALID = 0; // 可用
    public static final int SIM_INVALID = 1; // 不可用
    public static final int SIM_EMERGENCY_ONLY = 2; // 仅限紧急呼叫
    public static final int SIM_OUT_OF_SERVICE = 3; // 不在服务区
    public static final int SIM_POWER_OFF = 4; // 断电
    
    // net status
	public static final int NET_NA = 0;
	public static final int NET_WIFI = 1;
	public static final int NET_2G = 2;
	public static final int NET_3G = 3;
	public static final int NET_4G = 4;
	
	Context ctx;
	Callback callback;
	TelephonyManager telm;
	
	String
		tag;

	int
		netType, // 网络类型
		simState, // SIM 卡状态
		simSignal, // GSM 信号强度
		simSignalCdma1x, // CDMA 1X 信号强度
		simSignalEvdo; // EVDO 信号强度

	public SIMMng(Context ctx, Callback callback)
	{
		this.ctx = ctx;
		this.callback = callback;
		
		netType = NET_NA;
		simState = SIM_INVALID;
		simSignal = 99;
		simSignalCdma1x = 99;
		simSignalEvdo = 99;
		tag = "";
		
		telm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		telm.listen(pl, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE);
		initNetWrokType();
		
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        ctx.registerReceiver(netReceiver, filter);
	}

	public void destory()
	{
		try
		{
			ctx.unregisterReceiver(netReceiver);
			telm.listen(pl, PhoneStateListener.LISTEN_NONE);
		}
		catch (Exception e1)
		{
			Logger.e("SIMMng() destory()=" + e1.toString());
		}
	}
	
	PhoneStateListener pl = new PhoneStateListener()
	{
		public void onCallForwardingIndicatorChanged(boolean cfi)
		{
			Logger.i("onCallForwardingIndicatorChanged() " + cfi);
		};
		public void onCallStateChanged(int state, String incomingNumber)
		{
			Logger.i("onCallStateChanged() state:" + state + ", incomingNumber:" + incomingNumber);
		};
		public void onCellInfoChanged(java.util.List<android.telephony.CellInfo> cellInfo)
		{
			Logger.i("onCellInfoChanged() " + cellInfo.toString());
		};
		public void onCellLocationChanged(android.telephony.CellLocation location)
		{
			Logger.i("onCellLocationChanged() " + location.toString());
		};
		public void onDataActivity(int direction)
		{
			Logger.i("onDataActivity() " + direction);
		};
		public void onDataConnectionStateChanged(int state)
		{
			Logger.i("onDataConnectionStateChanged() " + state);
		};
		public void onDataConnectionStateChanged(int state, int networkType)
		{
			Logger.i("onDataConnectionStateChanged() state:" + state + ", networkType:" + networkType);
		};
		public void onMessageWaitingIndicatorChanged(boolean mwi)
		{
			Logger.i("onMessageWaitingIndicatorChanged() " + mwi);
		};
		
		
		// 手机信号变动
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
			super.onSignalStrengthsChanged(signalStrength);
			initNetWrokType();
			
			if(signalStrength == null) return;
			
			/* 
			  signalStrength.isGsm()           是否GSM信号 2G or 3G  
			  signalStrength.getCdmaDbm();     联通3G 信号强度 
			  signalStrength.getCdmaEcio();    联通3G 载干比 
			  signalStrength.getEvdoDbm();     电信3G 信号强度 
			  signalStrength.getEvdoEcio();    电信3G 载干比 
			  signalStrength.getEvdoSnr();     电信3G 信噪比 
			  signalStrength.getGsmSignalStrength();  2G 信号强度 
			  signalStrength.getGsmBitErrorRate();    2G 误码率 
			 
			  载干比 ，它是指空中模拟电波中的信号与噪声的比值 
			*/
			
			//test = "GSM(2G/3G):" + signalStrength.isGsm();
			
			boolean isGsm = signalStrength.isGsm();

			Logger.i("SIM status:" + telm.getSimState() + ", isGsm: " + isGsm);
			switch(telm.getSimState())
			{
				case TelephonyManager.SIM_STATE_READY: // 良好
					Logger.i("SIM卡状态良好");
					
					if(isGsm)
					{
						// 有效值为0-31,无效99
						simSignal = signalStrength.getGsmSignalStrength();
						Logger.i("信号强度 GSM:" + simSignal);
					}
					else
					{
						simSignalCdma1x = signalStrength.getCdmaDbm();
						simSignalEvdo = signalStrength.getEvdoDbm();
						Logger.i("信号强度 CDMA1X:" + simSignalCdma1x + ", EVDO:" + simSignalEvdo);
					}
					
					break;
				case TelephonyManager.SIM_STATE_ABSENT: // 无SIM卡
				case TelephonyManager.SIM_STATE_UNKNOWN:
				case TelephonyManager.SIM_STATE_PIN_REQUIRED:
				case TelephonyManager.SIM_STATE_PUK_REQUIRED:
				case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
				default:
					Logger.i("SIM卡错误");
					simState = SIM_INVALID;
					break;
			}
			if(callback != null)
			{
				if(isGsm)
				{
					callback.onSignalChanged(simState, simSignal);
				}
				else
				{
					callback.onCdmaSignalChanged(simState, simSignalCdma1x, simSignalEvdo);
				}
			}
		}
		
		// 手机服务状态变动 
		public void onServiceStateChanged(android.telephony.ServiceState serviceState)
		{
			super.onServiceStateChanged(serviceState);
			if(serviceState == null) return;

			// 不知道为什么接收不到任何changed,哈哈,因为没有监听
			Logger.i("onServiceStateChanged() " + serviceState.getState());
			/* 
			  ServiceState.STATE_EMERGENCY_ONLY   仅限紧急呼叫 
			  ServiceState.STATE_IN_SERVICE       信号正常 
			  ServiceState.STATE_OUT_OF_SERVICE   不在服务区
			  ServiceState.STATE_POWER_OFF        断电 
			*/
			switch (serviceState.getState())
			{
				case ServiceState.STATE_EMERGENCY_ONLY: // 仅限紧急呼叫 
					Logger.d("仅限紧急呼叫");
					simState = SIM_EMERGENCY_ONLY;
					break;
				case ServiceState.STATE_IN_SERVICE: // 信号正常
					Logger.i("信号正常");
					simState = SIM_VALID;
					break;
				case ServiceState.STATE_OUT_OF_SERVICE: // 不在服务区
					Logger.d("不在服务区");
					simState = SIM_OUT_OF_SERVICE;
					break;
				case ServiceState.STATE_POWER_OFF: // 断电
					Logger.d("断电");
					simState = SIM_POWER_OFF;
					break;
				default:
					break;
			}
			
			if(callback != null) callback.onSimStatusChanged(simState);
		};
	};
	
	// sim卡是否可用 0可用,1不可用
	public int getSIMStatus()
	{
		return simState;
	}
	
	// 获取信号强度
	
	
	@Deprecated // 可用,但不是单纯返回
	public String getOperators()
	{
		String imsi = telm.getSubscriberId();
		if (imsi != null)
		{
			if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007"))
			{
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
				return "中国移动";
			}
			else if (imsi.startsWith("46001"))
			{
				// 中国联通
				return "中国联通";
			}
			else if (imsi.startsWith("46003"))
			{
				// 中国电信
				return "中国电信";
			}
		}
		return null;
	}
	
	public String getOperators2()
	{
		String operator = telm.getSimOperator();
		if (operator != null)
		{
			if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007"))
			{
				// 中国移动
				return "中国移动";
			}
			else if (operator.equals("46001"))
			{
				// 中国联通
				return "中国联通";
			}
			else if (operator.equals("46003"))
			{
				// 中国电信
				return "中国电信";
			}
		}
		return null;
	}
	
	@Deprecated // 不准确
	public String getOperators3()
	{
		return telm.getSimOperatorName();
	}
	
	// 返回设备的电话号码（MSISDN号码） READ_PHONE_STATE
	public String getLine1Number()
	{
		return telm.getLine1Number();
	}
	
	// 返回的IMEI / MEID的设备。 如果该设备是GSM设备然后IMEI号将被退回，如果该设备是一个CDMA设备然后MEID 将被退回 , READ_PHONE_STATE
	public String getDeviceId()
	{
		return telm.getDeviceId();
	}
	
	// 返回SIM卡的序列号
	public String getSimSerialNumber()
	{
		return telm.getSimSerialNumber();
	}
	
	/*功能 说明
	getCellLocation（） 返回的单元格位置的装置 ACCESS_COARSE_LOCATION或ACCESS_FINE_LOCATION
	getDeviceId（） 返回的IMEI / MEID的设备。 如果该设备是GSM设备然后IMEI号将被退回，如果该设备是一个CDMA设备然后MEID 将被退回 READ_PHONE_STATE
	getLine1Number（） 返回设备的电话号码（MSISDN号码） READ_PHONE_STATE
	getNetworkOperatorName（） 返回注册的网络运营商的名字
	getNetworkOperator（） 返回的MCC +跨国公司的注册网络运营商
	getNetworkCountryIso（） 返回注册的网络运营商的国家代码
	getSimCountryIso（） 返回SIM卡运营商的国家代码 READ_PHONE_STATE
	getSimOperator（） 返回SIM卡运营商的单个核细胞数+冶 READ_PHONE_STATE
	getSimOperatorName（） 返回SIM卡运营商的名字 READ_PHONE_STATE
	getSimSerialNumber（） 返回SIM卡的序列号 READ_PHONE_STATE
	
	getNetworkType（） 返回网络设备可用的类型。 这将是下列其中一个值：
	TelephonyManager.NETWORK_TYPE_UNKNOWN
	TelephonyManager.NETWORK_TYPE_GPRS
	TelephonyManager.NETWORK_TYPE_EDGE
	TelephonyManager.NETWORK_TYPE_UMTS READ_PHONE_STATE
	
	getPhoneType（） 返回设备的类型。
	这将是以下值之一：
	TelephonyManager.PHONE_TYPE_NONE
	TelephonyManager.PHONE_TYPE_GSM
	TelephonyManager.PHONE_TYPE_CDMA READ_PHONE_STATE
	getSubscriberId（） 返回用户识别码（的IMSI）的设备 READ_PHONE_STATE
	getNeighboringCellInfo（） 返回NeighboringCellInfo类代表名单相邻小区的信息，如果可用，否则将返回null ACCESS_COARSE_UPDATES*/
	
	public int[] getCellInfo()
	{
		try
		{
			String operator = telm.getSimOperator();
			if (operator != null)
			{
				int[] iArr = new int[2];
				
				if (operator.equals("46001") || operator.equals("46000") || operator.equals("46002") || operator.equals("46007"))
				{
			        // 中国移动和中国联通获取LAC、CID的方式    
			        GsmCellLocation location = (GsmCellLocation) telm.getCellLocation();
			        int lac = location.getLac();
			        int cid = location.getCid();
			        
			        iArr[0] = lac;
			        iArr[1] = cid;
				}
				else if (operator.equals("46003"))
				{
				     // 中国电信获取LAC、CID的方式  
			        CdmaCellLocation location = (CdmaCellLocation) telm.getCellLocation();
			        int lac = location.getNetworkId();
			        int cid = location.getBaseStationId();
			        cid /= 16;
					
			        iArr[0] = lac;
			        iArr[1] = cid;
				}
				
				return iArr;
			}
		}
		catch(Exception ext)
		{
			Logger.e("getCellInfo() " + ext.toString());
		}
		return null;
	}
	public List<int[]> getNeighboringCellInfo()
	{
		try
		{
	        // 获取邻区基站信息  
	        List<NeighboringCellInfo> ltInfos = telm.getNeighboringCellInfo();
	        if(ltInfos != null && ltInfos.size() > 0)
	        {
	        	List<int[]> lt = new ArrayList<int[]>();
	        	NeighboringCellInfo info;
	        	
	        	int i1 = 0;
	        	while(i1 < ltInfos.size())
	        	{
	        		if(null != (info = ltInfos.get(i1)))
	        		{
	        			int[] iArr = new int[3];
	        			iArr[0] = info.getLac(); // 取出当前邻区的LAC  
	        			iArr[1] = info.getCid(); // 取出当前邻区的CID  
	        			iArr[2] = info.getRssi(); // 获取邻区基站信号强度 
	        			
	        			lt.add(iArr);
	        		}
	        		++i1;
	        	}
		        
	        	return lt;
	        }
		}
		catch(Exception ext)
		{
			Logger.e("getNeighboringCellInfo() " + ext.toString());
		}
		return null;
	}

	// 以下是网络部分
	private BroadcastReceiver netReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				if (intent == null) return;
				
				String action = intent.getAction();
				if ((ConnectivityManager.CONNECTIVITY_ACTION).equals(action))
				{
					initNetWrokType();
				}
			}
			catch (Exception e1)
			{
				Logger.e("netReceiver=" + e1.toString());
			}
		}
	};
	
	public synchronized void initNetWrokType()
	{
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		
		if (info != null && info.isAvailable() && info.isConnected())
		{
			String newTag = info.getState() + info.getTypeName() + "";
			if (info.getState() == NetworkInfo.State.CONNECTED)
			{
				if (info.getTypeName().equals("WIFI"))
				{
					netType = NET_WIFI;
					newTag += "" + netType;
					
					if(tag == null || !newTag.equals(tag))
					{
						tag = newTag;
						if(callback != null) callback.onNetWorkChanged(netType);
					}
					
					Logger.i("网络已连接-WIFI");
				}
				else
				{
					TelephonyManager telmng = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
					if (telmng.getDataState() == TelephonyManager.DATA_CONNECTED)
					{
						Logger.i("net work type: " + telmng.getNetworkType());
						
						switch (telmng.getNetworkType())
						{
							case TelephonyManager.NETWORK_TYPE_GPRS:
							case TelephonyManager.NETWORK_TYPE_EDGE:
							case TelephonyManager.NETWORK_TYPE_CDMA:
							case TelephonyManager.NETWORK_TYPE_1xRTT:
							case TelephonyManager.NETWORK_TYPE_IDEN:
								Logger.i("网络已连接-2G");
								netType = NET_2G;
								break;
							case TelephonyManager.NETWORK_TYPE_EVDO_0:
							case TelephonyManager.NETWORK_TYPE_EVDO_A:
							case TelephonyManager.NETWORK_TYPE_EVDO_B:
							case TelephonyManager.NETWORK_TYPE_UMTS:
							case TelephonyManager.NETWORK_TYPE_HSDPA:
							case TelephonyManager.NETWORK_TYPE_HSPA:
							case TelephonyManager.NETWORK_TYPE_HSPAP:
							case TelephonyManager.NETWORK_TYPE_HSUPA:
							case TelephonyManager.NETWORK_TYPE_EHRPD:
								Logger.i("网络已连接-3G");
								netType = NET_3G;
								break;
							case TelephonyManager.NETWORK_TYPE_LTE:
								Logger.i("网络已连接-4G");
								netType = NET_4G;
								break;
							default:
								Logger.i("网络已连接-未知");
								netType = NET_NA;
								break;
						}
						
						newTag += "" + netType;
						if(tag == null || !newTag.equals(tag))
						{
							tag = newTag;
							if(callback != null) callback.onNetWorkChanged(netType);
						}
					}
				}
			}
		}
		else
		{
			Logger.i("网络断开");
			netType = NET_NA;
			if(tag != null)
			{
				tag = null;
				if(callback != null) callback.onNetWorkChanged(netType);
			}
		}
	}
	
	static public boolean getNetStatus(Context ctx)
	{
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isAvailable() && info.isConnected())
		{
			return true;
		}
		return false;
	}
	
	public int getNetType()
	{
		return netType;
	}
	
	// 区分SIM类型
	/*public  String getSimType()
	{
		if (SystemProperties.get("gsm.sim.card.type", "SIM").contentEquals("USIM"))
		{
			return "USIM";
		}
		else
		{
			return "SIM";
		}
	}*/
	// 是否有ICC卡：
	public boolean hasIccCard()
	{
		return ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).hasIccCard();
	}
	public int getNetworkType()
	{
		try
		{
			int nwt = NET_NA;
			switch (telm.getNetworkType())
			{
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					nwt = NET_2G;
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
					nwt = NET_3G;
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					nwt = NET_4G;
					break;
			}
			
			return nwt;
		}
		catch (Exception ext)
		{
			Logger.e("getNetworkType() " + ext.toString());
		}
		return NET_NA;
	}
}

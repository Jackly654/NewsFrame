package hz.dodo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class TstUtil implements Handler.Callback
{
	public static final int LONG = Toast.LENGTH_LONG;
	public static final int SHORT = Toast.LENGTH_SHORT;
	
	Context ctx;
	Handler handler;
	Toast toast;
	int duration;
	
	public TstUtil(Context ctx, final int duration)
	{
		this.ctx = ctx;
		this.duration = duration;
		handler = new Handler(Looper.getMainLooper(), this);
	}

	public void showTst(final String txt)
	{
		Message msg = handler.obtainMessage();
		msg.arg1 = duration;
		msg.obj = txt;
		handler.sendMessage(msg);
	}
	
	public void showTst(final String txt, final int duration)
	{
		Message msg = handler.obtainMessage();
		msg.arg1 = duration;
		msg.obj = txt;
		handler.sendMessage(msg);
	}
	
	public boolean handleMessage(Message msg)
	{
		if(toast == null)
		{
			toast = Toast.makeText(ctx, "", SHORT);
			Logger.i("Toast create()");
		}
//		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.setDuration(msg.arg1);
		toast.setText("" + msg.obj);
		toast.show();
		return true;
	}
}

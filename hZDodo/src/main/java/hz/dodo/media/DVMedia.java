package hz.dodo.media;

import hz.dodo.Logger;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DVMedia extends SurfaceView implements SurfaceHolder.Callback,
													OnBufferingUpdateListener,
													OnCompletionListener,
													OnErrorListener,
													OnSeekCompleteListener,
													OnVideoSizeChangedListener,
													OnPreparedListener
{
	public interface Callback
	{
		public void sendStatus(final int status, final String info, final String tag1, final String tag2);
	}

	public static final int INIT_ERROR = -2;
	public static final int ERROR = -1;
	public static final int IDLE = 0;
	public static final int PREPARE = 1;
	public static final int PREPARED = 2;
	public static final int BUFFING = 3;
	public static final int PAUSE = 4;
	public static final int PLAYING = 5;
	public static final int SEEKING = 6;
	public static final int SEEKOK = 7;
	public static final int COMPLETE = 8;
	public static final int TELEGRAM = 9;
	
	MediaPlayer player;
	Uri uri; // 视频路径
	PowerManager powerManager = null;
	WakeLock wakeLock = null;
	
	Callback callback;
	
	String tag1, tag2;
	int vw, vh, buf, pos, duration, status;
	
	protected DVMedia(Context ctx)
	{
		super(ctx);
	}
	
	@SuppressWarnings ("deprecation")
	public DVMedia(Context ctx, Callback callback)
	{
		super(ctx);
		try
		{
			this.callback = callback;
			
//			setFocusable(true);
//			setFocusableInTouchMode(true);
//			requestFocus(); // 请求焦点
//			setWillNotDraw(false); // 有该行才会调用onDraw
			powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE); // power
			wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
			
			initPlayer(getHolder());
			
			getHolder().addCallback(this); // 添加surface状态的方法回调
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 清除原生数据
		}
		catch(Exception e1)
		{
			Logger.e("DVMedia()=" + e1.toString());
		}
	}
	
	
	private void initPlayer(SurfaceHolder sh)
	{
		try
		{
			vw = 0;
			vh = 0;
			status = IDLE;
			
			player = new MediaPlayer();
			// player = MediaPlayer.create(context, uri);该方法不需要setDataSource和prepare
//			player.setDisplay(sh);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 监听
			player.setOnBufferingUpdateListener(this);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			player.setOnSeekCompleteListener(this);
			player.setOnVideoSizeChangedListener(this);
			
			player.setOnPreparedListener(this);
		}
		catch(Exception e1)
		{
			Logger.e("dv init=" + e1.toString());
			status = INIT_ERROR;
			callback("初始化播放器失败");
		}
	}
	
	protected void openVideo()
	{
		try
		{
			if (uri == null) return;
			
			if(status == IDLE)
			{
				reset();
				long t = System.currentTimeMillis();
				player.setDataSource(uri.toString());
				Logger.d("setPath:" + (System.currentTimeMillis()- t));
				// player = MediaPlayer.create(context, uri);该方法不需要setDataSource和prepare

				/*
				 * File file = new File(mMediaPath); FileInputStream fileInput = new
				 * FileInputStream(file); FileDescriptor fd = fileInput.getFD();
				 * player.setDataSource(fd, 8, file.length()-8);
				 * fileInput.close();
				 */

				status = PREPARE;
				player.prepareAsync(); 
				/*vw = player.getVideoWidth();
				vh = player.getVideoHeight();
				
				duration = player.getDuration();
				if(pos >= 0 && pos < duration)
				{
					seekTo(pos);
				}
				else
				{
					onSeekComplete(player);
				}*/
			}
			else if(status == PAUSE)
			{
				start();
			}else if(status == TELEGRAM){
				callback("电话挂断");
			}
		}
		catch (Exception e1)
		{
			Logger.i("openVideo()=" + e1.toString());
			status = ERROR;
			callback("打开资源崩溃");
		}
	}

	public void setVideoPath(final String path, final String tag1, final String tag2, final int pos)
	{
		status = IDLE;
		duration = 0;
		this.pos = pos;
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.uri = Uri.parse(path);
	}
	
	public void start()
	{
		if(status == PREPARED || status == PAUSE || status == SEEKOK)
		{
			player.start();
			status = PLAYING;
			callback("播放中");
		}
	}
	
	public void pause()
	{
		// 暂停
		if(status == PLAYING)
		{
			player.pause();
			status = PAUSE;
			callback("暂停");
		}
	}
	
	public void stop()
	{
//		player.stop();
//		reset(); // 不知道为什么,不能立即释放
		this.pos = 0;
		status = IDLE;
	}
	
	private void reset()
	{
		player.reset();
		status = IDLE;
	}
	
	public void destroy()
	{
		player.stop();
		player.reset();
		player.release();
		player = null;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public int getCurrentPosition()
	{
		return player.getCurrentPosition();
	}
	
	public void seekTo(int pos)
	{
		if(0 > pos || duration < pos) return;
		status = SEEKING;
		callback("快进中");
		player.seekTo(pos);
	}

	public int getMediaWidth()
	{
		return vw;
	}
	
	public int getMediaHeight()
	{
		return vh;
	}
	
	/**
	 * media callback
	 */
	public void onBufferingUpdate(MediaPlayer mp, int percent)
	{
		Logger.i("onBufferingUpdate()-" + percent);
		if(null != player && player.equals(mp))
		{
			buf = percent;
		}
	}
	
	public void onCompletion(MediaPlayer mp)
	{
		status = COMPLETE;
		callback("播放完毕");
		player.pause();
		reset(); // 不知道为什么,不能立即释放
		pos = 0;
		status = IDLE;
	}
	
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		status = ERROR;
		callback("播放出错 " + what + "-" + extra);
		reset();
		return true;
	}
	
	public void onSeekComplete(MediaPlayer mp)
	{
		Logger.i("onSeekComplete()"+pos);
		if(status == PREPARE)
		{
			status = PREPARED;
			Logger.i("Video Started");
			callback(vw + "_" + vh);
		}
		else if(status == SEEKING)
		{
			status = SEEKOK;
			callback(vw + "_" + vh);
		}
	}
	
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
	{
		vw = width;
		vh = height;
	}
	
	/**
	 * surface callback
	 */
	public void surfaceCreated(SurfaceHolder holder)
	{
		Logger.i("surfaceCreated()");
		
		try
		{
			if(!wakeLock.isHeld()) wakeLock.acquire(); // 请求常亮
		}
		catch(Exception e1)
		{
			Logger.e("wakeLock.acquire " + e1.toString());
		}
		try {
			player.setDisplay(holder);
			new Thread()
			{
				public void run()
				{
					openVideo();
				};
			}.start();
		} catch (Exception e) {
			Logger.d("surfaceCreated:"+e.toString());
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Logger.i("surfaceChanged() format："+format+",width:"+width+",height:"+height);
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Logger.i("surfaceDestroyed()");
		try
		{
			if(wakeLock.isHeld()) wakeLock.release(); // 注销常亮
		}
		catch(Exception e1)
		{
			Logger.e("wakeLock.release " + e1.toString());
		}
		if(status > PREPARED){
			pos = getCurrentPosition();
			pause();
		}
	}
	
	/**
	 * layout
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// 设置测量尺寸
		setMeasuredDimension(getDefaultSize(vw, widthMeasureSpec), getDefaultSize(vh, heightMeasureSpec));
	}
	
	private void callback(final String info)
	{
		if(callback != null) callback.sendStatus(status, info, tag1, tag2);
	}
	
	public void setStatusTele(){
		status = TELEGRAM;
	}
	public void setStatusPause(){
		status = PAUSE;
	}

	public int getStatus(){
		return status;
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		Logger.i("onPrepared");
		vw = player.getVideoWidth();
		vh = player.getVideoHeight();
		
		duration = player.getDuration();
		if(pos >= 0 && pos < duration)
		{
			seekTo(pos);
		}
		else
		{
			onSeekComplete(player);
		}
	}
}
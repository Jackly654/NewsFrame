package hz.dodo.media;

import hz.dodo.Logger;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

public class DMPlayer implements OnCompletionListener, OnErrorListener, OnSeekCompleteListener
{
	public interface Callback
	{
		public void sendStatus(final int status, final String info, final String tag1, final String tag2);
	}

	public static final int ERROR = -1;
	public static final int IDLE = 0;
	public static final int PREPARE = 1;
	public static final int PREPARED = 2;
	public static final int BUFFING = 3;
	public static final int PAUSE = 4;
	public static final int PLAYING = 5;
	public static final int SEEKING = 6;
	public static final int SEEKED = 7;
	public static final int COMPLETE = 8;

	MediaPlayer mp;
	Callback callback;
	int status, duration;
	String tag1, tag2;

	public DMPlayer(Callback callback)
	{
		this.callback = callback;
		initMp();
	}

	private void initMp()
	{
		try
		{
			status = IDLE;
			mp = new MediaPlayer();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setOnCompletionListener(this);
			mp.setOnErrorListener(this);
			mp.setOnSeekCompleteListener(this);
		}
		catch (Exception e1)
		{
			Logger.e("initMp=" + e1.toString());
		}
	}

	public void onCompletion(MediaPlayer mp)
	{
		// reset();
		status = COMPLETE;
		callback("播放完成");
	}

	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		duration = 1;
		status = ERROR;
		callback("播放出错");
		reset();
		return true;
	}

	public void onSeekComplete(MediaPlayer mp)
	{
		status = SEEKED;
		callback("seek ok");

		if (!isPlaying())
		{
			start();
		}
		else
		{
			status = PLAYING;
			callback("播放中");
		}
	}

	private void callback(final String info)
	{
		if (callback != null)
			callback.sendStatus(status, info, tag1, tag2);
	}

	public void setResPath(final int typeSource, final String path, final String tag1, final String tag2, Context context)
	{
		try
		{
			this.tag1 = tag1;
			this.tag2 = tag2;
			reset();
			if (typeSource == 1)
				mp.setDataSource(context, Uri.parse(path));
			else if (typeSource == 2)
			{
				File file = new File(path);
				FileInputStream fis = new FileInputStream(file);
				mp.setDataSource(fis.getFD());
				fis.close();
			}
			status = PREPARE;
			mp.prepare();
			status = PREPARED;
			duration = mp.getDuration();
			callback("已经准备好");
		}
		catch (Exception e1)
		{
			Logger.e("setRes=" + e1.toString());
			duration = 1;
			status = ERROR;
			callback("打开资源出错");
			reset();
		}
	}

	public void setResAsset(final Context ctx, final String assetname, final String tag1, final String tag2)
	{
		try
		{
			this.tag1 = tag1;
			this.tag2 = tag2;
			reset();

			AssetFileDescriptor afd = ctx.getResources().getAssets().openFd(assetname);
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

			status = PREPARE;
			mp.prepare();
			status = PREPARED;

			duration = getDuration();

			callback("已经准备好");
		}
		catch (Exception e1)
		{
			Logger.e("setRes=" + e1.toString());
			duration = 1;
			status = ERROR;
			callback("打开资源出错");
			reset();
		}
	}

	public void start()
	{
		if (status == PREPARED || status == PAUSE || status == SEEKED)
		{
			mp.start();
			status = PLAYING;
			callback("播放中");
		}
		else if (status == COMPLETE)
		{
			seekTo(0);
		}
	}

	public void pause()
	{
		if (status == PLAYING)
		{
			mp.pause();
			status = PAUSE;
			callback("暂停");
		}
	}

	public void recycle()
	{
		reset();
	}

	private void reset()
	{
		if (status != IDLE)
		{
			status = IDLE;
			mp.reset();
		}
	}

	public void seekDiff(int diff)
	{
		if (diff == 0)
			return;

		int target = getCurPos() + diff;
		if (target <= 0)
		{
			seekTo(0);
		}
		else if (target >= duration)
		{
			seekTo(duration);
		}
		else
		{
			seekTo(target);
		}
	}

	public void seekTo(int pos)
	{
		if (status != SEEKING && status != IDLE && status != ERROR)
		{
			if (0 > pos || duration < pos)
				return;
			status = SEEKING;
			mp.seekTo(pos);
		}
	}

	public boolean isPlaying()
	{
		return mp.isPlaying();
	}

	public int getCurPos()
	{
		if (status != ERROR && status != IDLE)
			return mp.getCurrentPosition();
		else
			return 0;
	}

	public int getDuration()
	{
		if (status != ERROR && status != IDLE)
		{
			if (duration <= 0)
				return duration = mp.getDuration();
		}
		return duration;
	}
}

package hz.dodo.media;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;

public class DSound
{
	// 触摸提示音
	public static void playTouchSound(Context ctx)
	{
		// 0:close , 1:open
//		int i = Settings.System.getInt(at.getContentResolver(),Settings.System.DTMF_TONE_WHEN_DIALING, 0);
//		int i = Settings.System.getInt(at.getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, 0);
		
		if(Settings.System.getInt(ctx.getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, 0) == 1)
		{
			AudioManager am = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
//			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1.0f);
			am.playSoundEffect(AudioManager.FX_KEY_CLICK);
		}
	}
}

package hz.dodo.data;

import hz.dodo.Logger;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class DCursorLoader
{
	@SuppressLint ("NewApi")
	public String cursorLoader(final Context ctx, final Uri uri)
	{
		if(ctx == null || uri == null) return null;
		
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Audio.Media.TITLE };
//			Uri.parse("content://media/internal/audio/media/124")
//			Uri.parse("content://media/external/audio/media/19617")
		    CursorLoader loader = new CursorLoader(ctx, uri, proj, null, null, null);
		    if(loader != null)
		    {
			    cursor = loader.loadInBackground();
			    if(cursor != null && cursor.moveToFirst())
				{
			    	return cursor.getString(0);
				}
		    }
		}
		catch(Exception ext)
		{
			Logger.e("getTitleByUri() uri=" + uri.toString() + " - " + ext.toString());
		}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}

	    return null;
	}
}

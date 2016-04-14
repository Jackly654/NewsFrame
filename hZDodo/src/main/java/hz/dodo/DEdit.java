package hz.dodo;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
//import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
//import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
//import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
//import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 注:外部调用时,用 addView(edit, lpedit) 不用直接用addView(edit)否则文字不垂直居中
 * 如果需要回车换行的话,请设置为 action_none
 */
public class DEdit extends EditText implements TextView.OnEditorActionListener, android.view.View.OnFocusChangeListener/*, android.view.MenuItem.OnMenuItemClickListener*/
{
	static public final int action_done = EditorInfo.IME_ACTION_DONE;
	static public final int action_go = EditorInfo.IME_ACTION_GO;
	static public final int action_next = EditorInfo.IME_ACTION_NEXT;
	static public final int action_none = EditorInfo.IME_ACTION_NONE;
//	public final int action_go = EditorInfo.IME_ACTION_PREVIOUS;
	static public final int action_search = EditorInfo.IME_ACTION_SEARCH;
	static public final int action_send = EditorInfo.IME_ACTION_SEND;
	static public final int action_unspecified = EditorInfo.IME_ACTION_UNSPECIFIED;
	
	public interface Callback
	{
		public void onEditorAction();
		public void onTouchBtn(boolean left, boolean right);
	}
	
	ImgMng im;
	InputMethodManager imm;
	Callback callback;
	
	int imeOptions,
		vw, vh;
	boolean bleft, bright, btouch,key_down;
	
	protected DEdit(Context ctx)
	{
		super(ctx);
	}
	protected DEdit(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	protected DEdit(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	public DEdit(Context ctx, Callback callback, int imeOptions, int vw, int vh)
	{
		super(ctx);
		this.callback = callback;
		this.imeOptions = imeOptions;
		this.vw = vw;
		this.vh = vh;
		
		im = ImgMng.getInstance(ctx);
		imm = ((InputMethodManager)ctx.getSystemService(Activity.INPUT_METHOD_SERVICE));
		
		setImeOptions(imeOptions);
		setGravity(Gravity.CENTER_VERTICAL);
		if(action_none != imeOptions)
		{
			setOnEditorActionListener(this);
		}
		
		setTextSize(16);
//		setSingleLine(true);
//		setLines(1);
		setHintTextColor(Color.GRAY);
		setHint("请输入关键字");
		setBackgroundResource(android.R.drawable.editbox_background);
		setTextColor(Color.BLACK);
//		setOnFocusChangeListener(this); // 光标改变的回调
//		setCursorVisible(true); // 可以显示光标,默认显示
		key_down = false;
	}
	
	@SuppressWarnings ("deprecation")
	public void setDrawable(int left, int right)
	{
		try
		{
			BitmapDrawable dleft = null, dright = null;
			Bitmap bm;
			if(left > 0)
			{
				if(null != (bm = im.getBmId(left)))
				{
					if(vh < bm.getHeight())
					{
						dleft = new BitmapDrawable(im.getBmId(left, vh));
					}
					else
					{
						dleft = new BitmapDrawable(bm);
					}
				}
			}
			if(right > 0)
			{
				if(null != (bm = im.getBmId(right)))
				{
					if(vh < bm.getHeight())
					{
						dright = new BitmapDrawable(im.getBmId(right, vh));
					}
					else
					{
						dright = new BitmapDrawable(bm);
					}
				}
			}
			
			bleft = dleft != null ? true : false;
			bright = dright != null ? true : false;
			
			setCompoundDrawablesWithIntrinsicBounds(dleft, null, dright, null);
			setCompoundDrawablePadding(10); // 文本和可绘制对象的间距
		}
		catch(Exception e1)
		{
			Logger.e("setDrawable()=" + e1.toString());
		}
	}
	
	// 改光标样式
	public void setCursorDrawable(int drawableId)
	{
//		drawableId:
//		1.可以是图片
//		2.可以是cursor.xml,如下
		
//		<?xml version="1.0" encoding="utf-8"?>
//		<shape xmlns:android="http://schemas.android.com/apk/res/android"
//		    android:shape="rectangle" >
//
//		    <solid android:color="#ff0000" />
//		    <size android:width="1dp" />
//
//		</shape>
		
		try
		{
			Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
		    f.setAccessible(true);
		    f.set(this, drawableId);
		}
		catch(Exception ext)
		{
			Logger.e("edit cursor " + ext.toString());
		}
	}

	int tdx, tdy, tux, tuy;
	public boolean onTouchEvent(MotionEvent event)
	{
		btouch = super.onTouchEvent(event);
		
		try
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					tdx = (int) event.getX();
					tdy = (int) event.getY();
					break;
				case MotionEvent.ACTION_UP:
					tux = (int) event.getX();
					tuy = (int) event.getY();
					
					if(tux < vh)
					{
						if(bleft)
						{
							Logger.i("点击了左侧的图片");
							try
							{
								if(callback != null) callback.onTouchBtn(true, false);
							}
							catch(Exception e1)
							{
								Logger.e("DEdit::callback.onTouchBtn(left)=" + e1.toString());
							}
						}
					}
					else if(tux > vw - vh)
					{
						if(bright)
						{
							Logger.i("点击了右侧的图片");
							try
							{
								if(callback != null) callback.onTouchBtn(false, true);
							}
							catch(Exception e1)
							{
								Logger.e("DEdit::callback.onTouchBtn(right)=" + e1.toString());
							}
						}
					}
					break;
			}
		}
		catch(Exception e1)
		{
			Logger.e("DEdit onTouchEvent()=" + e1.toString());
		}
		Logger.i("Edit onTouch return " + btouch);
		return btouch;
	}

	public boolean onEditorAction(TextView textview, int i, KeyEvent keyevent)
	{
		try
		{
			switch (i)
			{
				case action_done:
					break;
				case action_go:
					break;
				case action_next:
					break;
				case action_search:
					break;
				case action_send:
					break;
				case action_unspecified:
//					if(KeyEvent.KEYCODE_ENTER == keyevent.getKeyCode())
//					{
//						super.onEditorAction(i);
//					}
					break;
				case action_none:
					break;
				default:
					break;
			}
			
			if(callback != null && imeOptions == i) callback.onEditorAction();
		}
		catch(Exception e1)
		{
			Logger.e("DEdit::onEditorAction()=" + e1.toString());
		}
		return true;
	}
	
	public void setFocus()
	{
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
	}
	
	public void showInput(View view)
	{
		try
		{
			imm.showSoftInput(view, 0);
		}
		catch(Exception e1)
		{
			Logger.e("DEdit showInput() " + e1.toString());
		}
	}
	
	public void showInput()
	{
		try
		{
			Logger.i("show Input");
			imm.showSoftInput(this, InputMethodManager.RESULT_SHOWN);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
		catch(Exception e1)
		{
			Logger.e("showInput() " + e1.toString());
		}
	}
	
	public void dismissInput()
	{
		try
		{
//			if(isFocused())
			{
				Logger.i("dismiss Input");
				clearFocus();
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
				
//				if(imm.isActive()) // 似乎这个判断不好使
//				{
//					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
//				}
			}
		}
		catch(Exception e1)
		{
			Logger.e("DEdit::dismissInput()=" + e1.toString());
		}
	}
	
	public void dismissInputKeepFocus()
	{
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	public void onFocusChange(View v, boolean hasFocus)
	{
		Logger.i("焦点:" + hasFocus);
		if(hasFocus)
		{
//			setSelection(getText().toString().length());
		}
		else
		{
			dismissInput();
		}
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Logger.d("-onkeyDown");
		key_down = true;
		return super.onKeyDown(keyCode, event);
	}
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Logger.d("-onkeyUp");
		if(!key_down){
			key_down = false;
			return true;
		}
		key_down = false;
		return super.onKeyUp(keyCode, event);
	}
//	使用setSoftInputShownOnFocus 函数来设置显示光标 隐藏键盘，但此函数为隐藏的，用下面java放射
//	try {
//	Class cls = EditText.class;
//	Method setSoftInputShownOnFocus;
//	setSoftInputShownOnFocus = cls.getMethod("setSoftInputShownOnFocus", // setShowSoftInputOnFocus
//	boolean.class);
//	setSoftInputShownOnFocus.setAccessible(true);
//	setSoftInputShownOnFocus.invoke(mPasswordEntry, false);
//	} catch (Exception e) {
//	e.printStackTrace();
//	}
	/*protected void onCreateContextMenu(ContextMenu menu)
	{
		super.onCreateContextMenu(menu);
		Logger.d("EditText弹出菜单");
	}
	public boolean onMenuItemClick(MenuItem item)
	{
		Logger.d("onMenuItemClick() ");
		return false;
	}
	public boolean onTextContextMenuItem(int id)
	{
		Logger.d("onTextContextMenuItem() ");
		return super.onTextContextMenuItem(id);
	}
	public WindowInsets onApplyWindowInsets(WindowInsets insets)
	{
		Logger.d("onApplyWindowInsets()");
		return super.onApplyWindowInsets(insets);
	}
	public void onBeginBatchEdit()
	{
		Logger.d("onBeginBatchEdit()");
		super.onBeginBatchEdit();
	}
	public InputConnection onCreateInputConnection(EditorInfo outAttrs)
	{
		Logger.d("InputConnection()");
		return super.onCreateInputConnection(outAttrs);
	}
	public void onEndBatchEdit()
	{
		Logger.d("onEndBatchEdit()");
		super.onEndBatchEdit();
	}
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		Logger.d("onLayout()");
		super.onLayout(changed, left, top, right, bottom);
	}
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event)
	{
		Logger.d("onKeyMultiple()");
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}*/
}

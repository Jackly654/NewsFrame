package hz.dodo.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import hz.dodo.Logger;

@SuppressWarnings ("deprecation")
public class FBrowser extends WebView
{
	public interface Callback
	{
		public void onPageFinished(final boolean canGoBack);
		public void onProgressChanged(int newProgress);
		public void onGoBack();
	}

	Context
		ctx;
	Callback
		callback;

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi", "ClickableViewAccessibility" })
	public FBrowser(final Context ctx, final Callback callback, int vw, int vh)
	{
		super(ctx);
		setLayoutParams(new android.view.ViewGroup.LayoutParams(vw, vh));
		this.callback = callback;

		getSettings().setJavaScriptEnabled(true); // js
		getSettings().setBlockNetworkImage(false); // 去除图片

		// 网页的放大缩小
//		getSettings().setBuiltInZoomControls(true);
//		getSettings().setSupportZoom(true);
		
		//优先使用缓存：
		getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // //不使用缓存 WebSettings.LOAD_NO_CACHE
        
		// 取消滚动条
//		setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
		
		setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if(url.indexOf("tel:") < 0)
				{
					view.loadUrl(url); //页面上有数字会导致连接电话
				}
		        return true;
			}
			
			public void onLoadResource(WebView view, String url)
			{
				super.onLoadResource(view, url);
			}
			
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				Logger.i("onPageStarted()" + (favicon == null ? "null" : "favicon"));
				super.onPageStarted(view, url, favicon);
			}
			
			public void onPageFinished(WebView view, String url)
			{
				Logger.i("onPageFinished()");
				super.onPageFinished(view, url);
				if(callback != null) callback.onPageFinished(canGoBack());
			}
			
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				super.onReceivedError(view, errorCode, description, failingUrl);
				Logger.i("onReceivedError()");
			}
			
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
			{
				super.onReceivedSslError(view, handler, error);
			}
			
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm)
			{
				Logger.i("onReceivedHttpAuthRequest()");
				super.onReceivedHttpAuthRequest(view, handler, host, realm);
			}
		});
		
		setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView view, int newProgress)
			{
				super.onProgressChanged(view, newProgress);
				if(callback != null) callback.onProgressChanged(newProgress);
			}
			
			public void onReceivedIcon(WebView view, Bitmap icon)
			{
				super.onReceivedIcon(view, icon);
			}
		});
		
		setDownloadListener(new DownloadListener()
		{
			public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength)
			{
				Logger.d("onDownloadStart() url:" + url + ", userAgent:" + userAgent + ", contentDisposition:" + contentDisposition + ", mimetype:" + mimetype + ", contentLength:" + contentLength);
			}
		});
		
		setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				Logger.d("onLongClick()");
				return false;
			}
		});
		
		setOnTouchListener(new OnTouchListener()
		{
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event)
			{
				return false;
			}
		});
		
		setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Logger.d("onClick()");
			}
		});
		
		setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
			{
				Logger.d("onCreateContextMenu()");
				
				HitTestResult result = ((WebView) v).getHitTestResult();
				if (null == result) return;

				switch (result.getType())
				{
					case HitTestResult.UNKNOWN_TYPE:
						menu.add("UNKNOWN_TYPE");
						break;
					case HitTestResult.EDIT_TEXT_TYPE:
						// let TextViewhandles context menu
						menu.add("EDIT_TEXT_TYPE");
						break;
					case HitTestResult.PHONE_TYPE:
						// 处理拨号
						menu.add("PHONE_TYPE");
						break;
					case HitTestResult.EMAIL_TYPE:
						menu.add("Email");
						// 处理Email
						break;
					case HitTestResult.GEO_TYPE:
						menu.add("GEO_TYPE");
						break;
					case HitTestResult.SRC_ANCHOR_TYPE:
						// 超链接
						menu.add("超链接");
						break;
					case HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
					case HitTestResult.IMAGE_TYPE:
						// 处理长按图片的菜单项
						menu.add("图片");
						break;
					default:
						break;
				}
			}
		});
		
		if(android.os.Build.VERSION.SDK_INT >= 11)
		{
			setOnDragListener(new OnDragListener()
			{
				public boolean onDrag(View v, DragEvent event)
				{
					Logger.d("onDrag() onDrag():");
					return false;
				}
			});
		}
		
		setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener()
		{
			public void onSystemUiVisibilityChange(int visibility)
			{
				Logger.d("onSystemUiVisibilityChange() visibility:" + visibility);
			}
		});
	}
	public void onDestroy()
	{
		callback = null;
	}
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		super.dispatchKeyEvent(event);
		if(event.getAction() != KeyEvent.ACTION_UP) return true;
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			if(!canGoBack())
			{
				if(callback != null)
				{
					callback.onGoBack();
				}
			}
			else
			{
				goBack();
			}
		}
		return false;
	}
	public void setUrl(String httpurl)
	{
		try
		{
			if(httpurl == null || httpurl.length() <= 0 || httpurl.equals("" + getUrl())) return;
			setFocus();
			loadUrl(httpurl);
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

	private void setFocus()
	{
		setFocusable(true);
		setFocusableInTouchMode(true); // 设置焦点
		requestFocusFromTouch();
		requestFocus(View.FOCUS_DOWN);
		requestFocus(); // 请求焦点
	}

	@SuppressWarnings ("unused")
	private void clearCache()
	{
		clearCache(true);
	}
	@SuppressWarnings ("unused")
	private void celarHistory()
	{
		clearHistory();
	}
	
	// 是否已经到底部
	@SuppressWarnings ({ "unused" })
	private boolean isBtm()
	{
		if(getContentHeight()*getScale() == (getHeight()+getScrollY()))
		{
	       //已经处于底端
			return true;
		}
		return false;
	}
	public void reRefresh()
	{
		reload();
	}

	// 关于cookie
	protected void clearCookie()
	{
		CookieSyncManager.createInstance(ctx);
		CookieSyncManager.getInstance().startSync(); 
		CookieManager.getInstance().removeSessionCookie();
	}
}

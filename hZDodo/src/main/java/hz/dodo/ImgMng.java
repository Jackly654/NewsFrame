package hz.dodo;

import hz.dodo.data.Empty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;

public class ImgMng
{
	private final String ID_ = "ID_";
	private final String ID_W_ = "ID_W_";
	private final String RAW_ = "RAW_";
	private final String RAW_W_ = "RAW_W_";
	private final String AS_ = "AS_";
	private final String AS_W_ = "AS_W_";
	private final String PATH_ = "PATH_";
	private final String PATH_W_ = "PATH_W_";
	private final String PATH_W_H_ = "PATH_W_H_";
	private final String ROUND_W_ = "ROUND_W_";
	private final String STREAM = "STREAM";
	
	private Context ctx;

	private static ImgMng im;
	private HashMap<String, FSoftReference<String, Bitmap>> hmBm;
	ReferenceQueue<Bitmap> rq;
//	private Matrix matrix;
//	private BitmapFactory.Options opts;
	
	class FSoftReference<K, V> extends SoftReference<V>
	{
		public
		K
			key;

		public FSoftReference(K key, V v, ReferenceQueue<? super V> q)
		{
			super(v, q);
			this.key = key;
		}
	}

	static public ImgMng getInstance(Context ctx)
	{
		if (im == null)
			im = new ImgMng(ctx);
		return im;
	}

	private ImgMng(Context ctx)
	{
		this.ctx = ctx;
		
		rq = new ReferenceQueue<Bitmap>();
		hmBm = new HashMap<String, FSoftReference<String, Bitmap>>();
//		matrix = new Matrix();
//		opts = new BitmapFactory.Options();
//		opts.inJustDecodeBounds = true;
	}
	private Canvas getCvs(final Bitmap bm)
	{
		Canvas cvs = new Canvas(bm);
		cvs.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		return cvs;
	}
	private Paint getPaint()
	{
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setDither(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		return paint;
	}
	private void put(final String key, final Bitmap bm)
	{
		try
		{
			if(Empty.isEmpty(key) || bm == null) return;
			hmBm.put(key, new FSoftReference<String, Bitmap>(key, bm, rq));
			poll();
		}
		catch(OutOfMemoryError oom)
		{
			Logger.w("ImgMng put " + oom.toString());
		}
		catch(Exception ext)
		{
			Logger.e("ImgMng put " + ext.toString());
		}
	}
	private Bitmap get(final String key)
	{
		try
		{
			if(Empty.isEmpty(key)) return null;
			FSoftReference<String, Bitmap> fsr = hmBm.get(key);
			if(fsr != null)
			{
				Bitmap bm = fsr.get();
				if(bm != null && !bm.isRecycled())
				{
					return bm;
				}
			}
		}
		catch(Exception ext)
		{
			Logger.e("ImgMng get " + ext.toString());
		}
		return null;
	}
	private void poll()
	{
		try
		{
			if(rq != null)
			{
				FSoftReference<?, ?> fsr = (FSoftReference<?, ?>) rq.poll();
				while(fsr != null)
				{
					Bitmap bm = (Bitmap) fsr.get();
					if(bm == null || bm.isRecycled())
					{
						hmBm.remove(fsr.key);
					}
					fsr = (FSoftReference<?, ?>) rq.poll();
				}
			}
		}
		catch(Exception ext)
		{
			Logger.e("ImgMng poll " + ext.toString());
		}
	}
	
	public Bitmap getBmPath(final String abspath)
	{
		Bitmap bm;
		if (null != (bm = get(PATH_ + abspath)))
		{
			return bm;
		}
		if (null != (bm = getPath(abspath)))
		{
			put(PATH_ + abspath, bm);
			return bm;
		}
		return null;
	}
	public Bitmap getBmPath(final String abspath, final int w)
	{
		if (w <= 0) return null;
		
		Bitmap bmTmp;
		if (null != (bmTmp = get(PATH_W_ + abspath)))
		{
			return bmTmp;
		}
		if (null != (bmTmp = getPath(abspath)))
		{
			Bitmap bm = scale(bmTmp, w);
			if(bm != null)
			{
				put(PATH_W_ + abspath, bm);
				return bm;
			}
		}
		return null;
	}
	
	public Bitmap getBmPath(final String abspath, final int maxw, final int maxh)
	{
		if(maxw <= 0 || maxh <= 0) return null;
		
		Bitmap bmTmp;
		if(null != (bmTmp = get(PATH_W_H_ + abspath)))
		{
			return bmTmp;
		}
		if (null != (bmTmp = getPath(abspath)))
		{
			Bitmap bm = scaleProportion(bmTmp, maxw, maxh);
			if(bm != null)
			{
				put(PATH_W_H_ + abspath, bm);
				return bm;
			}
		}
		return null;
	}
	@Deprecated
	public Bitmap getBmPath(final String abspath, final Config config, final int maxWidth, final int maxHeight)
	{
		if (maxWidth <= 0 || maxHeight <= 0)
			return null;
		try
		{
			Bitmap bm;
			if (null != (bm = get("abspath_width_" + abspath)))
			{
				return bm;
			}
			if (null != (bm = getPath(abspath)) && bm.getWidth() > 0)
			{
				float
					imgw = bm.getWidth(),
					imgh = bm.getHeight(),
					neww,
					newh;
				if(imgw > 0 && imgh > 0)
				{
					if(imgw < maxWidth && imgh < maxHeight)
					{
						neww = imgw;
						newh = imgh;
					}
					else
					{
						float
							imgscale = imgh/imgw,
							maxscale = (maxHeight*1.0f)/(maxWidth*1.0f);
						if(imgscale > maxscale) // 高度很高
						{
							newh = maxHeight;
							neww = newh*imgw/imgh;
						}
						else // 宽度很宽
						{
							neww = maxWidth;
							newh = neww*imgh/imgw;
						}
					}
					
					Bitmap bitmap = Bitmap.createBitmap((int)neww, (int)newh, bm.getConfig());
					if(bitmap != null)
					{
						Canvas cvs = getCvs(bitmap);
						cvs.drawBitmap(bm, null, new Rect(0, 0, (int)neww, (int)newh), null);
						bm.recycle();
						bm = null;
		
						put("abspath_width_" + abspath, bitmap);
		
						return bitmap;
					}
				}
			}
		}
		catch (OutOfMemoryError oom)
		{
//			if (ltKey.size() > 0)
//			{
//				hmBm.remove(ltKey.remove(0));
//			}
			Logger.w("getBmPath(abspath, maxWidth, maxHeight)=" + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("getBmPath(abspath, maxWidth, maxHeight)=" + e1.toString());
		}
		return null;
	}

	/** 
	 * 读取图片属性：旋转的角度 
	 * @param abspath 图片绝对路径 
	 * @param targetDegrees 目标角度
	 */
	@Deprecated
	public Bitmap getBmPath(final String abspath, final Config config, final int maxWidth, final int maxHeight, final int targetDegrees)
	{
		if (abspath == null || maxWidth <= 0 || maxHeight <= 0)
			return null;
		try
		{
			Bitmap bm;
			if (null != (bm = get("abspath_degrees_" + abspath)))
			{
//				if (ltKey.remove("abspath_degrees_" + abspath))
//				{
//					ltKey.add("abspath_degrees_" + abspath);
//				}
				return bm;
			}
			
			int degrees = targetDegrees - getBmDegrees(abspath);

			if(degrees == 0)
			{
//				bm = getBmPath(abspath, config, maxWidth, maxHeight);
				
				return bm;
			}
			
			if (null != (bm = getPath(abspath)) && bm.getWidth() > 0)
			{
				Matrix matrix = new Matrix();
				matrix.setRotate(degrees, bm.getWidth() / 2, bm.getHeight() / 2);

				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);

				float scaleW = (float)maxWidth / bm.getWidth(), scaleH = (float)maxHeight / bm.getHeight();

				int newWidth = 0, newHeight = 0;

				if(scaleW >= scaleH && scaleW < 1)
				{
					newHeight = maxHeight;
					newWidth = maxHeight * bm.getWidth() / bm.getHeight();
				}
				else if(scaleH > scaleW && scaleH < 1)
				{
					newWidth = maxWidth;
					newHeight = maxWidth * bm.getHeight() / bm.getWidth();
				}
				else
				{
					newWidth = bm.getWidth();
					newHeight = bm.getHeight();
				}

				Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, config);
				Canvas cvs = new Canvas();
				cvs.setBitmap(bitmap);
				Rect rect = new Rect(0, 0, newWidth, newHeight);
				cvs.drawBitmap(bm, null, rect, null);
				bm.recycle();
				bm = null;

				put("abspath_degrees_" + abspath, bitmap);
				
				return bitmap;
			}
			
		}
		catch (OutOfMemoryError oom)
		{
			Logger.w("getBmPath(abspath, degrees)=" + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("getBmPath(abspath, degrees)=" + e1.toString());
		}
		return null;
	}
	
	/** 
	 * 读取图片属性：旋转的角度 
	 * @param path 图片绝对路径 
	 * @return degree旋转的角度 
	 */
	@SuppressLint("NewApi")
	public int getBmDegrees(String path)
	{
		if(path == null || path.isEmpty()) return 0;
		
		int degree = 0;
	    try {
	        ExifInterface exifInterface = new ExifInterface(path);
	        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
	        switch (orientation) {
	        case ExifInterface.ORIENTATION_ROTATE_90:
	            degree = -90;
	            break;
	        case ExifInterface.ORIENTATION_ROTATE_180:
	            degree = -180;
	            break;
	        case ExifInterface.ORIENTATION_ROTATE_270:
	            degree = -270;
	            break;
	        }
	    } 
	    catch (IOException e) 
	    {
	    	Logger.e("getBmDegrees(path)=" + e.toString());
	    }
	    return degree;
	}

	public Bitmap getBmStream(InputStream in, String key, int w, int h)
	{
		Bitmap bm;
		if (null != (bm = get(STREAM + key)))
		{
			return bm;
		}
		try
		{
			if (null != (bm = BitmapFactory.decodeStream(in)))
			{
				/*Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_4444);
				if(bitmap != null)
				{
					Canvas cvs = getCvs(bitmap);
					cvs.drawBitmap(bm, null, new Rect(0, 0, w, h), null);
					bm.recycle();
					bm = null;
					
					bm = scale(bitmap, w, h);

					put(STREAM + key, bitmap);
					return bitmap;
				}*/
				
				Bitmap bitmap = scale(bm, w, h);
				if(bitmap != null)
				{
					put(STREAM + key, bitmap);
					return bitmap;
				}
			}
		}
		catch (OutOfMemoryError oom)
		{
			Logger.w("getBmStream() " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("getBmStream() " + e1.toString());
		}
		return null;
	}

	public Bitmap getBmAsset(final String assetname, final int width)
	{
		if (width <= 0) return null;
			
		Bitmap bmTmp;
		if (null != (bmTmp = get(AS_W_ + assetname)))
		{
			return bmTmp;
		}

		if (null != (bmTmp = getAs(assetname)))
		{
			Bitmap bm = scale(bmTmp, width);
			if(bm != null)
			{
				put(AS_W_ + assetname, bm);
				return bm;
			}
		}
		return null;
	}

	public Bitmap getBmAsset(final String assetname)
	{
		Bitmap bmTmp;
		if (null != (bmTmp = get(AS_ + assetname)))
		{
			return bmTmp;
		}

		if (null != (bmTmp = getAs(assetname)))
		{
			put(AS_ + assetname, bmTmp);
			return bmTmp;
		}
		return null;
	}

	public Bitmap getBmRaw(final int rawid, final int width)
	{
		if (width <= 0) return null;
			
		Bitmap bmTmp;
		if (null != (bmTmp = get(RAW_W_ + rawid)))
		{
			return bmTmp;
		}

		if (null != (bmTmp = getRaw(rawid)))
		{
			Bitmap bm = scale(bmTmp, width);
			if(bm != null)
			{
				put(RAW_W_ + rawid, bm);
				return bm;
			}
		}
		return null;
	}

	public Bitmap getBmRaw(final int rawid)
	{
		Bitmap bmTmp;
		if (null != (bmTmp = get(RAW_ + rawid)))
		{
			return bmTmp;
		}

		if (null != (bmTmp = getRaw(rawid)))
		{
			put(RAW_ + rawid, bmTmp);
			return bmTmp;
		}
		return null;
	}

	public Bitmap getBmId(final int id, final int width)
	{
		if (width <= 0) return null;
			
		Bitmap bmTmp;
		if (null != (bmTmp = get(ID_W_ + id)))
		{
			return bmTmp;
		}

		if (null != (bmTmp = getId(id)))
		{
			Bitmap bm = scale(bmTmp, width);
			if(bm != null)
			{
				put(ID_W_ + id, bm);
				return bm;
			}
		}
		return null;
	}

	public Bitmap getBmId(final int id)
	{
		Bitmap bm;
		if (null != (bm = get(ID_ + id)))
		{
			return bm;
		}
		if (null != (bm = getId(id)))
		{
			put(ID_ + id, bm);
			return bm;
		}
		return null;
	}

	@SuppressWarnings ("deprecation")
	public Bitmap getWallPaper(int fw, int fh)
	{
		Bitmap bm;
		if (null != (bm = get("WallPaper")))
		{
			return bm;
		}

		try
		{
			WallpaperInfo wallpaperInfo = WallpaperManager.getInstance(ctx).getWallpaperInfo();

			if (wallpaperInfo == null)
			{// "/data/data/com.android.settings/files/wallpaper";

				bm = ((BitmapDrawable) (ctx.getWallpaper())).getBitmap();
				if (bm != null)
				{
					Bitmap bitmap = Bitmap.createBitmap(fw, fh, Bitmap.Config.RGB_565);
					if(bitmap != null)
					{
						Canvas cvs = getCvs(bitmap);

						Rect rect = new Rect(0, 0, fw, fh);
						Rect rect1 = new Rect();
						if (bm.getWidth() > fw)
						{
							rect1.left = (int) ( (bm.getWidth() - fw) * 0.5f);
							rect1.right = rect1.left + fw;
						}
						else
						{
							rect1.left = 0;
							rect1.right = bm.getWidth();
						}

						rect1.top = 0;
						if (bm.getHeight() > fh)
						{
							rect1.bottom = fh;
						}
						else
						{
							rect1.bottom = bm.getHeight();
						}

						cvs.drawBitmap(bm, rect1, rect, null);

						put("WallPaper", bitmap);

						return bitmap;
					}
				}
			}
		}
		catch (OutOfMemoryError oom)
		{
			Logger.w("getWallPaper() " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("getWallPaper() " + e1.toString());
		}
		return null;
	}
	// width == 0 (不指定宽度,加载原图) , roundPixels == 0 (不指定圆角,按正圆返回)
	public Bitmap getBmRound(final String abspath, final int width, int roundPixels)
	{
		Bitmap
			bmTmp,
			bmRound;

		if (null != (bmTmp = get(ROUND_W_ + abspath)))
		{
			return bmTmp;
		}

		bmTmp = width == 0 ? getBmPath(abspath) : getBmPath(abspath, width);
		if(bmTmp != null)
		{
			if(null != (bmRound = getRoundCornerImage(bmTmp, roundPixels == 0 ? bmTmp.getWidth()/2 : roundPixels)))
			{
				put(ROUND_W_ + abspath, bmRound);
				return bmRound;
			}
		}
		return null;
	}
	// 圆角图片
	public Bitmap getRoundCornerImage(Bitmap bm, int roundPixels)
	{
		try
		{
			if(bm == null) return null;
			roundPixels = Math.abs(roundPixels);

			Config cfg = bm.getConfig();
			if(cfg == null)
			{
				cfg = Config.ARGB_4444;
			}
			Bitmap rstImg = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), cfg);
			if(rstImg != null)
			{
				Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
				RectF rectF = new RectF(rect);
				
				Canvas cvs = getCvs(rstImg);
				Paint paint = getPaint();
				
				paint.setXfermode(null);
				cvs.drawRoundRect(rectF, roundPixels, roundPixels, paint);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); // 设置相交模式
				cvs.drawBitmap(bm, null, rect, paint); // 把图片画到矩形去
				return rstImg;
			}
		}
		catch(Exception e1)
		{
			Logger.e("getRoundCornerImage() " + e1.toString());
		}
		return null;
	}

	// 倒影图片
	public Bitmap getReflectedImage(Bitmap bm)  
	{
		try
		{
			if(bm == null) return null;
			int width = bm.getWidth();
			int height = bm.getHeight();
			Matrix matrix = new Matrix();
			// 实现图片翻转90度
			matrix.preScale(1, -1);
			// 创建倒影图片（是原始图片的一半大小）
			Bitmap rstBm = Bitmap.createBitmap(bm, 0, height / 2, width, height / 2, matrix, false);
			// 创建总图片（原图片 + 倒影图片）
			Bitmap rstImg = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);
			
			if(rstBm != null && rstImg != null)
			{
				// 创建画布
				Canvas cvs = getCvs(rstImg);
				cvs.drawBitmap(bm, 0, 0, null);
				// 把倒影图片画到画布上
				cvs.drawBitmap(rstBm, 0, height + 1, null);
				Paint shaderPaint = new Paint();
				shaderPaint.setAntiAlias(true);
				// 创建线性渐变LinearGradient对象
				LinearGradient shader = new LinearGradient(0, bm.getHeight(), 0, rstImg.getHeight() + 1, 0x70ffffff, 0x00ffffff, TileMode.MIRROR);
				shaderPaint.setShader(shader);
				shaderPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
				// 画布画出反转图片大小区域，然后把渐变效果加到其中，就出现了图片的倒影效果。
				cvs.drawRect(0, height + 1, width, rstImg.getHeight(), shaderPaint);
				return rstImg;
			}
		}
		catch(Exception e1)
		{
			Logger.e("getReflectedImage() " + e1.toString());
		}
		return null;
	}

	// mask图
	public Bitmap getMask(final Bitmap src, final Bitmap mask)
	{
		try
		{
			if(src == null || mask == null) return null;
			//获取遮罩层图片
			Rect rect = new Rect(0, 0, mask.getWidth(), mask.getHeight());
			Bitmap result = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_8888);
			if(result != null)
			{
				//将遮罩层的图片放到画布中
				Canvas cvs = getCvs(result);
				Paint paint = getPaint();

				cvs.drawBitmap(src, null, rect, null);
				cvs.drawBitmap(mask, 0, 0, paint);

				return result;
			}
		}
		catch(Exception e1)
		{
			Logger.e("getMask() " + e1.toString());
		}
		return null;
	}
	public Bitmap getMask(final Bitmap src, final Bitmap mask, final Rect rect)
	{
		try
		{
			if(src == null || mask == null || rect == null) return null;
			//获取遮罩层图片
			Bitmap result = Bitmap.createBitmap(rect.width(), rect.height(), Config.ARGB_8888);
			if(result != null)
			{
				//将遮罩层的图片放到画布中
				Canvas cvs = getCvs(result);
				Paint paint = getPaint();

				cvs.drawBitmap(src, null, rect, null);
				cvs.drawBitmap(mask, null, rect, paint);
				paint.setXfermode(null);

				return result;
			}
		}
		catch(Exception e1)
		{
			Logger.e("getMask() " + e1.toString());
		}
		return null;
	}

	// 获取图片的主色调
	public int getMainColor(final Bitmap bitmap)
	{
		if(bitmap == null) return 0;
		int key = 0,
				count = 0,
				pixelColor,
				height = bitmap.getHeight(),
				width = bitmap.getWidth();

		HashMap<String, Integer> ht = new HashMap<String, Integer>();

		//		int A, R, G, B;

		for (int h = 0; h < height; h++)
		{
			for (int w = 0; w < width; w++)
			{
				pixelColor = bitmap.getPixel(w, h);

				//				A = Color.alpha(pixelColor);
				//                R = Color.red(pixelColor);
				//                G = Color.green(pixelColor);
				//                B = Color.blue(pixelColor);
				//                Logger.i(", a:" + A + " r:" + R + " g:" + G + " b:" + B);

				if(Color.alpha(pixelColor) > 0) // 记录不透明的部分
				{
					Integer intg = ht.get("" + pixelColor);
					if(null == intg)
					{
						intg = Integer.valueOf(0);
					}
					ht.put("" + pixelColor, intg.intValue() + 1);

					if(intg.intValue() + 1 > count)
					{
						count = intg.intValue() + 1;
						key = pixelColor;
					}
				}
			}
		}
		return key;
	}

	public Bitmap drawable2Bitmap(final Drawable drawable)
	{
		try
		{
			if(drawable == null) return null;
			return ((BitmapDrawable)drawable).getBitmap();
		}
		catch(Exception exc)
		{
			Logger.e("drawable2Bitmap() " + exc.toString());
		}
		return null;
	}

	public Drawable bitmap2Drawable(final Bitmap bm)
	{
		try
		{
			if(bm == null) return null;
			return new BitmapDrawable(ctx.getResources(), bm);
		}
		catch(Exception exc)
		{
			Logger.e("bitmap2Drawable() " + exc.toString());
		}
		return null;
	}

	private Bitmap getAs(final String assetname)
	{
		try
		{
			return BitmapFactory.decodeStream(ctx.getAssets().open(assetname));
		}
		catch(OutOfMemoryError oom)
		{
			Logger.e("get bm as " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.w("get bm as " + e1.toString());
		}
		return null;
	}

	private Bitmap getRaw(final int rawid)
	{
		try
		{
			return BitmapFactory.decodeStream(ctx.getResources().openRawResource(rawid));
		}
		catch(OutOfMemoryError oom)
		{
			Logger.e("get bm raw " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("get bm raw " + e1.toString());
		}
		return null;
	}

	private Bitmap getPath(final String abspath)
	{
		try
		{
			if(FileUtil.isExists(abspath) != null)
			{
				return BitmapFactory.decodeFile(abspath);
			}
		}
		catch(OutOfMemoryError oom)
		{
			Logger.e("get bm path = " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("get bm path = " + e1.toString());
		}
		return null;
	}

	private Bitmap getId(final int id)
	{
		try
		{
			return BitmapFactory.decodeResource(ctx.getResources(), id);
		}
		catch(OutOfMemoryError oom)
		{
			Logger.e("get bm id " + oom.toString());
		}
		catch (Exception e1)
		{
			Logger.e("get bm id " + e1.toString());
		}
		return null;
	}
	// 按指定的宽/高进行绘制
	private Bitmap scale(Bitmap bm, final int w, int h)
	{
		if(bm == null || bm.isRecycled()) return null;
		try
		{
//			Config cfg = bm.getConfig();
//			if(cfg == null)
//			{
//				cfg = Config.ARGB_4444;
//			}
//			Bitmap bitmap = Bitmap.createBitmap(w, h, cfg/*bm.getConfig()*/);
//			if(bitmap != null)
//			{
//				Canvas cvs = getCvs(bitmap);
//				cvs.drawBitmap(bm, null, new Rect(0, 0, w, h), null);
//				return bitmap;
//			}
			return Bitmap.createScaledBitmap(bm, w, h, true);
		}
		catch(OutOfMemoryError oom)
		{
			Logger.w("scale " + oom.toString());
		}
		catch(Exception ext)
		{
			Logger.e("scale " + ext.toString());
		}
		return null;
	}
	// 指定宽度
	private Bitmap scale(final Bitmap bm, int w)
	{
		if(bm == null || bm.isRecycled() || w <= 0) return null;
		
		float
			imgw = bm.getWidth(),
			imgh = bm.getHeight(),
			neww = w,
			newh;
		
		if(imgw > 0 && imgh > 0)
		{
			newh = neww*imgh/imgw;
			return scale(bm, (int)neww, (int)newh);
		}
		
		return null;
	}
	// 在指定范围内等比例缩放
	private Bitmap scaleProportion(final Bitmap bm, final int maxw, int maxh)
	{
		if(bm == null || bm.isRecycled() || maxw <= 0 || maxh <= 0) return null;
		
		float
			imgw = bm.getWidth(),
			imgh = bm.getHeight(),
			neww,
			newh;
		if(imgw > 0 && imgh > 0)
		{
			if(imgw < maxw && imgh < maxh)
			{
				neww = imgw;
				newh = imgh;
			}
			else
			{
				float
					imgscale = imgh/imgw,
					maxscale = (maxh*1.0f)/(maxw*1.0f);
				if(imgscale > maxscale) // 高度很高
				{
					newh = maxh;
					neww = newh*imgw/imgh;
				}
				else // 宽度很宽
				{
					neww = maxw;
					newh = neww*imgh/imgw;
				}
			}
			return scale(bm, (int)neww, (int)newh);
		}
		return null;
	}
	public void dump(final String key)
	{
		if (key == null || key.length() <= 0)
			return;

		try
		{
//			if (ltKey.remove(key))
			{
//				Bitmap bitmap = hmBm.remove(key);
				Bitmap bitmap = get(key);
				if (bitmap != null && !bitmap.isRecycled())
				{
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
		catch (Exception e1)
		{
			Logger.e("dump()=" + e1.toString());
		}
	}

	public void destroy()
	{
		try
		{
			im = null;
			hmBm.clear();
			hmBm = null;
			rq = null;
		}
		catch (Exception e1)
		{
			Logger.e("image mng destroy()=" + e1.toString());
		}
	}
}

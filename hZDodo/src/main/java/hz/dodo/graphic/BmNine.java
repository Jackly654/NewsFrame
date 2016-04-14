package hz.dodo.graphic;

import android.graphics.Bitmap;
import android.graphics.NinePatch;

public class BmNine
{
	/** .9图片画法
	 * byte[] bArr = bm.getNinePatchChunk();
	 * NinePatch np = new NinePatch(bm, bArr, null);
	 * 
	 * onDraw()
	 * np.draw(canvas, rect9src);
	 */
	
	public BmNine()
	{
	}

	// [0]:left [1]:right [2]:top [3]:bottom
	public int[] getPadding(Bitmap bm)
	{
		if (bm == null) return null;
		byte[] bArr = bm.getNinePatchChunk();
		if (NinePatch.isNinePatchChunk(bArr)) // 验证合法性
		{
			// padding
			// 第12,13,14,15字节 paddingLeft
			// 第16,17,18,19字节 paddingRight
			// 第20,21,22,23字节 paddingTop
			// 第24,25,26,27字节 paddingBottom

			int[] iArr = new int[4];
			iArr[0] = bytesToInt(bArr, 12);
			iArr[1] = bytesToInt(bArr, 16);
			iArr[2] = bytesToInt(bArr, 20);
			iArr[3] = bytesToInt(bArr, 24);
			return iArr;
		}
		return null;
	}

	// 低位在前，高位在后
	private int bytesToInt(byte[] bytes, int offset)
	{
		int addr = bytes[offset] & 0xFF;
		addr |= ( (bytes[offset + 1] << 8) & 0xFF00);
		addr |= ( (bytes[offset + 2] << 16) & 0xFF0000);
		addr |= ( (bytes[offset + 3] << 24) & 0xFF000000);
		return addr;
	}

	// 低位在后，高位在前
	@SuppressWarnings ("unused")
	private int bytesToInt2(byte[] src, int offset)
	{
		int value = (int) ( ( (src[offset] & 0xFF) << 24)
				| ( (src[offset + 1] & 0xFF) << 16)
				| ( (src[offset + 2] & 0xFF) << 8)
				| (src[offset + 3] & 0xFF));
		return value;
	}
}

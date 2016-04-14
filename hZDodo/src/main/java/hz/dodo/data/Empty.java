package hz.dodo.data;

import java.util.HashMap;
import java.util.List;

public class Empty
{
	public static boolean isEmpty(final String sStr)
	{
		return sStr == null || sStr.length() <= 0;
	}
	public static boolean isEmpty(final List<?> lt)
	{
		return lt == null || lt.size() <= 0;
	}
	public static boolean isEmpty(final HashMap<?, ?> hm)
	{
		return hm == null || hm.size() <= 0;
	}
	public static boolean isEmpty(final String[] sArr)
	{
		return sArr == null || sArr.length <= 0;
	}
	public static boolean isEmpty(final int[] iArr)
	{
		return iArr == null || iArr.length <= 0;
	}
}
package hz.dodo.pinyin;

import java.util.Comparator;

public class PinyinCmp implements Comparator<String[]>
{
	public int compare(String[] s1, String[] s2)
	{
		if(s1 == null || s1.length <= 0 || s2 == null || s2.length <= 0) return 0;
		
//		if (s1[0].startsWith("@") || s2[0].startsWith("#"))
//		{
//			return -1;
//		}
//		else if (s1[0].startsWith("#") || s2[0].startsWith("@"))
//		{
//			return 1;
//		}
		
		if (s1[0].startsWith("#") || s2[0].startsWith("#"))
		{
			return 1;
		}

		return s1[0].compareTo(s2[0]);
	}
}

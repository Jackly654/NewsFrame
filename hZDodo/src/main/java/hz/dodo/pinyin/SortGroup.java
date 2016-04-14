package hz.dodo.pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SortGroup
{
	public List<String[]> group(List<String> lt)
	{
		if(lt == null || lt.size() <= 1) return null;
		
		CharacterParser cp = CharacterParser.getInstance();
		String sortString;
		
		int i1 = 0, ic = lt.size();
		List<String[]> lt2 = new ArrayList<String[]>(ic);
		
		while(i1 < ic)
		{
			String[] add = new String[2];
			add[1] = lt.get(i1); // 原字符串
			add[0] = cp.getSelling(add[1]); // pinyin
			
			sortString = add[0].substring(0, 1).toUpperCase(Locale.ENGLISH);
			if (sortString.matches("[A-Z]")) // 正则判断是否是字母
			{
			}
			else
			{
				add[0] = "#";
			}
			
			lt2.add(add);
			++i1;
		}
		
		Collections.sort(lt2, new PinyinCmp());
		return lt2;
	}
}

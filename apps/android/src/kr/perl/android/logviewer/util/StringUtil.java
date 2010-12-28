package kr.perl.android.logviewer.util;

import java.util.AbstractCollection;
import java.util.Iterator;

public class StringUtil {
	public static String join(AbstractCollection<String> s, String delimiter) {
		if (s.isEmpty()) return "";
		Iterator<String> iter = s.iterator();
		StringBuffer buffer = new StringBuffer(iter.next());
		while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
		return buffer.toString();
	}
}
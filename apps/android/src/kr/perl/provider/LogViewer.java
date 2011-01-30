package kr.perl.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class LogViewer {
    public static final class Logs implements BaseColumns {
    	public static final String TABLE_NAME   = "log";
        public static final String CHANNEL      = "channel";
        public static final String NICKNAME     = "nickname";
        public static final String MESSAGE      = "message";
        public static final String FAVORITE     = "favorite";
        public static final String CREATED_ON   = "created_on";

        public static final String AUTHORITY            = "kr.perl.provider.LogViewer.Log";
        public static final Uri CONTENT_URI             = Uri.parse("content://" + AUTHORITY + "/log");
        public static final String CONTENT_TYPE         = "vnd.android.cursor.dir/vnd.kr.perl.log";
        public static final String CONTENT_ITEM_TYPE    = "vnd.android.cursor.item/vnd.kr.perl.log";
        public static final String DEFAULT_SORT_ORDER   = CREATED_ON;
    }
}

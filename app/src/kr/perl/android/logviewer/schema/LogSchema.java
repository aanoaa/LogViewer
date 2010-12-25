package kr.perl.android.logviewer.schema;

import android.net.Uri;
import android.provider.BaseColumns;

public final class LogSchema implements BaseColumns {
    public static final String TABLE_NAME   = "log";
    public static final String CHANNEL      = "channel";
    public static final String NICKNAME     = "nickname";
    public static final String USERNAME     = "username";
    public static final String HOSTNAME     = "hostname";
    public static final String MESSAGE      = "message";
    public static final String CREATED_ON   = "created_on";

    public static final String AUTHORITY            = "kr.perl.android.logviewer.provider.logprovider";
    public static final Uri CONTENT_URI             = Uri.parse("content://" + AUTHORITY + "/log");
    public static final String CONTENT_TYPE         = "vnd.android.cursor.dir/vnd.kr.perl.android.logviewer.log";
    public static final String CONTENT_ITEM_TYPE    = "vnd.android.cursor.item/vnd.kr.perl.android.logviewer.log";
    public static final String DEFAULT_SORT_ORDER   = CREATED_ON + " DESC";
}

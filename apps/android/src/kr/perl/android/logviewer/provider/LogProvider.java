package kr.perl.android.logviewer.provider;

import java.util.HashMap;
import java.util.Map;

import kr.perl.provider.LogViewer.Logs;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class LogProvider extends ContentProvider {

    private static final String     TAG         = "LogProvider";
    public static final String[]    PROJECTION  = new String[] { Logs._ID, Logs.CHANNEL, Logs.NICKNAME, Logs.MESSAGE, Logs.CREATED_ON };

    private static final String DATABASE_NAME       = "log.db";
    private static final int    DATABASE_VERSION    = 2;

    private static final int LOG = 1;       // for all
    private static final int LOG_ID = 2;    // for a row

    private static final UriMatcher     sUriMatcher;
    private static Map<String, String>  sLogsProjectionMap;
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Logs.AUTHORITY, Logs.TABLE_NAME, LOG);
        sUriMatcher.addURI(Logs.AUTHORITY, Logs.TABLE_NAME + "/#", LOG_ID);

        sLogsProjectionMap = new HashMap<String, String>();
        sLogsProjectionMap.put(Logs._ID,          Logs._ID);
        sLogsProjectionMap.put(Logs.CHANNEL,      Logs.CHANNEL);
        sLogsProjectionMap.put(Logs.NICKNAME,     Logs.NICKNAME);
        sLogsProjectionMap.put(Logs.MESSAGE,      Logs.MESSAGE);
        sLogsProjectionMap.put(Logs.FAVORITE,     Logs.FAVORITE);
        sLogsProjectionMap.put(Logs.CREATED_ON,   Logs.CREATED_ON);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL("CREATE TABLE " + Logs.TABLE_NAME + " ("
                + Logs._ID +           " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Logs.CHANNEL +       " TEXT NOT NULL,"
                + Logs.NICKNAME +      " TEXT,"
                + Logs.MESSAGE +       " TEXT,"
                + Logs.FAVORITE +      " INTEGER DEFAULT 0, "
                + Logs.CREATED_ON +    " INTEGER NOT NULL"
                + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + Logs.TABLE_NAME + ";"); // Expedients
            onCreate(db);
        }
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return mDatabaseHelper == null ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Logs.TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case LOG:
                qb.setProjectionMap(sLogsProjectionMap);
                break;

            case LOG_ID:
                qb.setProjectionMap(sLogsProjectionMap);
                qb.appendWhere(Logs._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
            }

            String orderBy;
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Logs.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }

            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
            c.setNotificationUri(getContext().getContentResolver(), uri);

            return c;
        }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case LOG:
                return Logs.CONTENT_TYPE;

            case LOG_ID:
                return Logs.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
    	int numValues = values.length;
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
    	Uri historyUri = null;
    	try {
    		db.beginTransaction();
        	for (ContentValues value : values) {
            	long rowId = db.insert(Logs.TABLE_NAME, Logs.CHANNEL, value);
            	if (rowId > 0) {
                    historyUri = ContentUris.withAppendedId(Logs.CONTENT_URI, rowId);
                }
        	}
        	db.setTransactionSuccessful();
        	if (historyUri != null)	getContext().getContentResolver().notifyChange(historyUri, null);
    	} catch(SQLException e) {
    		Log.e(TAG, e.toString());
    		e.printStackTrace();
    	} finally {
    		db.endTransaction();
    	}
        
        return numValues;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != LOG) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        if (values.containsKey(Logs.CHANNEL) == false) {
            Resources r = Resources.getSystem();
            values.put(Logs.CHANNEL, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(Logs.CREATED_ON) == false) {
            values.put(Logs.CREATED_ON, System.currentTimeMillis());
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.insert(Logs.TABLE_NAME, Logs.CHANNEL, values);
        if (rowId > 0) {
            Uri historyUri = ContentUris.withAppendedId(Logs.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(historyUri, null);
            return historyUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case LOG:
                count = db.delete(Logs.TABLE_NAME, where, whereArgs);
                break;

            case LOG_ID:
                String logId = uri.getPathSegments().get(1);
                count = db.delete(Logs.TABLE_NAME, Logs._ID + "=" + logId 
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case LOG:
                count = db.update(Logs.TABLE_NAME, values, where, whereArgs);
                break;

            case LOG_ID:
                String logId = uri.getPathSegments().get(1);
                count = db.update(Logs.TABLE_NAME, values, Logs._ID + "=" + logId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

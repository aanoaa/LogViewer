package kr.perl.android.logviewer.provider;

import java.util.HashMap;
import java.util.Map;

import kr.perl.android.logviewer.schema.LogSchema;
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
    public static final String[]    PROJECTION  = new String[] { LogSchema._ID, LogSchema.CHANNEL, LogSchema.NICKNAME, LogSchema.MESSAGE, LogSchema.CREATED_ON };

    private static final String DATABASE_NAME       = "log.db";
    private static final int    DATABASE_VERSION    = 1;

    private static final int LOG = 1;       // for all
    private static final int LOG_ID = 2;    // for a row

    private static final UriMatcher     sUriMatcher;
    private static Map<String, String>  sLogSchemaProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(LogSchema.AUTHORITY, LogSchema.TABLE_NAME, LOG);
        sUriMatcher.addURI(LogSchema.AUTHORITY, LogSchema.TABLE_NAME + "/#", LOG_ID);

        sLogSchemaProjectionMap = new HashMap<String, String>();
        sLogSchemaProjectionMap.put(LogSchema._ID,          LogSchema._ID);
        sLogSchemaProjectionMap.put(LogSchema.CHANNEL,      LogSchema.CHANNEL);
        sLogSchemaProjectionMap.put(LogSchema.NICKNAME,     LogSchema.NICKNAME);
        sLogSchemaProjectionMap.put(LogSchema.MESSAGE,      LogSchema.MESSAGE);
        sLogSchemaProjectionMap.put(LogSchema.CREATED_ON,   LogSchema.CREATED_ON);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL("CREATE TABLE " + LogSchema.TABLE_NAME + " ("
                + LogSchema._ID +           " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LogSchema.CHANNEL +       " TEXT NOT NULL,"
                + LogSchema.NICKNAME +      " TEXT,"
                + LogSchema.MESSAGE +       " TEXT,"
                + LogSchema.CREATED_ON +    " INTEGER NOT NULL"
                + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXIST " + LogSchema.TABLE_NAME + ";"); // Expedients
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
        qb.setTables(LogSchema.TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case LOG:
                qb.setProjectionMap(sLogSchemaProjectionMap);
                break;

            case LOG_ID:
                qb.setProjectionMap(sLogSchemaProjectionMap);
                qb.appendWhere(LogSchema._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unkown URI " + uri);
            }

            String orderBy;
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = LogSchema.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }

            SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
            Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
            c.setNotificationUri(getContext().getContentResolver(), uri);

            return c;
        }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case LOG:
                return LogSchema.CONTENT_TYPE;

            case LOG_ID:
                return LogSchema.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
    	Log.d(TAG, "Start");
    	long start = System.currentTimeMillis();
    	int numValues = values.length;
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
    	try {
    		db.beginTransaction();
        	for (ContentValues value : values) {
            	long rowId = db.insert(LogSchema.TABLE_NAME, LogSchema.CHANNEL, value);
            	if (rowId > 0) {
                    Uri historyUri = ContentUris.withAppendedId(LogSchema.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(historyUri, null);
                }
        	}
        	db.setTransactionSuccessful();
    	} catch(SQLException e) {
    		Log.e(TAG, e.toString());
    		e.printStackTrace();
    	} finally {
    		db.endTransaction();
    	}
        
        long elapsedTimeMillis = System.currentTimeMillis()-start;
        Log.d(TAG, "elapsed time: " + (elapsedTimeMillis / 1000.0));
        Log.d(TAG, "End");
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

        if (values.containsKey(LogSchema.CHANNEL) == false) {
            Resources r = Resources.getSystem();
            values.put(LogSchema.CHANNEL, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(LogSchema.CREATED_ON) == false) {
            values.put(LogSchema.CREATED_ON, System.currentTimeMillis());
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.insert(LogSchema.TABLE_NAME, LogSchema.CHANNEL, values);
        if (rowId > 0) {
            Uri historyUri = ContentUris.withAppendedId(LogSchema.CONTENT_URI, rowId);
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
                count = db.delete(LogSchema.TABLE_NAME, where, whereArgs);
                break;

            case LOG_ID:
                String logId = uri.getPathSegments().get(1);
                count = db.delete(LogSchema.TABLE_NAME, LogSchema._ID + "=" + logId 
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
                count = db.update(LogSchema.TABLE_NAME, values, where, whereArgs);
                break;

            case LOG_ID:
                String logId = uri.getPathSegments().get(1);
                count = db.update(LogSchema.TABLE_NAME, values, LogSchema._ID + "=" + logId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

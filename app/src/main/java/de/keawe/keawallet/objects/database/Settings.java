package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.Editable;

import de.keawe.keawallet.objects.Globals;

public class Settings extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static String TABLE_NAME = "settings";
    private static String KEY = "name";
    private static String VAL = "value";
    public final static String PASSWORD_HASH = "password_hash";
    public Settings(Context context) {
        super(context, Globals.APP_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE "+TABLE_NAME+" ("+KEY+" VARCHAR(255) PRIMARY KEY, "+VAL+" TEXT)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void set(String name, Object value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY,name);
        values.put(VAL,value.toString());
        db.insert(TABLE_NAME,null,values);
    }

    public String getString(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{ VAL };
        String select = KEY+"=?";
        String[] args = new String[]{ name };
        Cursor cursor = db.query(TABLE_NAME, columns, select, args, null, null, null);
        if (cursor.getCount()<1) return null;
        cursor.moveToFirst();
        return cursor.getString(0);
    }
}

package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.Editable;

import de.keawe.keawallet.objects.Globals;

public class Settings {

    private static String TABLE_NAME = "settings";
    private static String KEY = "name";
    private static String VAL = "value";
    public final static String PASSWORD_HASH = "password_hash";
    public static final String TABLE_CREATION = "CREATE TABLE "+TABLE_NAME+" ("+KEY+" VARCHAR(255) PRIMARY KEY, "+VAL+" TEXT)";


    public static void set(String name, Object value){
        ContentValues values = new ContentValues();
        values.put(KEY,name);
        values.put(VAL,value.toString());
        Globals.writableDatabase().insert(TABLE_NAME,null,values);
    }

    public static String getString(String name){
        String[] columns = new String[]{ VAL };
        String select = KEY+"=?";
        String[] args = new String[]{ name };
        Cursor cursor = Globals.readableDatabase().query(TABLE_NAME, columns, select, args, null, null, null);
        if (cursor.getCount()<1) return null;
        cursor.moveToFirst();
        return cursor.getString(0);
    }
}

package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.gsm.GsmCellLocation;

import java.util.HashMap;

import de.keawe.keawallet.objects.Globals;

public class Text {

    private static final String KEY="id";
    private static final String TEXT="text";
    private static final String TABLE_NAME="texts";
    public static final String TABLE_CREATION = "CREATE TABLE "+TABLE_NAME+" ("+KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+TEXT+" VARCHAR(255))";

    private static HashMap<String,Text> knownTexts = new HashMap<>();
    private final String text;
    private long id;

    public Text(String s) {
        this.text =s;
    }

    public static Text get(String s) {
        Text text = knownTexts.get(s);
        if (text == null) { // try to get from db
            text = Text.load(s);

            if (text == null){ // create new db entry
                text = new Text(s);
                text.saveToDb();

            }
            knownTexts.put(s,text);
        }
        return text;
    }

    public static Text get(int id) {
        Text result = Text.load(id);
        knownTexts.put(result.text,result);
        return result;
    }

    private static Text load(int id) {
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, KEY+" = ?", new String[]{""+id}, null, null, null);
        Text result = null;
        if (cursor.moveToNext()){
            result = new Text(cursor.getString(1));
            result.id = cursor.getLong(0);
        }
        db.close();
        return result;
    }

    private void saveToDb() {
        SQLiteDatabase db = Globals.writableDatabase();
        ContentValues values = new ContentValues();
        values.put(TEXT, text);
        id = db.insert(TABLE_NAME,null,values);
        db.close();
    }

    private static Text load(String s) {
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "text = ?", new String[]{s}, null, null, null);
        Text result = null;
        if (cursor.moveToNext()){
            result = new Text(cursor.getString(1));
            result.id = cursor.getLong(0);
        }
        db.close();
        return result;
    }

    public long getId() {
        return id;
    }

    public String get() {
        return text;
    }
}

package de.keawe.keawallet.objects.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.keawe.keawallet.R;
import de.keawe.keawallet.objects.Globals;

public class Category {

    private static final String TABLE_NAME = "categories";
    private static final String CATEGORY = "category";
    private static final String KEY = "id";
    public static final String TABLE_CREATION = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CATEGORY + " VARCHAR(255))";
    private static HashMap<Integer,Category> catList = new HashMap<>();
    private final String definition;
    private long id = 0;

    public Category(String def) {
        definition = def;
    }

    public static Category load(int id) {
        Category cat = catList.get(id);
        if (cat != null) return cat;

        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor=db.query(TABLE_NAME,null,KEY+" = ?",new String[]{""+id},null,null,null);
        if (cursor.moveToNext()){
            cat = new Category(cursor.getString(cursor.getColumnIndex(CATEGORY)));
            cat.id = id;
            catList.put(id,cat);
        }
        db.close();
        return cat;
    }


    public static Vector<Category> loadAll() {

        Vector<Category> cats = new Vector<>();
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor=db.query(TABLE_NAME,null,null,null,null,null,CATEGORY+" ASC");
        while (cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(KEY));
            Category cat = catList.get(id);
            if (cat == null){
                cat = new Category(cursor.getString(cursor.getColumnIndex(CATEGORY)));
                cat.id = id;
            }
            cats.add(cat);
        }
        db.close();
        return cats;
    }

    @Override
    public String toString() {
        return definition;
    }

    public static String preset() {
        StringBuffer result = new StringBuffer("INSERT OR IGNORE INTO "+TABLE_NAME+"("+CATEGORY+") VALUES ");
        int[] values = { R.string.insurance_life,
                         R.string.insurance_health,
                         R.string.mobility_car,
                         R.string.mobility_public,
                         R.string.conumption,
                         R.string.leisure,
                         R.string.income,
                         R.string.ventures,
                         R.string.fees
                        };
        for (int v : values) result.append("('"+Globals.string(v)+"') ");
        return result.toString().replace(") (","), (");
    }

    public List<String> structure() {
        return Arrays.asList(definition.split("\\."));
    }
}
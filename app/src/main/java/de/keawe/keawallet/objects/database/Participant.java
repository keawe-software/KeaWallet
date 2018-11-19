package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.kapott.hbci.structures.Konto;

import de.keawe.keawallet.objects.Globals;

public class Participant {

    private static final String KEY = "id";
    private static final String HASH = "hash";
    private static final String BIC = "bic";
    private static final String BLZ = "blz";
    private static final String IBAN = "iban";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String TABLE_NAME = "participants";
    public static final String TABLE_CREATION = "CREATE TABLE "+TABLE_NAME+" ("+KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+HASH+" VARCHAR(255) NOT NULL, "+BIC+" VARCHAR(255), "+BLZ+" VARCHAR(255), "+IBAN+" VARCHAR(255), "+NAME+" VARCHAR(255), "+NUMBER+" VARCHAR(255))";

    private String bic;
    private String blz;
    private String iban;
    private String name;
    private String number;
    private Long id;

    public Participant(Konto k) {
        if (k.acctype != null) Globals.w("Konto has acctype set: "+k.acctype);
        this.bic = k.bic == null ? null : k.bic;
        this.blz = k.blz == null ? null : k.blz;
        if (k.customerid != null) Globals.w("Konto has customerid set: "+k.customerid);
        this.iban = k.iban == null ? null : k.iban;
        this.name = k.name == null ? (k.name2 == null? null : k.name2) : k.name+(k.name2 == null ? "": k.name2);
        this.number = k.number == null ? null : k.number.isEmpty() ? null : k.number;

        String hash = Globals.byteArrayToHexString(Globals.hash(bic+"\n"+iban+"\n"+name+"\n"+number));

        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME,new String[]{ KEY },HASH+" = ?",new String[]{ hash },null,null,null);
        id = cursor.moveToNext() ? cursor.getLong(0) : null;
        db.close();

        if (id == null){
            db = Globals.writableDatabase();
            ContentValues values = new ContentValues();
            values.put(HASH,hash);
            values.put(BIC,bic);
            values.put(IBAN,iban);
            values.put(NAME,name);
            values.put(NUMBER,number);
            id = db.insert(TABLE_NAME,null,values);
            db.close();
        }
    }

    public Long getId() {
        return id;
    }
}

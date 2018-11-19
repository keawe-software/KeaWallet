package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.kapott.hbci.GV_Result.GVRKUms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import de.keawe.keawallet.objects.Globals;

public class Transaction {
    private final static String TABLE_NAME = "transactions";
    private final static String KEY = "id";
    private final static String ACCOUNT   = "bank_account";
    private final static String BDATE     = "bdate";
    private final static String GVCODE    = "gv";
    private final static String INSTREF   = "instref";
    private final static String OTHER     = "other";
    private final static String PRIMANOTA = "primanota";
    private final static String TEXT      = "text";
    private final static String USAGE     = "usage";
    private final static String VALUE     = "value";
    private final static String VALUTA    = "valuta";

    public final static String TABLE_CREATION = "CREATE TABLE "+TABLE_NAME+"("+
            KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            ACCOUNT+" INT NOT NULL, "+
            BDATE+" LONG, "+
            GVCODE+" INT, "+
            INSTREF+" VARCHAR(255), "+
            OTHER+" LONG,"+
            PRIMANOTA+" INT,"+
            TEXT+" INT, "+
            USAGE+" VARCHAR(255), "+
            VALUE+" LONG, "+
            VALUTA+" LONG)";

    private long id = 0;
    private BankAccount account = null;
    private Long bdate = null; // Buchungsdatum
    private Integer gvcode = null;
    private String instRef = null;
    private Long other = null;
    private Integer primanota = null;
    private Text text = null;
    private String usage = null;
    private Long value = null; // Cent
    private Long valuta = null; // Wertstellung

    public Transaction(Cursor cursor, BankAccount account) {
        this.account = account;
        for (int index = 0; index < cursor.getColumnCount(); index++){
            String col = cursor.getColumnName(index);
            switch (col){
                case KEY:       this.id        = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case BDATE:     this.bdate     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case GVCODE:    this.gvcode    = cursor.isNull(index) ? null : cursor.getInt(index); break;
                case INSTREF:   this.instRef   = cursor.getString(index); break;
                case OTHER:     this.other     = cursor.getLong(index); break;
                case PRIMANOTA: this.primanota = cursor.isNull(index) ? null : cursor.getInt(index); break;
                case VALUTA:    this.valuta    = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case VALUE:     this.value     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case USAGE:     this.usage     = cursor.getString(index); break;
                case TEXT:      this.text      = cursor.isNull(index) ? null : Text.get(cursor.getInt(index)); break;
            }
        }
    }

    public Transaction(GVRKUms.UmsLine hbciTransaction,BankAccount account) {
        this.account   = account;
        this.bdate     = hbciTransaction.bdate    == null ? null : hbciTransaction.bdate.getTime();
        this.gvcode    = hbciTransaction.gvcode    == null ? null : Integer.parseInt(hbciTransaction.gvcode);
        this.instRef   = hbciTransaction.instref   == null ? null : hbciTransaction.instref;
        this.other     = hbciTransaction.other     == null ? null : (new Participant(hbciTransaction.other)).getId();
        this.primanota = hbciTransaction.primanota == null ? null : Integer.parseInt(hbciTransaction.primanota);
        this.text      = hbciTransaction.text      == null ? null : Text.get(hbciTransaction.text);
        this.value     = hbciTransaction.value     == null ? null : hbciTransaction.value.getLongValue();
        this.valuta    = hbciTransaction.valuta    == null ? null : hbciTransaction.valuta.getTime();

        if (hbciTransaction.usage != null) {
            StringBuffer sb = new StringBuffer();
            Iterator<String> it = hbciTransaction.usage.iterator();
            while (it.hasNext()) sb.append(it.next()+"\n");
            this.usage = sb.toString().replace("\t"," ");
            while (this.usage.indexOf("  ")>0) this.usage=this.usage.replace("  "," ");
        }

        if (hbciTransaction.charge_value != null) Globals.w("HBCI Transaction contains charge_value: "+hbciTransaction.charge_value);
        if (hbciTransaction.purposecode != null) Globals.w("HBCI Transaction contains purposecode: "+hbciTransaction.purposecode);
        if (hbciTransaction.id != null) Globals.w("HBCI Transaction contains id: "+hbciTransaction.id);
        if (hbciTransaction.additional != null) Globals.w("HBCI Transaction contains additional: "+hbciTransaction.additional);
        if (hbciTransaction.orig_value != null) Globals.w("HBCI Transaction contains orig_value: "+hbciTransaction.orig_value);
    }



    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getDefault());
        return "Transaction("+sdf.format(new Date(bdate))+": "+String.format("%10s",value/100f)+"€, "+usage+")";
    }

    public static Transaction getLastFor(BankAccount account) {
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ?", new String[]{"" + account.id()}, null, null, KEY + " DESC", "1");
        Transaction result = null;
        if (cursor.moveToNext()) result = new Transaction(cursor,account);
        db.close();
        return result;
    }


    public void saveToDb() {
        SQLiteDatabase db = Globals.writableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT,  account.id());
        values.put(BDATE,    bdate);
        values.put(GVCODE,   gvcode);
        values.put(INSTREF,  instRef);
        values.put(OTHER,    other);
        values.put(PRIMANOTA,primanota);
        values.put(TEXT,     text == null ? null : text.getId());
        values.put(USAGE,    usage);
        values.put(VALUE,    value);
        values.put(VALUTA,   valuta);
        this.id = db.insert(TABLE_NAME,null,values);
    }

    public Long bdate() {
        return bdate;
    }
}

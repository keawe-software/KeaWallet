package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.kapott.hbci.GV_Result.GVRKUms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import de.keawe.keawallet.objects.Globals;

public class Transaction {
    private final static String ACCOUNT   = "bank_account";
    private final static String BDATE     = "bdate";
    private final static String GVCODE    = "gv";
    private final static String INSTREF   = "instref";
    private final static String PRIMANOTA = "primanota";
    private final static String TEXT      = "text";
    private final static String USAGE     = "usage";
    private final static String VALUE     = "value";
    private final static String VALUTA    = "valuta";

    private final static String TABLE_NAME = "transactions";
    private final static String KEY = "id";
    public final static String TABLE_CREATION = "CREATE TABLE "+TABLE_NAME+"("+
            KEY+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            ACCOUNT+" INT NOT NULL, "+
            BDATE+" LONG, "+
            VALUTA+" LONG, "+
            GVCODE+" INT, "+
            VALUE+" LONG, "+
            USAGE+" VARCHAR(255), "+
            TEXT+" INT, "+
            INSTREF+" VARCHAR(255), "+
            PRIMANOTA+" INT)";


    public void saveToDb() {
        SQLiteDatabase db = Globals.writableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT,  account.id());
        values.put(BDATE,    bdate);
        values.put(GVCODE,   gvcode);
        values.put(INSTREF,  instRef);
        values.put(PRIMANOTA,primanota);
        values.put(TEXT,     text);
        values.put(USAGE,    usage);
        values.put(VALUE,    value);
        values.put(VALUTA,   valuta);
        this.id = db.insert(TABLE_NAME,null,values);
    }


    private Long bdate = null; // Buchungsdatum
    private Long valuta = null; // Wertstellung

    private Integer gvcode = null;
    private Long value = null; // Cent

    private String usage = null;
    private Long text = null;
    private String instRef = null;
    private Integer primanota = null;
    private BankAccount account;
    private long id = 0;

    public Transaction(GVRKUms.UmsLine hbciTransaction,BankAccount account) {
        this.account   = account;
        this.bdate     = hbciTransaction.bdate    == null ? null : hbciTransaction.bdate.getTime();
        this.valuta    = hbciTransaction.valuta    == null ? null : hbciTransaction.valuta.getTime();
        this.gvcode    = hbciTransaction.gvcode    == null ? null : Integer.parseInt(hbciTransaction.gvcode);
        this.value     = hbciTransaction.value     == null ? null : hbciTransaction.value.getLongValue();
        this.text      = hbciTransaction.text      == null ? null : Text.get(hbciTransaction.text).getId();
        this.instRef   = hbciTransaction.instref   == null ? null : hbciTransaction.instref;
        this.primanota = hbciTransaction.primanota == null ? null : Integer.parseInt(hbciTransaction.primanota);

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


}

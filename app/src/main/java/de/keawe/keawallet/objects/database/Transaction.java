package de.keawe.keawallet.objects.database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.kapott.hbci.GV_Result.GVRKUms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

import de.keawe.keawallet.Levenshtein;
import de.keawe.keawallet.R;
import de.keawe.keawallet.TransactionDetailActivity;
import de.keawe.keawallet.objects.Globals;

public class Transaction implements Serializable {
    private final static String TABLE_NAME = "transactions";
    private final static String KEY = "id";
    private final static String ACCOUNT   = "bank_account";
    private final static String BDATE     = "bdate";
    private final static String CATEGORY  = "category";
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
            CATEGORY+" INT DEFAULT 0, "+
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
    private Long category = null; // referenz auf category tabelle
    private Integer gvcode = null;
    private String instRef = null;
    private Long other = null;
    private Integer primanota = null;
    private Text text = null;
    private String usage = null;
    private Long value = null; // Cent
    private Long valuta = null; // Wertstellung
    private String hash = null;

    public Transaction(Cursor cursor, BankAccount account) {
        this.account = account;
        for (int index = 0; index < cursor.getColumnCount(); index++){
            String col = cursor.getColumnName(index);
            switch (col){
                case KEY:       this.id        = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case BDATE:     this.bdate     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case CATEGORY:  this.category  = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case GVCODE:    this.gvcode    = cursor.isNull(index) ? null : cursor.getInt(index); break;
                case INSTREF:   this.instRef   = cursor.getString(index); break;
                case OTHER:     this.other     = cursor.isNull(index) ? null : cursor.getLong(index); break;
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
        this.category  = null;
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

    public static Vector<Transaction> getFor(BankAccount account, Calendar month) {
        Calendar date = Globals.firstOf(month);
        long start = date.getTimeInMillis() - 1;
        date.add(Calendar.DATE,35);
        date = Globals.firstOf(date);
        long end = date.getTimeInMillis();
        SQLiteDatabase db = Globals.readableDatabase();

        Vector<Transaction> transactions = new Vector<>();
        Cursor cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ? AND bdate > ? AND bdate < ?", new String[]{"" + account.id(), ""+start, ""+end}, null, null, null);
        while (cursor.moveToNext()) transactions.add(new Transaction(cursor,account));
        cursor.close();
        db.close();
        return transactions;
    }

    public static Transaction load(long id) {
        SQLiteDatabase db = Globals.readableDatabase();
        Transaction transaction = null;
        Cursor cursor = db.query(TABLE_NAME,null,KEY+" = "+id,null,null,null, null);
        if (cursor.moveToNext()) {
            long account_id = cursor.getLong(cursor.getColumnIndex(ACCOUNT));
            BankAccount account = BankAccount.load(account_id);
            transaction = new Transaction(cursor,account);
        }
        cursor.close();
        db.close();
        return transaction;
    }

    public Long bdate() {
        return bdate;
    }

    public static Vector<Transaction> getLastFor(BankAccount account) {
        SQLiteDatabase db = Globals.readableDatabase();

        Vector<Transaction> lastTransactions = new Vector<>();
        Long lastDate = null;
        Cursor cursor = db.query(TABLE_NAME, new String[]{ BDATE },ACCOUNT + " = ?", new String[]{"" + account.id()},null,null,BDATE + " DESC","1");
        if (cursor.moveToNext()) lastDate = cursor.getLong(0);
        if (lastDate != null){
            cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ? AND bdate = ?", new String[]{"" + account.id(), ""+lastDate}, null, null, null);
            while (cursor.moveToNext()) lastTransactions.add(new Transaction(cursor,account));
        }
        cursor.close();
        db.close();
        return lastTransactions;
    }

    public boolean in(Vector<Transaction> otherTransactions) {
        String hash = this.hash();
        for (Transaction otherT: otherTransactions) {
            if (hash.equals(otherT.hash())) return true;
        }
        return false;
    }

    public String hash() {
        if (this.hash == null){
            Long textId = text == null ? null : text.getId();
            this.hash = Globals.byteArrayToHexString(Globals.hash(account.id() + "\\" + bdate + "\\" + gvcode + "\\" + instRef + "\\" + other + "\\" + primanota + "\\" + textId + "\\" + usage.replace("\n", "#") + "\\" + value + "\\" + valuta));
        }
        return this.hash;
    }



    public void saveToDb() {
        SQLiteDatabase db = Globals.writableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT,  account.id());
        values.put(BDATE,    bdate);
        values.put(CATEGORY, category);
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

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getDefault());
        return "Transaction("+sdf.format(new Date(bdate))+": "+String.format("%10s",value/100f)+"€, "+usage.replace("\n","\\")+")";
    }

    public Category category() {
        return category == null ? null : Category.load(category);
    }

    public String niceUsage() {
        String[] parts = usage.split("\n");
        StringBuffer sb = new StringBuffer();
        for (String p:parts) {
            if (p.startsWith("EREF+")) continue;
            if (p.startsWith("MREF+")) continue;
            if (p.startsWith("CRED+")) continue;
            if (p.startsWith("DEBT+")) continue;

            int i = p.indexOf("EREF:");
            if (i<0) i = p.indexOf("MREF:");
            if (i<0) i = p.indexOf("CREAD:");
            if (i<0) i = p.indexOf("SVWZ+");
            if (i>0) {
                sb.append(p.substring(0,i));
                break;
            }
            sb.append(p);
        }

        return sb.toString().trim();
    }

    public String value(String currency) {
        return String.format("%.2f",value/100.0)+" "+currency;
    }

    public long value(){
        return value;
    }

    public Participant participant() {
        if (other == null) return null;
        return Participant.load(other);
    }

    public void setCategory(Category cat) {
        System.out.println("Assigning "+cat+" to "+this);
        category = cat == null ? 0 : cat.getId();
        ContentValues values = new ContentValues();
        values.put(CATEGORY,category);
        SQLiteDatabase db = Globals.writableDatabase();
        db.update(TABLE_NAME,values,KEY+" = "+id,null);
        db.close();
    }

    public RelativeLayout getView(final Activity activity) {
        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.transaction_display,null);

        ((TextView) layout.findViewById(R.id.transaction_date_view)).setText(bdate("yyyy-MM-dd"));
        ((TextView) layout.findViewById(R.id.transaction_usage_view)).setText(niceUsage());
        ((TextView) layout.findViewById(R.id.transaction_value_view)).setText(value(account.currency()));
        ((TextView) layout.findViewById(R.id.transaction_participant_view)).setText(participant()==null?"":participant().name());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent transactionView = new Intent(activity,TransactionDetailActivity.class);
                transactionView.putExtra(TransactionDetailActivity.TRANSACTION,Transaction.this.id);
                activity.startActivity(transactionView);
            }
        });

        return layout;
    }

    public String bdate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(bdate()));
    }

    public String currency() {
        return account.currency();
    }

    public String usage() {
        return usage;
    }

    public Transaction findMostSimilarIn(Vector<Transaction> transactionList) {
        double similarity = 0;
        Transaction result = null;
        for(Transaction transaction:transactionList) {
            double sim = this.compare(transaction);
            if (sim >= similarity) {
                similarity = sim;
                result = transaction;
            }
        }return result;
    }

    public static Vector<Transaction> loadCategorized(long account_id){
        Vector<Transaction> transactions = new Vector<>();
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME,null,CATEGORY+" != 0 AND "+ACCOUNT+" = "+account_id,null, null, null, null);
        while (cursor.moveToNext()) transactions.add(new Transaction(cursor, null));
        cursor.close();
        db.close();
        return transactions;
    }

    public double compare(Transaction transaction) {
        return this.compareValue(transaction) * this.compareUsage(transaction) * this.compareParticipant(transaction);
    }

    private double compareParticipant(Transaction transaction) {
        if (this.other == null){
            if (transaction.other == null) return 0.8; // both transactions have no participant
            return 0.2; // one has no participant, the other has
        }
        if (transaction.other == null) return 0.2;  // one has no participant, the other has
        return participant().compare(transaction.participant());
    }

    private double compareUsage(Transaction transaction) {
        int x=Levenshtein.distance(this.usage,transaction.usage);
        return 1d/(1+x);
    }

    private double compareValue(Transaction transaction) {
        double x = this.value - transaction.value;
        return 1 - (x/(x+1));
    }
}

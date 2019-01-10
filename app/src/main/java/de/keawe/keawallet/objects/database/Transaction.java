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
    private final static String TABLE_NAME   = "transactions";
    private final static String KEY          = "id";
    private final static String ACCOUNT      = "bank_account";
    private final static String AUTO_CAT     = "auto_cat";
    private final static String BDATE        = "bdate";
    private final static String CATEGORY     = "category";
    private final static String EXP_REPEAT   = "exp_repeat";
    private final static String GVCODE       = "gv";
    private final static String INSTREF      = "instref";
    private final static String MOST_SIMILAR = "similar";
    private final static String OTHER        = "other";
    private final static String PRIMANOTA    = "primanota";
    private final static String SALDO        = "saldo";
    private final static String TEXT         = "text";
    private final static String USAGE        = "usage";
    private final static String VALUE        = "value";
    private final static String VALUTA       = "valuta";


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
    private boolean auto_cat = false;
    private Long bdate = null; // Buchungsdatum
    private Long category = null; // referenz auf category tabelle
    private Integer gvcode = null;
    private String instRef = null;
    private Long other = null;
    private Integer primanota = null;
    private Long saldo = null;
    private Long similar = null;
    private Text text = null;
    private String usage = null;
    private Long value = null; // Cent
    private Long valuta = null; // Wertstellung
    private String hash = null;
    private String exp_repeat = null; // YYYY-MM der erwarteten Wiederholung der Transaktion

    public Transaction(Cursor cursor, BankAccount account) {
        this.account = account;
        for (int index = 0; index < cursor.getColumnCount(); index++){
            String col = cursor.getColumnName(index);
            switch (col){
                case KEY:          this.id        = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case AUTO_CAT:     this.auto_cat  = cursor.getInt(index)==1; break;
                case BDATE:        this.bdate     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case CATEGORY:     this.category  = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case EXP_REPEAT:   this.exp_repeat= cursor.getString(index); break;
                case GVCODE:       this.gvcode    = cursor.isNull(index) ? null : cursor.getInt(index); break;
                case INSTREF:      this.instRef   = cursor.getString(index); break;
                case MOST_SIMILAR: this.similar = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case OTHER:        this.other     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case PRIMANOTA:    this.primanota = cursor.isNull(index) ? null : cursor.getInt(index); break;
                case SALDO:        this.saldo     = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case TEXT:         this.text      = cursor.isNull(index) ? null : Text.get(cursor.getInt(index)); break;
                case USAGE:        this.usage     = cursor.getString(index); break;
                case VALUTA:       this.valuta    = cursor.isNull(index) ? null : cursor.getLong(index); break;
                case VALUE:        this.value     = cursor.isNull(index) ? null : cursor.getLong(index); break;
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
        this.saldo     = hbciTransaction.saldo.value.getLongValue();
        this.similar   = null;
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
        Cursor cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ? AND bdate > ? AND bdate < ?", new String[]{"" + account.id(), ""+start, ""+end}, null, null,KEY);
        while (cursor.moveToNext()) transactions.add(new Transaction(cursor,account));
        cursor.close();
        db.close();
        return transactions;
    }

    public static Vector<Transaction> getExpectedFor(BankAccount account, Calendar month) {
        String searchKey = Globals.yearDate(month);

        SQLiteDatabase db = Globals.readableDatabase();

        Vector<Transaction> transactions = new Vector<>();
        Cursor cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ? AND exp_repeat = ?", new String[]{"" + account.id(), searchKey}, null, null,KEY);
        while (cursor.moveToNext()) transactions.add(new Transaction(cursor,account));
        cursor.close();
        db.close();
        return transactions;
    }

    public static void reassign(long fromAccountId, long toAccountId) {
        BankAccount fromAccount = BankAccount.load(fromAccountId);
        BankAccount toAccount = BankAccount.load(toAccountId);
        System.out.println("Changing assignment of transactions from "+fromAccount+" to "+toAccount);
        SQLiteDatabase db = Globals.writableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNT,toAccountId);
        db.update(TABLE_NAME,values,ACCOUNT+" = "+fromAccountId,null);
        db.close();
    }

    public String getSaldo() {
        if (saldo == null) return "null";
        return account.currency(saldo);
    }

    public static Transaction load(long transaction_id) {
        SQLiteDatabase db = Globals.readableDatabase();
        Transaction transaction = null;
        Cursor cursor = db.query(TABLE_NAME,null,KEY+" = "+transaction_id,null,null,null, null);
        if (cursor.moveToNext()) {
            long account_id = cursor.getLong(cursor.getColumnIndex(ACCOUNT));
            BankAccount account = BankAccount.load(account_id);
            transaction = new Transaction(cursor,account);
        }
        cursor.close();
        db.close();
        return transaction;
    }

    public static String getUpdate(int version) {
        switch (version){
            case 2:
                return "ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+AUTO_CAT+" BOOLEAN DEFAULT false";
            case 3:
                return "ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+SALDO+" LONG";
            case 4:
                return "ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+MOST_SIMILAR+" LONG";
            case 5:
                return "ALTER TABLE "+TABLE_NAME+" ADD COLUMN "+EXP_REPEAT+" VARCHAR(7)";
        }
        return null;
    }

    public Long bdate() {
        return bdate;
    }

    public static Vector<Transaction> getLastFor(long bankAccountId) {
        SQLiteDatabase db = Globals.readableDatabase();

        Vector<Transaction> lastTransactions = new Vector<>();
        Long lastDate = null;
        Cursor cursor = db.query(TABLE_NAME, new String[]{ BDATE },ACCOUNT + " = ?", new String[]{"" + bankAccountId},null,null,BDATE + " DESC","1");
        if (cursor.moveToNext()) lastDate = cursor.getLong(0);
        if (lastDate != null){
            BankAccount bankAccout = BankAccount.load(bankAccountId);
            cursor = db.query(TABLE_NAME, null, ACCOUNT + " = ? AND bdate = ?", new String[]{"" + bankAccountId, ""+lastDate}, null, null, null);
            while (cursor.moveToNext()) lastTransactions.add(new Transaction(cursor,bankAccout));
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
        values.put(ACCOUNT,      account.id());
        values.put(BDATE,        bdate);
        values.put(CATEGORY,     category);
        values.put(GVCODE,       gvcode);
        values.put(EXP_REPEAT,   exp_repeat);
        values.put(INSTREF,      instRef);
        values.put(MOST_SIMILAR, similar);
        values.put(OTHER,        other);
        values.put(PRIMANOTA,    primanota);
        values.put(SALDO,        saldo);
        values.put(TEXT,         text == null ? null : text.getId());
        values.put(USAGE,        usage);
        values.put(VALUE,        value);
        values.put(VALUTA,       valuta);
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
        if (text != null) sb.append(text.get()+"\n");
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

    public String firstLine(){
        String parts[] = niceUsage().split("\n");
        if (parts.length>0) return parts[0];
        return null;
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
        this.setCategory(cat, false);
    }

    public void setCategory(Category cat, boolean auto) {
        if (cat == null){
            //if (category == 0l) System.out.println("Removing category from "+this.niceUsage());
            category = 0l;
        } else {
            //if (category == null || category != cat.getId()) System.out.println("Assigning category '"+cat.full()+"' with "+this.niceUsage());
            category = cat.getId();
        }
        auto_cat = auto;
        ContentValues values = new ContentValues();
        values.put(CATEGORY,category);
        values.put(AUTO_CAT,auto_cat);
        SQLiteDatabase db = Globals.writableDatabase();
        db.update(TABLE_NAME,values,KEY+" = "+id,null);
        db.close();
    }

    public void setAccount(BankAccount bankAccount){
        //System.out.println("Assigning transaction to "+bankAccount);
        account = bankAccount;
        ContentValues values = new ContentValues();
        values.put(ACCOUNT,account.id());
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
        layout.findViewById(R.id.category_warning).setVisibility(auto_cat?View.VISIBLE:View.GONE);

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

    public RelativeLayout getExpectationView(final Activity activity) {
        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.transaction_display,null);
        int gray = Globals.color(R.color.gray);
        TextView tv = ((TextView) layout.findViewById(R.id.transaction_date_view));

        long days = bdate / (24 * 3600 * 1000);
        if (days == 0) {
            tv.setText(R.string.today);
        } else if (days == 1){
            tv.setText(R.string.tomorrow);
        } else if (days < 0){
            tv.setText(Globals.string(R.string.x_days_ago).replace("?",-days+""));
        } else {
            tv.setText(Globals.string(R.string.in_x_days).replace("?",""+days));
        }



        tv.setTextColor(gray);
        tv.setMinWidth(200);
        tv = ((TextView) layout.findViewById(R.id.transaction_usage_view));
        tv.setText(firstLine());
        tv.setTextColor(gray);
        tv = ((TextView) layout.findViewById(R.id.transaction_value_view));
        tv.setText(value(account.currency()));
        tv.setTextColor(gray);
        tv = ((TextView) layout.findViewById(R.id.transaction_participant_view));
        tv.setText(participant()==null?"":participant().name());
        tv.setTextColor(gray);
        //layout.setBackgroundColor(Globals.context().getResources().getColor(R.color.yellow));
        layout.findViewById(R.id.category_warning).setVisibility(View.GONE);
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
        }
        return result;
    }

    public static Vector<Transaction> loadCategorized(long account_id){
        Vector<Transaction> transactions = new Vector<>();
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME,null,CATEGORY+" != 0 AND "+ACCOUNT+" = "+account_id,null, null, null, "id DESC");
        while (cursor.moveToNext()) transactions.add(new Transaction(cursor, null));
        cursor.close();
        db.close();
        return transactions;
    }

    public static Transaction first(long account_id){
        Transaction trans = null;
        SQLiteDatabase db = Globals.readableDatabase();
        BankAccount account = BankAccount.load(account_id);
        Cursor cursor = db.query(TABLE_NAME,null,ACCOUNT+" = "+account_id,null, null, null, "id ASC", "1");
        if (cursor.moveToNext()) trans = new Transaction(cursor, account);
        cursor.close();
        db.close();
        return trans;
    }

    public double compare(Transaction transaction) {
        return this.compareValue(transaction) * this.compareUsage(transaction) * this.compareParticipant(transaction) * this.compareText(transaction);
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
        double x = Math.abs(this.value - transaction.value);
        return 1 - (x/(x+1));
    }

    private double compareText(Transaction transaction){
        if (this.text == null){
            if (transaction.text == null) return 0.8; // both transactions have no participant
            return 0.2; // one has no participant, the other has
        }
        if (transaction.text == null) return 0.2;  // one has no participant, the other has
        int x=Levenshtein.distance(this.text(),transaction.text());
        return 1d/(1+x);
    }

    public void setMostSimilar(Transaction similarTransaction) {
        if (similarTransaction == null){
            //System.out.println("Resetting similar transaction.");
            similar = null;
        } else {
            //System.out.println("Storing most similar transaction: "+similarTransaction.id());
            similar = similarTransaction.id;
        }

        ContentValues values = new ContentValues();
        values.put(MOST_SIMILAR,similar);
        SQLiteDatabase db = Globals.writableDatabase();
        db.update(TABLE_NAME,values,KEY+" = "+id,null);
        db.close();
    }

    public String text() {
        return text.get();
    }

    public long id() {
        return id;
    }

    public Long similar() {
        return similar;
    }

    public void expectRepetition(String exp) {
        ContentValues values = new ContentValues();
        values.put(EXP_REPEAT,exp);
        SQLiteDatabase db = Globals.writableDatabase();
        db.update(TABLE_NAME,values,KEY+" = "+id,null);
        db.close();
    }

    public String expectedRepetition() {
        return exp_repeat;
    }

    public void setBDate(long timestamp) {
        bdate = timestamp;
    }
}

package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.keawe.keawallet.objects.Globals;

/**
 * Diese Klasse repräsentiert ein Bank(unter)konto
 * Created by srichter on 07.09.15.
 */
public class BankAccount {

    private static final String KEY = "id";
    private static final String LOGIN = "bank_login";
    private static final String NUMBER = "number";
    private static final String CURRENCY = "currency";
    private static final String TABLE_NAME = "bank_accounts";
    public static final String TABLE_CREATION = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LOGIN + " INT NOT NULL, " + NUMBER + " VARCHAR(255), " + CURRENCY + " VARCHAR(255))";
    // Datenfelder
    private String accountNumber;
    private BankLogin bankLogin;
    private String currency;
    private long id;

    public BankAccount(BankLogin bankLogin, String accountNumber, String currency) {
        this.bankLogin = bankLogin;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }

    public BankAccount(Cursor cursor){
        for (int index=0; index<cursor.getColumnCount();index++){
            String name = cursor.getColumnName(index);
            switch (name){
                case KEY:
                    id = cursor.getLong(index);
                    break;
                case NUMBER:
                    accountNumber = cursor.getString(index);
                    break;
                case CURRENCY:
                    currency = cursor.getString(index);
                    break;
                case LOGIN:
                    bankLogin = BankLogin.load(cursor.getLong(index));
                    break;
            }
        }
    }

    public static Vector<BankAccount> load(BankLogin bankLogin) {
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, bankLogin == null ? null : LOGIN+" = "+bankLogin.getId(), null, null, null, null);

        Vector<BankAccount> accounts = new Vector<>();
        while (cursor.moveToNext()){
            BankAccount account = new BankAccount(cursor);
            account.bankLogin = bankLogin;
            accounts.add(account);
        }
        cursor.close();
        db.close();
        return accounts;
    }

    public static BankAccount load(long id) {
        SQLiteDatabase db = Globals.readableDatabase();
        BankAccount account = null;
        Cursor cursor = db.query(TABLE_NAME,null,KEY+" = "+id,null,null,null, null);
        if (cursor.moveToNext()) account = new BankAccount(cursor);
        cursor.close();
        db.close();
        return account;
    }

    @Override
    /**
     * gibt eine einfache Textrepräsentation
     */
    public String toString() {
        return bankLogin.getInstitute().name()+": Konto " + accountNumber;
    }

    public void saveToDb() {
        Globals.d("Saving "+this);
        ContentValues values = new ContentValues();
        values.put(NUMBER,this.accountNumber);
        values.put(CURRENCY,this.currency);
        values.put(LOGIN,bankLogin.getId());
        SQLiteDatabase db = Globals.writableDatabase();
        id = db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public GVRKUms fetchNewTransactions() throws ParserConfigurationException, TransformerException, SAXException, IOException {
        return fetchNewTransactionsSince(null);
    }

    public GVRKUms fetchNewTransactionsSince(Long start) throws ParserConfigurationException, TransformerException, SAXException, IOException {
        Properties props = new Properties();
        props.put("my.number", accountNumber);
        props.put("my.blz", bankLogin.getInstitute().blz);
        props.put("my.bic", bankLogin.getInstitute().bic);
        props.put("my.iban", IBAN());
        if (start != null) {
            SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd");
            props.put("startdate", ISO.format(new Date(start)));
        }
        return (GVRKUms) bankLogin.executeJob("KUmsAll", props);
    }

    private String IBAN(){
        String blz = bankLogin.getInstitute().blz;
        Iban iban = new Iban.Builder().countryCode(CountryCode.DE).bankCode(blz).accountNumber(accountNumber).build();
        return iban.toString();

    }

    public String number() {
        return accountNumber;
    }

    public long id() {
        return id;
    }

    public Vector<Transaction> transactions(Calendar month) {
        return Transaction.getFor(this,month);
    }

    public String currency() {
        switch (currency){
            case "EUR": return "€";
        }
        return currency;
    }

    public String currency(long value){
        return value/100.0 + currency();
    }
}

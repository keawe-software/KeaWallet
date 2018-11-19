package de.keawe.keawallet.objects.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.manager.HBCIUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
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

    public static Vector<BankAccount> load(BankLogin bankLogin) {
        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, LOGIN+" = "+bankLogin.getId(), null, null, null, null);
        Vector<BankAccount> accounts = new Vector<>();
        while (cursor.moveToNext()){
            long id = 0;
            String number = null, currency = null;
            for (int index=0; index<cursor.getColumnCount();index++){
                String name = cursor.getColumnName(index);
                switch (name){
                    case KEY:
                        id = cursor.getInt(index);
                        break;
                    case NUMBER:
                        number = cursor.getString(index);
                        break;
                    case CURRENCY:
                        currency = cursor.getString(index);
                        break;
                }
            }
            BankAccount account = new BankAccount(bankLogin, number, currency);
            account.id = id;
            accounts.add(account);
        }
        return accounts;
    }

    @Override
    /**
     * gibt eine einfache Textrepräsentation
     */
    public String toString() {
        return "Konto " + accountNumber + ": "+currency+" / IBAN: "+IBAN();
    }

    public void saveToDb() {
        Globals.d("Saving "+this);
        ContentValues values = new ContentValues();
        values.put(NUMBER,this.accountNumber);
        values.put(CURRENCY,this.currency);
        values.put(LOGIN,bankLogin.getId());
        Globals.writableDatabase().insert(TABLE_NAME, null, values);
    }

    public GVRKUms fetchNewTransactions() throws ParserConfigurationException, TransformerException, SAXException, IOException {
        Properties props = new Properties();
        props.put("my.number", accountNumber);
        props.put("my.blz", bankLogin.getInstitute().blz);
        props.put("my.bic", bankLogin.getInstitute().bic);
        props.put("my.iban", IBAN());
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
}

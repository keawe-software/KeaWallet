package de.keawe.keawallet.objects;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.kapott.hbci.manager.HBCIHandler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.keawe.keawallet.objects.database.BankAccount;
import de.keawe.keawallet.objects.database.BankLogin;
import de.keawe.keawallet.objects.database.Category;
import de.keawe.keawallet.objects.database.Participant;
import de.keawe.keawallet.objects.database.Settings;
import de.keawe.keawallet.objects.database.Transaction;
import de.keawe.keawallet.objects.database.Text;

public class Globals {

    public static final String APP_NAME = "KeaWallet";
    public static final String ENCRYPTION = "ENCRYPTION";
    public static Context appContext = null;
    public static HBCIHandler hbciHandler = null;
    public static CreditInstitute currentInstitute = null;
    public static SecretKeySpec encryption_key = null;
    private final static int ENCRYPTION_KEY_LENGTH = 32;
    private final static String ENCRYPTION_ALGORITHM = "AES";
    public static final int DB_VERSION = 4;

    public static void d(Object o) {
        Log.d(APP_NAME,o.toString());
    }

    public static void w(Object o) {
        Log.w(APP_NAME,o.toString());
    }

    public static Calendar firstOf(Calendar month) {
        Calendar date = (Calendar) month.clone();
        date.set(Calendar.DAY_OF_MONTH,1);
        date.set(Calendar.HOUR,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        return date;
    }

    public static class DBHelper extends SQLiteOpenHelper{

        public DBHelper() {
            super(Globals.context(), Globals.APP_NAME, null, Globals.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(BankLogin.TABLE_CREATION);
            sqLiteDatabase.execSQL(Settings.TABLE_CREATION);
            sqLiteDatabase.execSQL(BankAccount.TABLE_CREATION);
            sqLiteDatabase.execSQL(Transaction.TABLE_CREATION);
            sqLiteDatabase.execSQL(Text.TABLE_CREATION);
            sqLiteDatabase.execSQL(Participant.TABLE_CREATION);
            sqLiteDatabase.execSQL(Category.TABLE_CREATION);
            sqLiteDatabase.execSQL(Category.preset());
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Globals.d("Updating database:");
            for (int version = oldVersion+1; version<=newVersion; version++) {
                String sql = Transaction.getUpdate(version);
                if (sql != null) {
                    Globals.d(sql);
                    sqLiteDatabase.execSQL(sql);
                }
            }
        }
    }


    public static Context context(){
        if (appContext==null) throw new NullPointerException("Please call Globals.setContext(...) first");
        return appContext;
    }
    /**
     * holt einen String-WErt aus dem Ressource-Bundle der App
     * @param resId
     * @return
     */
    public static String string(Integer resId) {
        if (resId==null) return null;
        return context().getResources().getString(resId);
    }

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes ==null) return null;
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static byte[] encrypt(String s) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, encryption_key);
        return cipher.doFinal(s.getBytes());
    }

    public static String decrypt(byte[] encryptedBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, encryption_key);
        return new String(cipher.doFinal(encryptedBytes));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] hash(String s) {
        return hash(s.getBytes());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] hash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA256");
            digest.update(bytes);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setEncryptionKey(byte[] key) {
        byte[] keyBytes = new byte[ENCRYPTION_KEY_LENGTH];
        for (int i=0; i<ENCRYPTION_KEY_LENGTH; i++) keyBytes[i] = key[i];
        encryption_key = new SecretKeySpec(keyBytes, "AES");
    }

    public static void setAppcontext(Context applicationContext) {
        appContext = applicationContext;
    }

    public static SQLiteDatabase writableDatabase(){
        return new DBHelper().getWritableDatabase();
    }

    public static SQLiteDatabase readableDatabase(){
        return new DBHelper().getReadableDatabase();
    }

}

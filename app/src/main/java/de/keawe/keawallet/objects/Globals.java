package de.keawe.keawallet.objects;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.kapott.hbci.manager.HBCIHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.keawe.keawallet.objects.database.BankLogin;
import de.keawe.keawallet.objects.database.Settings;

public class Globals {

    public static final String APP_NAME = "KeaWallet";
    public static final String ENCRYPTION = "ENCRYPTION";
    public static Context appContext = null;
    public static HBCIHandler hbciHandler = null;
    public static CreditInstitute currentInstitute = null;
    public static SecretKeySpec encryption_key = null;
    private final static int ENCRYPTION_KEY_LENGTH = 32;
    private final static String ENCRYPTION_ALGORITHM = "AES";
    public static final int DB_VERSION = 1;

    public static class DBHelper extends SQLiteOpenHelper{

        public DBHelper() {
            super(Globals.context(), Globals.APP_NAME, null, Globals.DB_VERSION);
            System.out.println("Creating DBHelper. Should invoke onCreate");
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(BankLogin.TABLE_CREATION);
            sqLiteDatabase.execSQL(Settings.TABLE_CREATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

        public void insert(String table, ContentValues values) {
            SQLiteDatabase db = this.getWritableDatabase();
            System.out.println("Database handle: "+db);
            db.insert(table,null,values);
        }

        public Cursor query(String tableName, String[] columns, String select, String[] args) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.query(tableName,columns,select,args,null,null,null);
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
        Log.d(Globals.ENCRYPTION,"Trying to encrypt "+s);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, encryption_key);
        return cipher.doFinal(s.getBytes());
    }

    public static String decrypt(byte[] encryptedBytes) throws Exception {
        Log.d(Globals.ENCRYPTION,"Trying to decrypt 0x"+Globals.byteArrayToHexString(encryptedBytes));
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, encryption_key);
        return new String(cipher.doFinal(encryptedBytes));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] hash(String s) {
        byte[] bytes = s.getBytes();
        Log.d(Globals.ENCRYPTION,"Hash of "+s+" is 0x"+byteArrayToHexString(bytes));
        return hash(bytes);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static byte[] hash(byte[] bytes) {
        try {
            Log.d(Globals.ENCRYPTION,"Hashing 0x"+byteArrayToHexString(bytes));
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

    public static DBHelper database(){
        return new DBHelper();
    }
}

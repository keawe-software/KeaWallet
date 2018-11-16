package de.keawe.keawallet.objects.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.keawe.keawallet.objects.AccountSetupListener;
import de.keawe.keawallet.objects.Globals;
import de.keawe.keawallet.objects.HBCIProperties;
import de.keawe.keawallet.objects.PinTanPass;
import de.keawe.keawallet.R;
import de.keawe.keawallet.objects.CreditInstitute;
import de.keawe.keawallet.objects.overrides.AndroidHBCIHandler;

/**
 * Klasse für Logindaten zum Zugang zu einer Bank
 * Created by srichter on 19.08.15.
 */
public class BankLogin extends HBCICallbackConsole  {

    private static final String COUNTRY = "DE";
    private static final String FILTER = "Base64";
    private static final String SECMECH = "997";
    private Vector<BankAccount> accounts = null;
    private String login;
    private String pin;
    private Long id = null;
    private CreditInstitute institute = null;

    /**
     * neuen Bank-Login erzuegen und mit Daten füllen
     * @param institute
     * @param login
     * @param pin
     */
    public BankLogin(CreditInstitute institute, String login, String pin) {
        if (institute == null) {
            throw new InvalidParameterException(Globals.string(R.string.invalid_credit_institute));
        }
        if (login == null || login.isEmpty()) {
            throw new InvalidParameterException(Globals.string(R.string.user_must_not_be_empty));
        }
        if (pin == null || pin.isEmpty()) {
            throw new InvalidParameterException(Globals.string(R.string.secret_must_not_be_empty));
        }
        this.institute = institute;
        this.accounts = new Vector<BankAccount>();
        this.login = login;
        this.pin = pin;
    }

    @Override
    /**
     * liefert eine einfache String-Repräsentation der vorliegenden Instanz
     */
    public String toString() {
        return "BankLogin: id=" + id + ", Institute=" + institute+", login="+login+", pin="+pin.substring(0,2)+"xxxx";
    }

    /**
     * setzt die von der HBCI-Library zwischengespeicherten Kontodaten zurück
     */
    private void cleanup() {
        try {
            Globals.hbciHandler.reset();
            Globals.hbciHandler.close();
        } catch (Exception e) {
        }
        Globals.hbciHandler = null;

        try {
            HBCIUtils.done();
        } catch (Exception e) {
        }
    }

    /**
     * sucht nach Konten, die zum vorliegenden Login gehören
     * @param listener
     */
    public Vector<BankAccount> findAccounts(AccountSetupListener listener) {
        try {
            initHBCIHandler(); // ggf. Erzuegen eines neuen Handlers
            listener.notifyHandlerCreated(true);
        } catch (Exception e) { // Erzuegen des Handlers fehlgeschlagen?
            e.printStackTrace();
            listener.notifyHandlerCreated(false);
            listener.notifyLoggedIn(false);
            listener.notifyJobDone(false);
            return null;
        }

        listener.notifyLoggedIn(true); // beim Erzuegen des Handlers erfolgt auch schon die Anmeldung an der Bank. D.h. erfolgreich erzeugter Handler == erfolgreicher Login
        Konto[] accs = Globals.hbciHandler.getPassport().getAccounts();  // Das Passport-Element enthält zu diesem Zeitpunkt schon eine Liste der gefundenen Konten
        try {
            for (Konto acc : accs) { // gefundene Konten inspizieren

                Properties props = new Properties();
                props.put("my.number", acc.number);
                props.put("my.blz", institute.blz);
                HBCIJobResult result = executeJob("SaldoReq", props); // Saldo des aktuellen Kontos abfragen
                if (result.isOK()) {
                    Properties data = result.getResultData();

                    String accountNumber = data.get("content.KTV.number").toString(); // Kontonummer

                    String saldoString = data.get("content.booked.CreditDebit").equals("D") ? "-" : ""; // Soll oder Haben?
                    String currency = data.get("content.curr").toString(); // Währung
                    saldoString += data.get("content.booked.BTG.value"); // Kontostand
                    listener.notifyAccount(accountNumber,saldoString,currency);
                    BankAccount bankAccount = new BankAccount(this, accountNumber, currency); // neues Konto mit den getesteten Daten anlegen
                    accounts.add(bankAccount); // neues Konto zu den Konten des Logins hinzufügen (aber nicht speichern)
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (accounts.isEmpty()) { // falls keine Konten gefunden wurden
            listener.notifyJobDone(false); // aktiviert den Zurück-Knopf
        } else {
            listener.notifyFoundAccounts(accounts);
            listener.notifyJobDone(true); // aktiviert die Auswahlliste und den Speichern-Knopf
        }
        return accounts;
    }

    @Override
    /**
     * Die Status-Methode gehört zur HBCICallbackConsole und wird verwendet, um zwischenzeitlich Status auszugeben.
     * Hier ist sie leer, um unnötige Logmeldungen zu unterbinden.
     */
    public synchronized void status(HBCIPassport passport, int statusTag, Object[] o) {
        // ENJOY THE SILENCE
    }

    //

    /**
     * modifizierte callback-methode, die daten-anfragen "automatisch" beantwortet
     * callback gehört zur HBCICallbackConsole und wird genutzt um durch die HBCI-Bibliothek abgefragte WErte zu liefern
     * @param passport
     * @param reason
     * @param msg
     * @param datatype
     * @param retData
     */
    public synchronized void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {
        switch (reason) {
            case NEED_CHIPCARD:
                System.out.println(HBCIUtilsInternal.getLocMsg("CALLB_NEED_CHIPCARD"));
                break;
            case NEED_HARDPIN:
                System.out.println(HBCIUtilsInternal.getLocMsg("CALLB_NEED_HARDPIN"));
                break;


            case NEED_PT_SECMECH:
                retData.replace(0, retData.length(), SECMECH);
                break;

            case NEED_PT_PIN:
                retData.replace(0, retData.length(), pin);
                break;

            case NEED_COUNTRY:
                retData.replace(0, retData.length(), COUNTRY);
                break;
            case NEED_BLZ:
                retData.replace(0, retData.length(), institute.blz);
                break;
            case NEED_HOST:
                retData.replace(0, retData.length(), institute.bic_url());
                break;
            case NEED_PORT:
                retData.replace(0, retData.length(), "" + institute.port());
                break;
            case NEED_FILTER:
                retData.replace(0, retData.length(), FILTER);
                break;
            case NEED_USERID:
                retData.replace(0, retData.length(), login);
                break;
            case NEED_CUSTOMERID:
                retData.replace(0, retData.length(), login);
                break;

            case NEED_NEW_INST_KEYS_ACK:
                retData.replace(0, retData.length(), "");
                break;
            case HAVE_NEW_MY_KEYS:
                System.out.println("please restart batch process");
                break;

            case HAVE_INST_MSG:
                HBCIUtils.log(msg, HBCIUtils.LOG_INFO);
                break;

            case NEED_CONNECTION:
            case CLOSE_CONNECTION:
                break;
        }
    }

    /**
     * liefert den LoginBenutzername des vorliegenden BankLogins
     * @return
     */
    public String getLogin() {
        return login;
    }

    /**
     * liefert Passwort/Pin des vorliegenden BankLogins
     * @return
     */
    public String getPin() {
        return pin;
    }

    /**
     * liefert das Institut des vorliegnenden BankLogins
     * @return
     */
    public CreditInstitute getInstitute() {
        return institute;
    }

    /**
     * liefert die Datenbank-Id des vorliegenden BankLogins
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * initialisiert ggf. den HBCIHandler zur Nutzung mit Online-Abfragen
     */
    private void initHBCIHandler() throws IOException, TransformerException, SAXException, ParserConfigurationException {
        if (Globals.currentInstitute != institute) {
            cleanup();
        }
        if (Globals.hbciHandler == null) {
            HBCIUtils.init(new HBCIProperties(), this);
            Globals.hbciHandler = new AndroidHBCIHandler(institute.getHBCIVersion(), new PinTanPass(),Globals.context().getAssets());
            Globals.currentInstitute = institute;
        }
    }

    /**
     * führt einen HBCIJob aus und liefert dessen Ergebis zurück
     * @param jobName der Name des HBCI-Jobs
     * @param props weitere Settings für den JOb
     * @return das HBCIJobResult (Ergebnis) des ausgeführten Jobs
     */
    public HBCIJobResult executeJob(String jobName, Properties props) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        initHBCIHandler(); // HBCI-Handler initialisieren, wenn er nicht schon bereit ist
        HBCIJob job = Globals.hbciHandler.newJob(jobName); // neuen Job anlegen
        for (Object key : props.keySet()) { // Parameter des Jobs setzen
            job.setParam(key.toString(), props.get(key).toString());
        }
        job.addToQueue(); // Job zur Ausführung vormerken
        Globals.hbciHandler.execute(); // vorgemerkte Jobs ausführen
        return job.getJobResult();
    }

    /**
     * setzt das Institut des vorliegenden BankLogins
     * @param institute
     */
    public void setInstitute(CreditInstitute institute) {
        this.institute = institute;

    }

    /**
     * Setzt die Login-Benutzerkennung des vorliegenden BankLogins
     * @param login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * setzt das Login-Kennwort für den vorleigenden BankLogin
     * @param pin
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    public void saveToDb() {
        System.out.println("BankLogin.saveToDb not implemented");
    }
}

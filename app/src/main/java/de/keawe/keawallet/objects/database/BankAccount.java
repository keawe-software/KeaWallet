package de.keawe.keawallet.objects.database;

/**
 * Diese Klasse repräsentiert ein Bank(unter)konto
 * Created by srichter on 07.09.15.
 */
public class BankAccount {

    public static final String AID = "aid";
    private static final String NUMBER = "number";
    private static final String CURRENCY = "currency";
    // Datenfelder
    private String accountNumber;
    private BankLogin bankLogin;
    private String currency;

    public BankAccount() {
    }

    public BankAccount(BankLogin bankLogin, String accountNumber, String currency) {
        this.bankLogin = bankLogin;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }

    @Override
    /**
     * gibt eine einfache Textrepräsentation
     */
    public String toString() {
        return "Konto " + accountNumber + ": "+currency;
    }

}

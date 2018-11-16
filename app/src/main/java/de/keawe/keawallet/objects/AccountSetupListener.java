package de.keawe.keawallet.objects;

import java.util.Vector;

import de.keawe.keawallet.objects.database.BankAccount;

public interface AccountSetupListener {
    void notifyHandlerCreated(boolean success);

    void notifyLoggedIn(boolean success);

    void notifyJobDone(boolean success);

    void notifyAccount(String accountNumber, String saldoString, String currency);

    void notifyFoundAccounts(Vector<BankAccount> accounts);
}

package de.keawe.keawallet.objects;

import android.content.Context;

import org.kapott.hbci.manager.HBCIHandler;

public class Globals {

    private static Context appContext = null;
    public static HBCIHandler hbciHandler = null;
    public static CreditInstitute currentInstitute = null;
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
}

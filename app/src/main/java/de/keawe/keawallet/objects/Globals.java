package de.keawe.keawallet.objects;

import android.content.Context;

public class Globals {

    private static Context appContext;

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

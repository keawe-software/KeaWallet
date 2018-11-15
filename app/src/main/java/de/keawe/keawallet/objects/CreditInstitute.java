package de.keawe.keawallet.objects;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CreditInstitute {
    public static CreditInstitute[] getList(AssetManager assets) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open("blz.properties")));
        String line = null;
        while ((line = reader.readLine()) != null) {
            Log.d("Institute", line);
        }
        reader.close();
        return null;
    }
}

package de.keawe.keawallet.objects;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CreditInstitute {
    public CreditInstitute(String blz,String name, String location, String bic, String hbci_url){

    }

    public static CreditInstitute[] getList(AssetManager assets) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open("blz.properties")));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=",2);
            String blz = parts[0];
            parts = parts[1].split("\\|");
            if (blz.equals("12096597"));
            for (String d:parts) {
                Log.d("Institute", d);
            }
            break;
            /*String name=parts[0];
            String location=parts[1];
            String bic=parts[2];
            String hbci_url=parts[5];*/

        }
        reader.close();
        return null;
    }
}

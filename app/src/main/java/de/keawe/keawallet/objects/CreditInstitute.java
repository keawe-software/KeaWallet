package de.keawe.keawallet.objects;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class CreditInstitute {
    public static final boolean HBCI_ONLY = true;
    public final String bic;
    public final String blz;
    private final String location;
    private final String name;
    private final String url;
    private final String version;

    public CreditInstitute(String blz,String name, String location, String bic, String hbciUrl,String hbciVersion){
        this.name=name;
        this.blz=blz;
        this.location=location;
        this.bic=bic;
        this.url=hbciUrl;
        this.version=hbciVersion;
    }

    public static Vector<CreditInstitute> getList() throws IOException {
        return getList(false);
    }

    public static Vector<CreditInstitute> getList(boolean hbciOnly) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Globals.context().getAssets().open("blz.properties")));
        Vector<CreditInstitute> institutes = new Vector<CreditInstitute>();

        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=", 2);
            String blz = parts[0].trim();
            parts = parts[1].split("\\|", -1);
            String name = parts[0];
            String location = parts[1];
            String bic = parts[2];
            String hbciUrl = parts[5];
            String hbciVersion = parts[7];
            if (!hbciOnly || !hbciUrl.isEmpty()) institutes.add(new CreditInstitute(blz, name, location, bic, hbciUrl, hbciVersion));
        }
        reader.close();
        Collections.sort(institutes, CreditInstitute.comparator());
        return institutes;
    }

    public static CreditInstitute get(String targetBlz) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Globals.context().getAssets().open("blz.properties")));

        CreditInstitute result = null;
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=", 2);
            String blz = parts[0].trim();
            if (blz.equals(targetBlz)) {
               parts = parts[1].split("\\|", -1);
                String name = parts[0];
                String location = parts[1];
                String bic = parts[2];
                String hbciUrl = parts[5];
                String hbciVersion = parts[7];
                result = new CreditInstitute(blz, name, location, bic, hbciUrl, hbciVersion);
            }
        }
        reader.close();
        return result;
    }

    private static Comparator<? super CreditInstitute> comparator() {
        return new Comparator<CreditInstitute>() {
            @Override
            public int compare(CreditInstitute lhs, CreditInstitute rhs) {
                return (lhs.name+lhs.blz).compareTo(rhs.name+rhs.blz); // sortby name and blz
            }
        };
    }

    @Override
    public String toString() {
        if (this.blz == null) return this.name;
        return '('+this.blz+") "+this.name;
    }

    public String bic_url() {
        return this.url;
    }

    public boolean hasUrl() {
        return this.url != null;
    }

    public int port() {
        return url.startsWith("https:")?443:80;
    }

    public String getHBCIVersion() {
        return version;
    }

    public String name() {
        return name;
    }
}

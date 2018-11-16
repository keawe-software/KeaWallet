package de.keawe.keawallet.objects.overrides;

import android.content.res.AssetManager;

import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidArgumentException;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIKernelImpl;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportInternal;

import java.util.Hashtable;
import java.util.Properties;

public class AndroidHBCIHandler extends HBCIHandler {

    public AndroidHBCIHandler(String hbciversion, HBCIPassport passport, AssetManager assets) {
        try {
            if (passport==null)
                throw new InvalidArgumentException(HBCIUtilsInternal.getLocMsg("EXCMSG_PASSPORT_NULL"));

            if (hbciversion==null) {
                hbciversion=passport.getHBCIVersion();
            }
            if (hbciversion.length()==0)
                throw new InvalidArgumentException(HBCIUtilsInternal.getLocMsg("EXCMSG_NO_HBCIVERSION"));

            this.kernel=new AndroidHBCIKernelImpl(this,hbciversion,assets);

            this.passport=(HBCIPassportInternal)passport;
            this.passport.setParentHandlerData(this);

            registerInstitute();
            registerUser();

            if (!passport.getHBCIVersion().equals(hbciversion)) {
                this.passport.setHBCIVersion(hbciversion);
                this.passport.saveChanges();
            }

            dialogs=new Hashtable<String, HBCIDialog>();
        } catch (Exception e) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_CANT_CREATE_HANDLE"),e);
        }

        // wenn in den UPD noch keine SEPA- und TAN-Medien-Informationen ueber die Konten enthalten
        // sind, versuchen wir, diese zu holen
        Properties upd=passport.getUPD();
        if (upd!=null && !upd.containsKey("_fetchedMetaInfo"))
        {
            // wir haben UPD, in denen aber nicht "_fetchedMetaInfo" drinsteht
            updateMetaInfo();
        }
    }
}

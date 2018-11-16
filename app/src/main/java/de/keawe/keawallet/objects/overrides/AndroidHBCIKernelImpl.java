package de.keawe.keawallet.objects.overrides;

import android.content.res.AssetManager;

import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.manager.HBCIKernelImpl;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.manager.IHandlerData;
import org.kapott.hbci.manager.MsgGen;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AndroidHBCIKernelImpl extends HBCIKernelImpl {
    public AndroidHBCIKernelImpl(IHandlerData parentHandlerData, String hbciversion, AssetManager assets) throws IOException {
        this.parentHandlerData=parentHandlerData;
        this.hbciversion=hbciversion;

        String filename="hbci-"+hbciversion+".xml";
        System.out.println("Trying to load "+filename);
        InputStream syntaxStream=assets.open(filename);
        if (syntaxStream==null) throw new InvalidUserDataException(HBCIUtilsInternal.getLocMsg("EXCMSG_KRNL_CANTLOAD_SYN",filename));

        try {
            gen=new MsgGen(syntaxStream);
            currentMsgName=null;
        } catch (Exception e) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_MSGGEN_INIT"),e);
        }
    }

}

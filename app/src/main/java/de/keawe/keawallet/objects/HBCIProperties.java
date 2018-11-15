package de.keawe.keawallet.objects;

import java.util.Properties;

/**
 * Grundlegende Eigenschaften, die von der HBCI-Bibliothek verwendet werden.
 * Created by srichter on 25.08.15.
 */
public class HBCIProperties extends Properties {
	public HBCIProperties(){
		super();

		setProperty("client.passport.default", "PinTan");
		setProperty("log.loglevel.default", "1");
    }
}

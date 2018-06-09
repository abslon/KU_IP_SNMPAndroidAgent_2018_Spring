package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class Opaque extends OctetString
{
    public Opaque() {
        super();
    }

    public Opaque(byte[] bytes) {
        super(bytes);
    }

    public Opaque(String string) {
        super(string);
    }

    public int getSyntax() {
        return BER.OPAQUE;
    }

    public void encodeBER(OutputStream outputStream) throws IOException
    {
        BER.encodeString(outputStream, BER.OPAQUE, getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        byte[] v = BER.decodeString(inputStream, type);
        if (type.getValue() != (BER.ASN_APPLICATION | 0x04)) {
            throw new IOException("Wrong type encountered when decoding OctetString: "+
                    type.getValue());
        }
        setValue(v);
    }

    public void setValue(OctetString value)
    {
        this.setValue(new byte[0]);
        append(value);
    }

    public Object clone() {
        return new Opaque(super.getValue());
    }
}

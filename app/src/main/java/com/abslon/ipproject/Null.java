package com.abslon.ipproject;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;

import java.io.OutputStream;

public class Null implements Variable
{
    private int syntax = BER.ASN_NULL;

    public Null() {
    }

    public int getSyntax() {
        return syntax;
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
        BER.MutableByte type = new BER.MutableByte();
        BER.decodeNull(inputStream, type);
        this.syntax = type.getValue() & 0xFF;
    }

    public int getBERLength() {
        return 2;
    }

    public boolean equals(Object o) {
        return (o instanceof Null) && (((Null) o).getSyntax() == getSyntax());
    }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException {
        BER.encodeHeader(outputStream, (byte) getSyntax(), 0);
    }

    public int getBERPayloadLength() {
        return getBERLength();
    }
}

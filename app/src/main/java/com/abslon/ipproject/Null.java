package com.abslon.ipproject;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;

import java.io.OutputStream;

public class Null implements Variable
{
    private int syntax = BER.ASN_NULL;

    public static final Null noSuchObject = new Null(BER.NOSUCHOBJECT);
    public static final Null noSuchInstance = new Null(BER.NOSUCHINSTANCE);
    public static final Null endOfMibView = new Null(BER.ENDOFMIBVIEW);
    public static final Null instance = new Null(BER.ASN_NULL);

    public Null() {
    }

    public Null(int exceptionSyntax) {
        setSyntax(exceptionSyntax);
    }

    public int getSyntax() {
        return syntax;
    }

    public void setSyntax(int syntax) {
        if ((syntax != BER.ASN_NULL) && (syntax != BER.NOSUCHOBJECT) &&
            (syntax != BER.NOSUCHINSTANCE) && (syntax != BER.ENDOFMIBVIEW))
        {
            throw new IllegalArgumentException("Syntax " + syntax + " is incompatible with Null type");
        }
        else this.syntax = syntax;
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        BER.decodeNull(inputStream, type);
        this.syntax = type.getValue() & 0xFF;
    }

    public int getBERLength() { return 2; }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeHeader(outputStream, (byte) getSyntax(), 0);
    }

    public int getBERPayloadLength() {
        return getBERLength();
    }

    public boolean equals(Object o)
    {
        return (o instanceof Null) && (((Null) o).getSyntax() == getSyntax());
    }

    public int compareTo(Variable o)
    {
        return (getSyntax() - ((Null)o).getSyntax());
    }

    public Object clone() {
        return new Null(this.syntax);
    }

    public boolean isException()
    {
        switch (syntax)
        {
            case BER.NOSUCHOBJECT:
            case BER.NOSUCHINSTANCE:
            case BER.ENDOFMIBVIEW:
                return true;
        }
        return false;
    }

    public String toString()
    {
        switch (syntax)
        {
            case BER.NOSUCHOBJECT: {
                return "NULL : NO SUCH OBJECT";
            }
            case BER.NOSUCHINSTANCE:{
                return "NULL : NO SUCH INSTANCE";
            }
            case BER.ENDOFMIBVIEW:{
                return "NULL : END OF MIB VIEW";
            }
        }
        return "NULL";
    }
}

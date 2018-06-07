package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class Int32 implements Variable
{
    private int value;

    public final void setValue(int value) {
        this.value = value;
    }
    public final int getValue() {
        return value;
    }

    public Int32()
    {

    }

    public Int32(int value)
    {
        setValue(value);
    }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeInteger(outputStream, BER.INTEGER, value);
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        int newValue = BER.decodeInteger(inputStream, type);
        if (type.getValue() != BER.INTEGER) {
            throw new IOException("Wrong type encountered when decoding Counter: "+type.getValue());
        }
        setValue(newValue);
    }

    public int getBERLength() {
        if ((value <   0x80) &&
                (value >= -0x80)) {
            return 3;
        }
        else if ((value <   0x8000) &&
                (value >= -0x8000)) {
            return 4;
        }
        else if ((value <   0x800000) &&
                (value >= -0x800000)) {
            return 5;
        }
        return 6;
    }

    public int getBERPayloadLength() {
        return getBERLength();
    }

    public boolean equals(Object o) {
        return (o instanceof Int32) && (((Int32) o).value == value);
    }
}

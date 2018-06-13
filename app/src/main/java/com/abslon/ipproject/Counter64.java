package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class Counter64 implements Variable
{
    // 64bit Counter 값
    private long value = 0;

    // 생성자
    public Counter64() { }

    public Counter64(long value) {
        setValue(value);
    }


    // BERSerializable 인터페이스
    public int getBERLength()
    {
        if (value < 0L) return 11;
        else if (value < 0x80000000L)
        {
            if (value < 0x8000L) return (value < 0x80L) ? 3 : 4;
            else return (value < 0x800000L) ? 5 : 6;
        }
        else if (value < 0x800000000000L) return (value < 0x8000000000L) ? 7 : 8;
        else return (value < 0x80000000000000L) ? 9 : 10;
    }

    public int getBERPayloadLength() { return getBERLength() - 2; }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeUnsignedInt64(outputStream, BER.COUNTER64, value);
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        long newValue = BER.decodeUnsignedInt64(inputStream, type);
        if (type.getValue() != BER.COUNTER64)
        {
            throw new IOException("Wrong type encountered when decoding Counter64: " + type.getValue());
        }
        setValue(newValue);
    }

    // Variable 인터페이스
    public int compareTo(Variable o)
    {
        long other = ((Counter64) o).value;
        if (getValue() > other) return 1;
        else if(getValue() < other) return -1;
        else return 0;
    }

    public boolean equals(Object o) { return (o instanceof Counter64) && ((Counter64) o).value == value; }

    public Object clone() {
        return new Counter64(value);
    }

    public int getSyntax() {
        return BER.COUNTER64;
    }

    public boolean isException() { return false; }

    public String toString() { return Long.toString(value); }

    // getsetter
    public void setValue(String value) {
        this.value = Long.parseLong(value);
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}

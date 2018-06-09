package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class UnsignedInt32 implements Variable
{
    // unsigned int 값
    protected long value = 0;

    // 생성자
    public UnsignedInt32() { }

    public UnsignedInt32(long value) {
        setValue(value);
    }

    public UnsignedInt32(int signedIntValue) {
        setValue(signedIntValue & 0xFFFFFFFFL);
    }

    // BERSerializable 인터페이스
    // value의 값에 따라 3 ~ 7까지의 길이를 가질 수 있다.
    public int getBERLength()
    {
        if (value < 0x80L)
            return 3;

        else if (value < 0x8000L)
            return 4;

        else if (value < 0x800000L)
            return 5;

        else if (value < 0x80000000L)
            return 6;

        else return 7;
    }
    // uint 는 payload가 없음
    public int getBERPayloadLength() { return getBERLength();}

    // BER encoding
    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeUnsignedInteger(outputStream, BER.GAUGE, value);
    }

    // BER decoding
    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        long newValue = BER.decodeUnsignedInteger(inputStream, type);
        if (type.getValue() != BER.GAUGE)
        {
            throw new IOException("Gauge(unsigned int)가 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        setValue(newValue);
    }

    // Variable 인터페이스
    public boolean equals(Object o)
    {
        if (o instanceof UnsignedInt32)
            return (((UnsignedInt32)o).value == value);

        else return false;
    }

    public int compareTo(Variable o)
    {
        long diff = (value - ((UnsignedInt32)o).getValue());
        if (diff < 0)
            return -1;

        else if (diff > 0)
            return 1;

        else return 0;
    }

    public Object clone() {
        return new UnsignedInt32(value);
    }

    public int getSyntax() {
        return BER.GAUGE32;
    }

    public boolean isException() { return false; }

    public String toString() { return Long.toString(value); }

    // getsetter
    public long getValue() { return value; }

    public void setValue(String value) {
        setValue(Long.parseLong(value));
    }

    public void setValue(long value)
    {
        if ((value < 0) || (value > 4294967295L))
            throw new IllegalArgumentException("unsigned int의 범위를 넘어선 값입니다.");

        else this.value = value;
    }
}

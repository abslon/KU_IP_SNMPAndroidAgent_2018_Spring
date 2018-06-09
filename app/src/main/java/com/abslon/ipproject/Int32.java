package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class Int32 implements Variable
{
    // int 변수 값
    private int value;

    // 생성자
    public Int32() { }

    public Int32(int value) { setValue(value); }

    // BERSerializable
    // int 값의 크기에 따라 3 ~ 6까지의 값을 가질 수 있음.
    public int getBERLength()
    {
        if ((value < 0x80) && (value >= -0x80))
            return 3;

        else if ((value < 0x8000) && (value >= -0x8000))
            return 4;

        else if ((value < 0x800000) && (value >= -0x800000))
            return 5;

        else return 6;
    }

    // int는 payload가 따로 있는게 아닌 type이므로 BER 길이와 같음.
    public int getBERPayloadLength() { return getBERLength(); }

    // BER encoding
    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeInteger(outputStream, BER.INTEGER, value);
    }

    // BER decoding
    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        int newValue = BER.decodeInteger(inputStream, type);
        if (type.getValue() != BER.INTEGER)
        {
            throw new IOException("int가 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        setValue(newValue);
    }

    // Variable 인터페이스
    public boolean equals(Object o) { return (o instanceof Int32) && (((Int32) o).value == value); }

    public int compareTo(Variable o) { return value - ((Int32)o).value; }

    public Object clone() { return new Int32(value); }

    public int getSyntax() {
        return BER.ASN_INTEGER;
    }

    public boolean isException() {
        return false;
    }

    public String toString() { return Integer.toString(value); }

    // getsetter
    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }
}

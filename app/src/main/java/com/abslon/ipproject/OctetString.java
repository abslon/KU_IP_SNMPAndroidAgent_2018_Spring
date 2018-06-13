package com.abslon.ipproject;

import org.snmp4j.asn1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class OctetString implements Variable
{
    // OctetString의 byte array
    private byte[] value = new byte[0];

    // 생성자
    public OctetString() { }

    public OctetString(byte[] rawValue) { this(rawValue, 0, rawValue.length); }

    public OctetString(byte[] rawValue, int offset, int length)
    {
        value = new byte[length];
        System.arraycopy(rawValue, offset, value, 0, length);
    }

    public OctetString(String stringValue) { this.value = stringValue.getBytes(); }

    // String operations
    public void append(byte[] bytes)
    {
        byte[] newValue = new byte[value.length + bytes.length];
        System.arraycopy(value, 0, newValue, 0, value.length);
        System.arraycopy(bytes, 0, newValue, value.length, bytes.length);
        value = newValue;
    }

    public void append(OctetString octetString) { append(octetString.getValue()); }

    public void append(String string) { append(string.getBytes()); }

    public void clear() { value = new byte[0]; }

    public final int length() { return value.length; }

    // BERSerializable 인터페이스
    public int getBERLength() { return value.length + BER.getBERLengthOfLength(value.length) + 1; }

    public int getBERPayloadLength() { return value.length; }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeString(outputStream, BER.OCTETSTRING, getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        byte[] v = BER.decodeString(inputStream, type);
        if (type.getValue() != BER.OCTETSTRING)
        {
            throw new IOException("OctetString이 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        setValue(v);
    }

    // Variable 인터페이스
    public int compareTo(Variable o)
    {
        if (o instanceof OctetString)
        {
            OctetString other = (OctetString)o;
            int maxlen = Math.min(value.length, other.value.length);
            for (int i = 0; i < maxlen; i++)
            {
                if (value[i] != other.value[i])
                {
                    if ((value[i] & 0xFF) < (other.value[i] & 0xFF)) return -1;
                    else return 1;
                }
            }
            return (value.length - other.value.length);
        }
        else throw new ClassCastException(o.getClass().getName());
    }

    public boolean equals(Object o)
    {
        if (o instanceof OctetString)
        {
            OctetString other = (OctetString)o;
            return Arrays.equals(value, other.value);
        }
        else return false;
    }

    public Object clone() { return new OctetString(value); }

    public int getSyntax() { return BER.ASN_OCTET_STR; }

    public boolean isException() { return false; }

    public String toString() { return new String(value); }

    // getsetter
    public void setValue(String value) { setValue(value.getBytes()); }

    public void setValue(byte[] value)
    {
        if (value == null)
            throw new IllegalArgumentException("OctetString must not be assigned a null value");

        else this.value = value;
    }

    public byte[] getValue() { return value; }
}

package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.StringTokenizer;

public class OID implements Variable
{
    // OID 값
    private int[] value = new int[0];

    // 생성자
    public OID() { }

    public OID(String OIDString) { value = parseOID(OIDString); }

    public OID(int[] rawOID) { this(rawOID, 0, rawOID.length); }

    public OID(int[] rawOID, int offset, int length) { setValue(rawOID, offset, length); }

    public OID(OID other) { this(other.getValue()); }

    // 추가 기능
    public final int size() { return value.length; }

    public static int[] parseOID(String text)
    {
        StringTokenizer st = new StringTokenizer(text, ".");
        int size = st.countTokens();
        int[] value = new int[size];
        size = 0;
        StringBuffer buf = null;
        while (st.hasMoreTokens())
        {
            String t = st.nextToken();
            value[size++] = (int) Long.parseLong(t);
        }
        return value;
    }

    // BERSerializable
    public int getBERLength()
    {
        int length = BER.getOIDLength(value);
        return length + BER.getBERLengthOfLength(length) + 1;
    }

    public int getBERPayloadLength() { return BER.getOIDLength(value); }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeOID(outputStream, BER.OID, value);
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        int[] v = BER.decodeOID(inputStream, type);
        if (type.getValue() != BER.OID)
        {
            throw new IOException("OID가 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        value = v;
    }

    // Variable 인터페이스
    public boolean equals(Object obj)
    {
        if (obj instanceof OID)
        {
            OID other = (OID)obj;
            if (other.value.length != value.length)
            {
                return false;
            }
            for (int i = 0; i< value.length; i++)
            {
                if (value[i] != other.value[i])
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int Compare(int n, OID other)
    {
        for (int i = 0; i<n && i < value.length && i < other.size(); i++)
        {
            if (value[i] != other.value[i])
            {
                if ((value[i] & 0xFFFFFFFFL) < (other.value[i] & 0xFFFFFFFFL))
                {
                    return -1;
                }
                else
                {
                    return 1;
                }
            }
        }
        if (n > value.length)
        {
            return -1;
        }
        else if (n > other.size())
        {
            return 1;
        }
        return 0;
    }

    public final int compareTo(Variable o)
    {
        if (o instanceof OID)
        {
            OID other = (OID)o;
            int min = Math.min(value.length, other.value.length);
            int result = Compare(min, other);
            if (result == 0)
            {
                return (value.length - other.value.length);
            }
            return result;
        }
        throw new ClassCastException(o.getClass().getName());
    }

    public Object clone() { return new OID(value); }

    @Override
    public int getSyntax() { return BER.ASN_OBJECT_ID; }

    public boolean isException() { return false; }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(3*value.length);
        for (int i=0; i<value.length; i++) {
            if (i != 0) {
                buf.append('.');
            }
            buf.append((value[i] & 0xFFFFFFFFL));
        }
        return buf.toString();
    }

    // getsetter
    public final int[] getValue() { return value; }

    private void setValue(int[] rawOID, int offset, int length)
    {
        value = new int[length];
        System.arraycopy(rawOID, offset, value, 0, length);
    }

}

package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class OID implements Variable
{
    private static final int[] OID_NULL = new int[0];
    private int[] oidValue = OID_NULL;

    public OID(String OIDString)
    {
        oidValue = parseOID(OIDString);
    }

    // TODO
    public static int[] parseOID(String text) {
        StringTokenizer st = new StringTokenizer(text, ".", true);
        int size = st.countTokens();
        int[] value = new int[size];
        size = 0;
        StringBuffer buf = null;
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if ((buf == null) && t.startsWith("'")) {
                buf = new StringBuffer();
                t = t.substring(1);
            }
            if ((buf != null) && (t.endsWith("'"))) {
                /*
                buf.append(t.substring(0, t.length()-1));
                OID o = new OctetString(buf.toString()).toSubIndex(true);
                int[] h = value;
                value = new int[st.countTokens()+h.length+o.size()];
                System.arraycopy(h, 0, value, 0, size);
                System.arraycopy(o.getValue(), 0, value, size, o.size());
                size += o.size();
                buf = null;*/
            }
            else if (buf != null) {
                buf.append(t);
            }
            else if (!".".equals(t)) {
                value[size++] = (int) Long.parseLong(t.trim());
            }
        }
        if (size < value.length) {
            int[] h = value;
            value = new int[size];
            System.arraycopy(h, 0, value, 0, size);
        }
        return value;
    }

    @Override
    public int getBERLength()
    {
        int length = BER.getOIDLength(oidValue);
        return length + BER.getBERLengthOfLength(length) + 1;
    }

    @Override
    public int getBERPayloadLength() {
        return 0;
    }

    @Override
    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        int[] v = BER.decodeOID(inputStream, type);
        if (type.getValue() != BER.OID) {
            throw new IOException("Wrong type encountered when decoding OID: "+
                    type.getValue());
        }
        oidValue = v;
    }

    @Override
    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        BER.encodeOID(outputStream, BER.OID, oidValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OID) {
            OID other = (OID)obj;
            if (other.oidValue.length != oidValue.length) {
                return false;
            }
            for (int i=0; i<oidValue.length; i++) {
                if (oidValue[i] != other.oidValue[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}

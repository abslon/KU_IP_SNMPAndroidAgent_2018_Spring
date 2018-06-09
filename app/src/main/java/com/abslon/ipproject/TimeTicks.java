package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class TimeTicks extends UnsignedInt32
{
    private static final int[] FORMAT_FACTORS = { 24*60*60*100, 60*60*100, 60*100, 100, 1 };

    public TimeTicks() {
    }

    public TimeTicks(TimeTicks other) {
        this.value = other.value;
    }

    public TimeTicks(long value) {
        super(value);
    }

    public TimeTicks(int value) {
        super(value);
    }

    public void encodeBER(OutputStream os) throws IOException {
        BER.encodeUnsignedInteger(os, BER.TIMETICKS, super.getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws IOException {
        BER.MutableByte type = new BER.MutableByte();
        long newValue = BER.decodeUnsignedInteger(inputStream, type);
        if (type.getValue() != BER.TIMETICKS) {
            throw new IOException("Wrong type encountered when decoding TimeTicks: "+type.getValue());
        }
        setValue(newValue);
    }

    public final void setValue(String value) {
        try {
            long v = Long.parseLong(value);
            setValue(v);
        }
        catch (NumberFormatException nfe) {
            long v = 0;
            String[] num = value.split("[days :,\\.]");
            int i = 0;
            for (String n : num) {
                if (n.length()>0) {
                    long f = FORMAT_FACTORS[i++];
                    v += Long.parseLong(n)*f;
                }
            }
            setValue(v);
        }
    }
}

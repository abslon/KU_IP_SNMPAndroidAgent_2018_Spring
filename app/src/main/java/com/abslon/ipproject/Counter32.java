package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;

public class Counter32 extends UnsignedInt32
{
    // 생성자
    public Counter32() { }

    public Counter32(long value) {
        super(value);
    }


    // BERSerializable 인터페이스
    // Unsigned int와 같으나, type만 BER.Counter32로 변경. length는 동일함.
    public void encodeBER(OutputStream outputStream) throws IOException
    {
        BER.encodeUnsignedInteger(outputStream, BER.COUNTER32, super.getValue());
    }

    public void decodeBER(BERInputStream inputStream) throws IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        long newValue = BER.decodeUnsignedInteger(inputStream, type);
        if (type.getValue() != BER.COUNTER32)
        {
            throw new IOException("Counter32가 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        setValue(newValue);
    }

    // Variable 인터페이스
    public boolean equals(Object o) { return (o instanceof Counter32) && (((Counter32) o).getValue() == getValue()); }

    public Object clone() {
        return new Counter32(value);
    }

    public int getSyntax() {
        return BER.COUNTER32;
    }
}

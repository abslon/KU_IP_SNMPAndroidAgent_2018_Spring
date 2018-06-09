package com.abslon.ipproject;

import org.snmp4j.asn1.*;

public class Gauge32 extends UnsignedInt32
{
    // 생성자
    public Gauge32() { }

    public Gauge32(long value) {
        super(value);
    }

    public Gauge32(int value) {
        super(value);
    }

    // Variable 인터페이스
    public Object clone() {
        return new Gauge32(value);
    }

    public int getSyntax() {
        return BER.GAUGE32;
    }
}

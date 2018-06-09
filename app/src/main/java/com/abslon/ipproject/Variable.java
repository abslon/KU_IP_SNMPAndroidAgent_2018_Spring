package com.abslon.ipproject;

import org.snmp4j.asn1.*;

public interface Variable extends BERSerializable
{
    boolean equals(Object o);
    int compareTo(Variable v);
    Object clone();
    int getSyntax();
    boolean isException();
    String toString();
}

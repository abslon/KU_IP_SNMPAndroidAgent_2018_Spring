package com.abslon.ipproject;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;

import java.io.IOException;
import java.io.OutputStream;

public class VariableBinding implements BERSerializable{
    private OID oid;
    private Variable variable;

    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        if (oid == null) {
            throw new IllegalArgumentException(
                    "OID of a VariableBinding must not be null");
        }
        this.oid = (OID) new OID(oid);
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        if (variable == null) {
            throw new IllegalArgumentException(
                    "Variable of a VariableBinding must not be null");
        }
        this.variable = (Variable) variable.clone();
    }

    public VariableBinding() {
        oid = new OID();
        this.variable = Null.instance;
    }

    public VariableBinding(OID oid) {
        setOid(oid);
        this.variable = Null.instance;
    }

    public VariableBinding(OID oid, Variable variable) {
        setOid(oid);
        setVariable(variable);
    }

    public final int getBERPayloadLength() {
        return oid.getBERLength() + variable.getBERLength();
    }

    public final int getBERLength() {
        int length = getBERPayloadLength();
        // add type byte and length of length
        length += BER.getBERLengthOfLength(length) + 1;
        return length;
    }

    public final void decodeBER(BERInputStream inputStream) throws IOException {
        BER.MutableByte type = new BER.MutableByte();
        int length = BER.decodeHeader(inputStream, type);
        long startPos = inputStream.getPosition();
        if (type.getValue() != BER.SEQUENCE) {
            throw new IOException("Invalid sequence encoding: " + type.getValue());
        }
        oid.decodeBER(inputStream);
        variable = createFromBER(inputStream);
        if (BER.isCheckSequenceLength()) {
            BER.checkSequenceLength(length,
                    (int) (inputStream.getPosition() - startPos),
                    this);
        }
    }

    public final void encodeBER(OutputStream outputStream) throws IOException {
        int length = getBERPayloadLength();
        BER.encodeHeader(outputStream, BER.SEQUENCE, length);
        oid.encodeBER(outputStream);
        variable.encodeBER(outputStream);
    }

    public Variable createFromBER(BERInputStream inputStream) throws IOException, IllegalArgumentException
    {
        inputStream.mark(2);
        int type = inputStream.read();
        Variable variable;
        variable = createVariable(type);
        inputStream.reset();
        variable.decodeBER(inputStream);
        return variable;
    }

    private static Variable createVariable(int smiSyntax)
    {
        switch (smiSyntax)
        {
            case BER.ASN_OBJECT_ID: {
                return new OID();
            }
            case BER.ASN_INTEGER: {
                return new Int32();
            }
            case BER.ASN_OCTET_STR: {
                return new OctetString();
            }
            case BER.GAUGE32: {
                return new Gauge32();
            }
            case BER.COUNTER32: {
                return new Counter32();
            }
            case BER.COUNTER64: {
                return new Counter64();
            }
            case BER.ASN_NULL: {
                return new Null();
            }
            case BER.TIMETICKS: {
                return new TimeTicks();
            }
            case BER.ENDOFMIBVIEW: {
                return new Null(BER.ENDOFMIBVIEW);
            }
            case BER.NOSUCHINSTANCE: {
                return new Null(BER.NOSUCHINSTANCE);
            }
            case BER.NOSUCHOBJECT: {
                return new Null(BER.NOSUCHOBJECT);
            }
            case BER.OPAQUE: {
                return new Opaque();
            }
            case BER.IPADDRESS: {
                return new IPAddress();
            }
            default:
            {
                throw new IllegalArgumentException("Unsupported variable syntax: " + smiSyntax);
            }
        }
    }

    public Object clone() {
        return new VariableBinding(oid, variable);
    }

}

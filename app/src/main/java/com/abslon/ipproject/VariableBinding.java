package com.abslon.ipproject;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.asn1.BERSerializable;

import java.io.IOException;
import java.io.OutputStream;

public class VariableBinding implements BERSerializable
{
    // OID (name)과 Variable (value)
    private OID oid;
    private Variable variable;

    // 생성자
    public VariableBinding()
    {
        oid = new OID();
        this.variable = Null.instance;
    }

    public VariableBinding(OID oid)
    {
        setOid(oid);
        this.variable = Null.instance;
    }

    public VariableBinding(OID oid, Variable variable)
    {
        setOid(oid);
        setVariable(variable);
    }

    public final int getBERLength()
    {
        int length = getBERPayloadLength();
        return length + BER.getBERLengthOfLength(length) + 1;
    }

    public final int getBERPayloadLength()
    {
        return oid.getBERLength() + variable.getBERLength();
    }

    public final void encodeBER(OutputStream outputStream) throws IOException
    {
        int length = getBERPayloadLength();
        BER.encodeHeader(outputStream, BER.SEQUENCE, length);
        oid.encodeBER(outputStream);
        variable.encodeBER(outputStream);
    }

    public final void decodeBER(BERInputStream inputStream) throws IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        BER.decodeHeader(inputStream, type);
        if (type.getValue() != BER.SEQUENCE)
        {
            throw new IOException("VariableBinding의 타입이 아닙니다. 들어온 type : "+ type.getValue());
        }
        oid.decodeBER(inputStream);
        variable = createVariable(inputStream);
    }


    public Variable createVariable(BERInputStream inputStream) throws IOException, IllegalArgumentException
    {
        inputStream.mark(2);
        int type = inputStream.read();
        Variable variable;
        switch (type)
        {
            case BER.ASN_OBJECT_ID:
                variable = new OID();
                break;

            case BER.ASN_INTEGER:
                variable = new Int32();
                break;

            case BER.ASN_OCTET_STR:
                variable = new OctetString();
                break;

            case BER.GAUGE32:
                variable = new Gauge32();
                break;

            case BER.COUNTER32:
                variable = new Counter32();
                break;

            case BER.COUNTER64:
                variable = new Counter64();
                break;

            case BER.ASN_NULL:
                variable = new Null();
                break;

            case BER.TIMETICKS:
                variable = new TimeTicks();
                break;

            case BER.ENDOFMIBVIEW:
                variable = new Null(BER.ENDOFMIBVIEW);
                break;

            case BER.NOSUCHINSTANCE:
                variable = new Null(BER.NOSUCHINSTANCE);
                break;

            case BER.NOSUCHOBJECT:
                variable = new Null(BER.NOSUCHOBJECT);
                break;

            case BER.OPAQUE:
                variable = new Opaque();
                break;

            case BER.IPADDRESS:
                variable = new IPAddress();
                break;

            default:
                throw new IllegalArgumentException("지원되지 않는 type 입니다 : " + type);
        }
        inputStream.reset();
        variable.decodeBER(inputStream);
        return variable;
    }

    public Object clone() {
        return new VariableBinding(oid, variable);
    }

    // getsetter
    public OID getOid() { return oid; }

    public void setOid(OID oid)
    {
        if (oid == null)
        {
            throw new IllegalArgumentException(
                    "OID of a VariableBinding must not be null");
        }
        this.oid = (OID) new OID(oid);
    }

    public Variable getVariable()
    {
        return variable;
    }

    public void setVariable(Variable variable)
    {
        if (variable == null)
        {
            throw new IllegalArgumentException(
                    "Variable of a VariableBinding must not be null");
        }
        this.variable = (Variable) variable.clone();
    }
}

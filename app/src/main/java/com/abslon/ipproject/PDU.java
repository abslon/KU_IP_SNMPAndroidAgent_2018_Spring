package com.abslon.ipproject;

import org.snmp4j.asn1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

public class PDU implements BERSerializable
{
    // PDU type
    public static final int GET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR);
    public static final int GETNEXT  = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x1);
    public static final int RESPONSE = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x2);
    public static final int SET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x3);

    // Error type
    public static final int noError = 0;
    public static final int tooBig = 1;
    public static final int noSuchName = 2;
    public static final int badValue = 3;
    public static final int readOnly = 4;
    public static final int genErr = 5;

    // 초기 변수
    int pduType = GET;
    protected Vector<VariableBinding> variableBindings = new Vector<VariableBinding>();
    protected Int32 errorStatus = new Int32();
    protected Int32 errorIndex = new Int32();
    protected Int32 requestID = new Int32();

    // 생성자
    public PDU() { }

    public int getBERLength()
    {
        int length = getBERPayloadLength();
        return length + BER.getBERLengthOfLength(length) + 1;
    }

    public int getBERPayloadLength()
    {
        int length = 0;
        for (VariableBinding variableBinding : variableBindings)
        {
            length += variableBinding.getBERLength();
        }
        length += BER.getBERLengthOfLength(length) + 1;
        length += requestID.getBERLength() + errorStatus.getBERLength() + errorIndex.getBERLength();
        return length;
    }

    public void encodeBER(OutputStream outputStream) throws IOException
    {
        BER.encodeHeader(outputStream, pduType, getBERPayloadLength());

        requestID.encodeBER(outputStream);
        errorStatus.encodeBER(outputStream);
        errorIndex.encodeBER(outputStream);

        int VBLength = 0;
        for (VariableBinding vb : variableBindings)
        {
            VBLength += vb.getBERLength();
        }
        BER.encodeHeader(outputStream, BER.SEQUENCE, VBLength);

        for (VariableBinding vb : variableBindings)
        {
            vb.encodeBER(outputStream);
        }
    }

    public void decodeBER(BERInputStream inputStream) throws IOException
    {
        BER.MutableByte pduType = new BER.MutableByte();
        int length = BER.decodeHeader(inputStream, pduType);
        switch (pduType.getValue())
        {
            case PDU.SET:
            case PDU.GET:
            case PDU.GETNEXT:
            case PDU.RESPONSE:
                break;
            default:
                throw new IOException("알 수 없는 PDU type: "+pduType.getValue());
        }
        this.pduType = pduType.getValue();
        requestID.decodeBER(inputStream);
        errorStatus.decodeBER(inputStream);
        errorIndex.decodeBER(inputStream);

        pduType = new BER.MutableByte();
        int VBLength = BER.decodeHeader(inputStream, pduType);
        if (pduType.getValue() != BER.SEQUENCE)
        {
            throw new IOException("SEQUENCE가 아닌 다른 tag : "+ pduType.getValue());
        }

        int startPos = (int) inputStream.getPosition();
        variableBindings = new Vector<>();
        while (inputStream.getPosition() - startPos < VBLength)
        {
            VariableBinding vb = new VariableBinding();
            vb.decodeBER(inputStream);
            variableBindings.add(vb);
        }
    }

    // getsetter
    public void add(VariableBinding vb) { variableBindings.add(vb); }

    public void setType(int type) { this.pduType  = type; }
    public void setRequestID(Int32 requestID) { this.requestID = requestID; }
}

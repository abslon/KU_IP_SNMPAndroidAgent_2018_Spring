package com.abslon.ipproject;

import org.snmp4j.asn1.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

public class PDU implements BERSerializable
{
    public static final int GET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR);
    public static final int GETNEXT  = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x1);
    public static final int RESPONSE = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x2);
    public static final int SET      = (BER.ASN_CONTEXT | BER.ASN_CONSTRUCTOR | 0x3);

    public static final int noError = 0;
    public static final int tooBig = 1;
    public static final int noSuchName = 2;
    public static final int badValue = 3;
    public static final int readOnly = 4;
    public static final int genErr = 5;

    int pduType = GET;
    protected Vector<VariableBinding> variableBindings = new Vector<VariableBinding>();
    protected Int32 errorStatus = new Int32();
    protected Int32 errorIndex = new Int32();
    protected Int32 requestID = new Int32();

    public PDU() { }

    public PDU(int pduType, List<? extends VariableBinding> vbs) {
        this.pduType = pduType;
        variableBindings = new Vector<VariableBinding>(vbs.size());
        for (VariableBinding vb : vbs) {
            variableBindings.add((VariableBinding) vb.clone());
        }
    }

    public void add(VariableBinding vb) {
        variableBindings.add(vb);
    }
    public VariableBinding get(int index) {
        return variableBindings.get(index);
    }
    public void setType(int type) {
        this.pduType  = type;
    }
    public void setRequestID(Int32 requestID) {
        this.requestID = requestID;
    }
    public int getBERLength() {
        // header for data_pdu
        int length = getBERPayloadLengthPDU();
        length += BER.getBERLengthOfLength(length) + 1;
        // assume maximum length here
        return length;
    }

    public int getBERPayloadLength() {
        return getBERPayloadLengthPDU();
    }

    public void decodeBER(BERInputStream inputStream) throws IOException {
        BER.MutableByte pduType = new BER.MutableByte();
        int length = BER.decodeHeader(inputStream, pduType);
        int pduStartPos = (int)inputStream.getPosition();
        switch (pduType.getValue()) {
            case PDU.SET:
            case PDU.GET:
            case PDU.GETNEXT:
                /*
            case PDU.GETBULK:
            case PDU.INFORM:
            case PDU.REPORT:
            case PDU.TRAP:*/
            case PDU.RESPONSE:
                break;
            default:
                throw new IOException("Unsupported PDU type: "+pduType.getValue());
        }
        this.pduType = pduType.getValue();
        requestID.decodeBER(inputStream);
        errorStatus.decodeBER(inputStream);
        errorIndex.decodeBER(inputStream);

        pduType = new BER.MutableByte();
        int vbLength = BER.decodeHeader(inputStream, pduType);
        if (pduType.getValue() != BER.SEQUENCE) {
            throw new IOException("Encountered invalid tag, SEQUENCE expected: "+
                    pduType.getValue());
        }
        // rest read count
        int startPos = (int)inputStream.getPosition();
        variableBindings = new Vector<VariableBinding>();
        while (inputStream.getPosition() - startPos < vbLength) {
            VariableBinding vb = new VariableBinding();
            vb.decodeBER(inputStream);
            variableBindings.add(vb);
        }
        if (inputStream.getPosition() - startPos != vbLength) {
            throw new IOException("Length of VB sequence ("+vbLength+
                    ") does not match real length: "+
                    ((int)inputStream.getPosition()-startPos));
        }
        if (BER.isCheckSequenceLength()) {
            BER.checkSequenceLength(length,
                    (int) inputStream.getPosition() - pduStartPos,
                    this);
        }
    }

    public static int getBERLength(List<? extends VariableBinding> variableBindings) {
        int length = 0;
        // length for all vbs
        for (VariableBinding variableBinding : variableBindings) {
            length += variableBinding.getBERLength();
        }
        return length;
    }

    protected int getBERPayloadLengthPDU() {
        int length = getBERLength(variableBindings);
        length += BER.getBERLengthOfLength(length) + 1;

        // req id, error status, error index
        Int32 i32 = new Int32(requestID.getValue());
        length += i32.getBERLength();
        i32 = errorStatus;
        length += i32.getBERLength();
        i32 = errorIndex;
        length += i32.getBERLength();
        i32 = null;
        return length;
    }

    public void encodeBER(OutputStream outputStream) throws IOException {
        BER.encodeHeader(outputStream, pduType, getBERPayloadLengthPDU());

        requestID.encodeBER(outputStream);
        errorStatus.encodeBER(outputStream);
        errorIndex.encodeBER(outputStream);

        int vbLength = 0;
        for (VariableBinding vb : variableBindings) {
            vbLength += vb.getBERLength();
        }
        BER.encodeHeader(outputStream, BER.SEQUENCE, vbLength);
        for (VariableBinding vb : variableBindings) {
            vb.encodeBER(outputStream);
        }
    }
}

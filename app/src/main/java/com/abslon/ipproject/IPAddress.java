package com.abslon.ipproject;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddress implements Variable
{
    /*
    private static final byte[] IPANYADDRESS = { 0,0,0,0 };
    public static final InetAddress ANY_IPADDRESS = //InetAddress.getByAddress(IPANYADDRESS);*/
    private java.net.InetAddress inetAddress;

    public IPAddress() {

    }

    public IPAddress(InetAddress address) {
        if (address == null) {
            throw new NullPointerException();
        }
        this.inetAddress = address;
    }

    public static IPAddress parse(String address) {
        try {
            InetAddress addr = InetAddress.getByName(address);
            return new IPAddress(addr);
        }
        catch (Exception ex) {
            return null;
        }
    }

    public boolean parseAddress(String address) {
        try {
            inetAddress = InetAddress.getByName(address);
            return true;
        }
        catch (UnknownHostException uhex) {
            return false;
        }
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException {
        BER.MutableByte type = new BER.MutableByte();
        byte[] value = BER.decodeString(inputStream, type);
        if (type.getValue() != BER.IPADDRESS) {
            throw new IOException("Wrong type encountered when decoding Counter: "+
                    type.getValue());
        }
        if (value.length != 4) {
            throw new IOException("IpAddress encoding error, wrong length: " +
                    value.length);
        }
        inetAddress = InetAddress.getByAddress(value);
    }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException {
        byte[] address = new byte[4];
        if (inetAddress instanceof Inet6Address) {
            Inet6Address v6Addr = (Inet6Address)inetAddress;
            if (v6Addr.isIPv4CompatibleAddress()) {
                byte[] v6Bytes = inetAddress.getAddress();
                System.arraycopy(v6Bytes, v6Bytes.length-5, address, 0, 4);
            }
        }
        else {
            System.arraycopy(inetAddress.getAddress(), 0, address, 0, 4);
        }
        BER.encodeString(outputStream, BER.IPADDRESS, address);
    }

    public int getBERPayloadLength() {
        return getBERLength();
    }

    public int getBERLength() {
        return 6;
    }
}

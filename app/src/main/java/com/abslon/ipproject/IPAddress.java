package com.abslon.ipproject;

import org.snmp4j.asn1.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

public class IPAddress implements Variable
{
    // IP 주소 값
    private java.net.InetAddress inetAddress;

    // 생성자
    public IPAddress() { }

    public IPAddress(InetAddress address)
    {
        if (address == null)
            throw new NullPointerException();

        else this.inetAddress = address;
    }

    public IPAddress(String address)
    {
        try
        {
            InetAddress addr = InetAddress.getByName(address);
            setInetAddress(addr);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(address);
        }
    }

    // parsing IP address

    public boolean CanParse(String address)
    {
        try
        {
            inetAddress = InetAddress.getByName(address);
            return true;
        }
        catch (UnknownHostException uhex)
        {
            return false;
        }
    }

    // BERSerializable 인터페이스
    public int getBERLength() { return 6; }

    public int getBERPayloadLength() { return 4; }

    public void encodeBER(OutputStream outputStream) throws java.io.IOException
    {
        byte[] address = new byte[4];
        System.arraycopy(inetAddress.getAddress(), 0, address, 0, 4);
        BER.encodeString(outputStream, BER.IPADDRESS, address);
    }

    public void decodeBER(BERInputStream inputStream) throws java.io.IOException
    {
        BER.MutableByte type = new BER.MutableByte();
        byte[] value = BER.decodeString(inputStream, type);
        if (type.getValue() != BER.IPADDRESS)
        {
            throw new IOException("IP 주소가 아닌 값을 decoding 했습니다. 들어온 type : "+ type.getValue());
        }
        if (value.length != 4)
        {
            throw new IOException("IP 주소 encoding이 잘못되었습니다. 길이 : " + value.length);
        }
        inetAddress = InetAddress.getByAddress(value);
    }

    // Variable 인터페이스

    public int compareTo(Variable o)
    {
        OctetString a = new OctetString(inetAddress.getAddress());
        return a.compareTo(new OctetString(((IPAddress)o).getInetAddress().getAddress()));
    }

    public boolean equals(Object o) { return (o instanceof IPAddress) && (compareTo((IPAddress)o) == 0); }

    public Object clone() { return new IPAddress(inetAddress); }

    public int getSyntax() { return BER.IPADDRESS; }

    public boolean isException() { return false; }

    public String toString() {return inetAddress.toString(); }

    // getsetter
    public void setAddress(byte[] rawValue) throws UnknownHostException { this.inetAddress = InetAddress.getByAddress(rawValue); }

    public void setInetAddress(java.net.InetAddress inetAddress) { this.inetAddress = inetAddress; }

    public InetAddress getInetAddress() { return inetAddress; }

    public void setValue(String value)
    {
        if (!CanParse(value))
            throw new IllegalArgumentException(value+" cannot be parsed by "+ getClass().getName());
    }

    public void setValue(byte[] value)
    {
        try
        {
            setAddress(value);
        }
        catch (UnknownHostException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}

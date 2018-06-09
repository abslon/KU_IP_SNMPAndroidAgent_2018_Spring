package com.abslon.ipproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Random;
import java.net.*;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
{
    private DatagramSocket socket;

    Int32 version = new Int32(1);
    OctetString public_community;
    OctetString write_community;
    int ID;

    TextView log;
    TextView typetext;
    EditText oidText;
    EditText valueText;
    Button getButton;
    Button setButton;
    Button walkButton;
    Button clearButton;
    Button typeButton;

    Handler handler = null;
    ScrollView scroll;

    int selectType = 0;
    CharSequence Types[] = new CharSequence[]
            {"Integer32", "UInteger32", "Counter32", "Counter64", "Gauge32", "IP Address",
                    "NULL", "Object ID", "OctetString", "Opaque", "TimeTicks"};
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            socket = new DatagramSocket(61666);
        } catch (Exception e) {}

        public_community = new OctetString("public");
        write_community = new OctetString("write");
        Random random = new Random();
        ID = random.nextInt(2147483647);

        log = findViewById(R.id.LogTextView);
        typetext = findViewById(R.id.TypeView);

        oidText = findViewById(R.id.OIDText);
        valueText = findViewById(R.id.ValueText);

        getButton = findViewById(R.id.GETButton);
        getButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Snmp_GET(oidText.getText().toString());
            }
        });

        setButton = findViewById(R.id.SETButton);
        setButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Snmp_SET(oidText.getText().toString(), valueText.getText().toString());
            }
        });

        walkButton = findViewById(R.id.WALKButton);
        walkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Snmp_WALKLoop("");
            }
        });

        clearButton = findViewById(R.id.ClearButton);
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                log.setText("");
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Value Type");
        builder.setItems(Types, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                selectType = which;
                typetext.setText(Types[selectType]);
            }
        });

        typeButton = findViewById(R.id.Typebutton);
        typeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                builder.show();
            }
        });

        handler = new Handler();
        scroll = findViewById(R.id.scrollView);
    }

    void Send_Packet(final byte[] send_buf)
    {
        Thread sendThread = new Thread()
        {
            byte[] buffer = send_buf;
            public void run()
            {
                try
                {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName("kuwiden.iptime.org"), 11161);
                    socket.send(packet);
                    Log.w("send_thread", "test");
                }
                catch (Exception e)
                {
                    String ex = e.toString();
                    Log.w("send_thread", ex);
                }
            }
        };
        sendThread.start();

        try
        {
            sendThread.join();
        }
        catch (Exception e){}
    }

    DatagramPacket Recv_Packet()
    {
        RecvTask task = new RecvTask(socket);
        new Thread(task).start();
        return task.getPacket();
    }

    void Snmp_GET(String oid)
    {
        if(oid.length() == 0) return;
        PDU SendingPdu = new PDU();
        SendingPdu.add(new VariableBinding(new OID(oid)));
        SendingPdu.setType(PDU.GET);
        SendingPdu.setRequestID(new Int32(ID));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer;
        
        try
        {
            int length = SendingPdu.getBERLength() + public_community.getBERLength() + version.getBERLength();
            BER.encodeHeader(byteArrayOutputStream, BER.SEQUENCE, length);
            version.encodeBER(byteArrayOutputStream);
            public_community.encodeBER(byteArrayOutputStream);
            SendingPdu.encodeBER(byteArrayOutputStream);
            buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }
        catch (IOException e)
        {
            Log.w("SendPacket", "Failed to Create Packet");
            Log.w("SendPacket", e.toString());
            buffer = new byte[0];
        }
        Send_Packet(buffer);


        DatagramPacket ReceivedPacket = Recv_Packet();
        byte[] recv_buf = new byte[ReceivedPacket.getLength()];
        System.arraycopy(ReceivedPacket.getData(), 0, recv_buf, 0, ReceivedPacket.getLength());
        ByteBuffer byteBuffer = ByteBuffer.allocate(recv_buf.length);
        byteBuffer.put(recv_buf);
        byteBuffer.position(0);
        BERInputStream messageBuffer = new BERInputStream(byteBuffer);
        BER.MutableByte mutableByte = new BER.MutableByte();
        PDU recv_pdu = new PDU();
        try{
            BER.decodeHeader(messageBuffer, mutableByte);
            Int32 RVersion = new Int32();
            RVersion.decodeBER(messageBuffer);
            OctetString Community = new OctetString();
            Community.decodeBER(messageBuffer);
            recv_pdu.decodeBER(messageBuffer);
        }
        catch (Exception e)
        {
            Log.w("RecvPacket", "Failed to Create PDU");
            Log.w("RecvPacket", e.toString());
        }

        if(recv_pdu.errorStatus.getValue() != PDU.noError)
        {
            String errmsg;
            switch (recv_pdu.errorStatus.getValue())
            {
                case PDU.tooBig: {
                    errmsg = "SNMPGET - SNMP ERROR: TOO BIG on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.noSuchName: {
                    errmsg = "SNMPGET - SNMP ERROR: NO SUCH NAME on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.badValue : {
                    errmsg = "SNMPGET - SNMP ERROR: BAD VALUE on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.readOnly : {
                    errmsg = "SNMPGET - SNMP ERROR: READ ONLY on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.genErr : {
                    errmsg = "SNMPGET - SNMP ERROR: GENERAL ERROR on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                default:{
                    errmsg = "SNMPGET - SNMP ERROR on" + recv_pdu.errorIndex.getValue() + '\n';
                }
            }
            log.setText(log.getText().toString() + errmsg);
        }

        else
        {
            VariableBinding vb = recv_pdu.variableBindings.get(0);
            String msg = "SNMPGET - " + vb.getOid().toString() + " = " + vb.getVariable().getClass().getSimpleName() + ": "+ vb.getVariable().toString() + '\n';
            log.setText(log.getText().toString() + msg);
        }
    }

    void Snmp_SET(String oid, String value)
    {
        if(oid.length() == 0 || value.length() == 0) return;
        PDU SendingPdu = new PDU();
        Variable variable;
        try{
            switch (selectType)
            {
                case 0: { // int
                    variable = new Int32(Integer.parseInt(value));
                    break;
                }
                case 1: { // uint
                    variable = new UnsignedInt32(Long.parseLong(value));
                    break;
                }
                case 2: { // Counter32
                    variable = new Counter32(Long.parseLong(value));
                    break;
                }
                case 3: { // Counter64
                    variable = new Counter64(Long.parseLong(value));
                    break;
                }
                case 4: { // Gauge32
                    variable = new Gauge32(Long.parseLong(value));
                    break;
                }
                case 5: { // IP Address
                    variable = new IPAddress(value);
                    break;
                }
                case 6: { // NULL
                    variable = Null.instance;
                    break;
                }
                case 7: { // OID
                    variable = new OID (value);
                    break;
                }
                case 8: { // OctetString
                    variable = new OctetString(value);
                    break;
                }
                case 9: { // Opaque
                    variable = new Opaque(value);
                    break;
                }
                case 10: { // OctetString
                    variable = new TimeTicks(Long.parseLong(value));
                    break;
                }
                default: {
                    variable = Null.instance;
                }
            }
        }
        catch (Exception e)
        {
            log.setText(log.getText().toString() + e.toString() + '\n');
            variable = Null.instance;
        }

        SendingPdu.add(new VariableBinding(new OID(oid), variable));
        SendingPdu.setType(PDU.SET);
        SendingPdu.setRequestID(new Int32(ID));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer;

        try
        {
            int length = SendingPdu.getBERLength() + write_community.getBERLength() + version.getBERLength();
            BER.encodeHeader(byteArrayOutputStream, BER.SEQUENCE, length);
            version.encodeBER(byteArrayOutputStream);
            write_community.encodeBER(byteArrayOutputStream);
            SendingPdu.encodeBER(byteArrayOutputStream);
            buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }
        catch (IOException e)
        {
            Log.w("SendPacket", "Failed to Create Packet");
            Log.w("SendPacket", e.toString());
            buffer = new byte[0];
        }
        Send_Packet(buffer);


        DatagramPacket ReceivedPacket = Recv_Packet();
        byte[] recv_buf = new byte[ReceivedPacket.getLength()];
        System.arraycopy(ReceivedPacket.getData(), 0, recv_buf, 0, ReceivedPacket.getLength());
        ByteBuffer byteBuffer = ByteBuffer.allocate(recv_buf.length);
        byteBuffer.put(recv_buf);
        byteBuffer.position(0);
        BERInputStream messageBuffer = new BERInputStream(byteBuffer);
        BER.MutableByte mutableByte = new BER.MutableByte();
        PDU recv_pdu = new PDU();
        try{
            BER.decodeHeader(messageBuffer, mutableByte);
            Int32 RVersion = new Int32();
            RVersion.decodeBER(messageBuffer);
            OctetString Community = new OctetString();
            Community.decodeBER(messageBuffer);
            recv_pdu.decodeBER(messageBuffer);
        }
        catch (Exception e)
        {
            Log.w("RecvPacket", "Failed to Create PDU");
            Log.w("RecvPacket", e.toString());
        }

        if(recv_pdu.errorStatus.getValue() != PDU.noError)
        {
            String errmsg;
            switch (recv_pdu.errorStatus.getValue())
            {
                case PDU.tooBig: {
                    errmsg = "SNMPSET - SNMP ERROR: TOO BIG on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.noSuchName: {
                    errmsg = "SNMPSET - SNMP ERROR: NO SUCH NAME on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.badValue : {
                    errmsg = "SNMPSET - SNMP ERROR: BAD VALUE on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.readOnly : {
                    errmsg = "SNMPSET - SNMP ERROR: READ ONLY on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.genErr : {
                    errmsg = "SNMPSET - SNMP ERROR: GENERAL ERROR on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                default:{
                    errmsg = "SNMPSET - SNMP ERROR on " + recv_pdu.errorIndex.getValue() + '\n';
                }
            }
            log.setText(log.getText().toString() + errmsg);
        }

        else
        {
            VariableBinding vb = recv_pdu.variableBindings.get(0);
            String msg = "SNMPSET - " + vb.getOid().toString() + " = " + vb.getVariable().getClass().getSimpleName() + ": "+ vb.getVariable().toString() + '\n';
            log.setText(log.getText().toString() + msg);
        }
    }

    String Snmp_WALK(String oid)
    {
        PDU SendingPdu = new PDU();
        SendingPdu.add(new VariableBinding(new OID(oid)));
        SendingPdu.setType(PDU.GETNEXT);
        SendingPdu.setRequestID(new Int32(ID));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer;

        try
        {
            int length = SendingPdu.getBERLength() + public_community.getBERLength() + version.getBERLength();
            BER.encodeHeader(byteArrayOutputStream, BER.SEQUENCE, length);
            version.encodeBER(byteArrayOutputStream);
            public_community.encodeBER(byteArrayOutputStream);
            SendingPdu.encodeBER(byteArrayOutputStream);
            buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }
        catch (IOException e)
        {
            Log.w("SendPacket", "Failed to Create Packet");
            Log.w("SendPacket", e.toString());
            buffer = new byte[0];
        }
        Send_Packet(buffer);


        DatagramPacket ReceivedPacket = Recv_Packet();
        byte[] recv_buf = new byte[ReceivedPacket.getLength()];
        System.arraycopy(ReceivedPacket.getData(), 0, recv_buf, 0, ReceivedPacket.getLength());
        ByteBuffer byteBuffer = ByteBuffer.allocate(recv_buf.length);
        byteBuffer.put(recv_buf);
        byteBuffer.position(0);
        BERInputStream messageBuffer = new BERInputStream(byteBuffer);
        BER.MutableByte mutableByte = new BER.MutableByte();
        PDU recv_pdu = new PDU();
        try{
            BER.decodeHeader(messageBuffer, mutableByte);
            Int32 RVersion = new Int32();
            RVersion.decodeBER(messageBuffer);
            OctetString Community = new OctetString();
            Community.decodeBER(messageBuffer);
            recv_pdu.decodeBER(messageBuffer);
        }
        catch (Exception e)
        {
            Log.w("RecvPacket", "Failed to Create PDU");
            Log.w("RecvPacket", e.toString());
        }

        if(recv_pdu.errorStatus.getValue() != PDU.noError)
        {
            String errmsg;
            switch (recv_pdu.errorStatus.getValue())
            {
                case PDU.tooBig: {
                    errmsg = "SNMPGET - SNMP ERROR: TOO BIG on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.noSuchName: {
                    errmsg = "SNMPGET - SNMP ERROR: NO SUCH NAME on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.badValue : {
                    errmsg = "SNMPGET - SNMP ERROR: BAD VALUE on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.readOnly : {
                    errmsg = "SNMPGET - SNMP ERROR: READ ONLY on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                case PDU.genErr : {
                    errmsg = "SNMPGET - SNMP ERROR: GENERAL ERROR on " + recv_pdu.errorIndex.getValue() + '\n';
                    break;
                }

                default:{
                    errmsg = "SNMPGET - SNMP ERROR on" + recv_pdu.errorIndex.getValue() + '\n';
                }
            }
            final String f = errmsg;
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(log.getText().length() > 5000)
                    {
                        String temp = log.getText().toString().substring(2000, log.getText().toString().length() - 1);
                        log.setText(temp);
                    }

                    log.setText(log.getText().toString() + f);
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
            return "";
        }

        else
        {
            VariableBinding vb = recv_pdu.variableBindings.get(0);
            if(vb.getVariable().isException()) return "";
            final String msg = "SNMPWALK - " + vb.getOid().toString() + " = " + vb.getVariable().getClass().getSimpleName() + ": "+ vb.getVariable().toString() + '\n';
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if(log.getText().length() > 5000)
                    {
                        String temp = log.getText().toString().substring(2000, log.getText().toString().length() - 1);
                        log.setText(temp);
                    }
                    log.setText(log.getText().toString() + msg);
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
            return vb.getOid().toString();
        }
    }

    void Snmp_WALKLoop(final String oid)
    {

        new Thread()
        {
            @Override
            public void run()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getButton.setEnabled(false);
                        setButton.setEnabled(false);
                        walkButton.setEnabled(false);
                        typeButton.setEnabled(false);
                    }
                });

                String resultOID = Snmp_WALK(oid);
                while(resultOID.length() > 0)
                {
                    resultOID = Snmp_WALK(resultOID);
                }

                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getButton.setEnabled(true);
                        setButton.setEnabled(true);
                        walkButton.setEnabled(true);
                        typeButton.setEnabled(true);
                    }
                });
            }
        }.start();
    }
}

class RecvTask implements Runnable
{
    DatagramSocket socket;
    private volatile boolean done = false;
    byte[] buffer = new byte[1024];
    DatagramPacket recv = new DatagramPacket(buffer, buffer.length);

    RecvTask(DatagramSocket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            socket.receive(recv);
            Log.w("recv_thread", "test");
        }
        catch(Exception e)
        {
            String ex = e.toString();
            Log.w("recv_thread", ex);
        }
        done = true;
        synchronized (this){
            this.notifyAll();
        }
    }

    public DatagramPacket getPacket()
    {
        if(!done)
        {
            synchronized (this)
            {
                try
                {
                    this.wait();
                }
                catch (InterruptedException e)
                {
                    Log.w("recv_thread", e.toString());
                }
            }
        }
        return recv;
    }
}
package com.abslon.ipproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.snmp4j.asn1.*;

public class MainActivity extends AppCompatActivity
{
    int version;
    String public_community;
    String write_community;
    PDU pdu;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

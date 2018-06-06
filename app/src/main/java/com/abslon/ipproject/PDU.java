package com.abslon.ipproject;

import java.util.List;

public class PDU
{
    int pduType;
    int requestID;
    int errorStatus;
    int errorIndex;

    List<VariableBinding> variableBindings;

    int length = 0; // byte 단위

    public PDU()
    {

    }

    public PDU(int pduType, List<VariableBinding> variableBindings)
    {
        this.pduType = pduType;
        this.variableBindings = variableBindings;
    }
}

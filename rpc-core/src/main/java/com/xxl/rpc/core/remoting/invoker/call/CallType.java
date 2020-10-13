package com.xxl.rpc.core.remoting.invoker.call;

/**
 * rpc call type
 *
 * @author mzj 2018-10-19
 */
public enum CallType {

    SYNC,
    FUTURE,
    CALLBACK,
    ONEWAY;

    public static CallType match(String name, CallType defaultCallType){
        for (CallType item : CallType.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultCallType;
    }

}

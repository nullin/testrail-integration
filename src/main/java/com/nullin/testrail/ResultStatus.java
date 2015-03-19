package com.nullin.testrail;

/**
* Result status that corresponds to TestRail
*
* @author nullin
*/
public enum ResultStatus {
    PASS,
    FAIL,
    SKIP, //shows up as blocked in TestRail
    UNTESTED
}

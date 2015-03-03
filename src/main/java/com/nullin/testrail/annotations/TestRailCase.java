package com.nullin.testrail.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents one or more cases in TestRail
 *
 * @author nullin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestRailCase {

    //automation id in TestRail (if any).
    //NOTE: "automation_id" custom field needs to be added in TestRail
    String value() default "";
    //if true, any value for automation id is ignored
    //lets the listener know that it should not raise a warning for no automation id
    boolean selfReporting() default false;
    //if the tests are data driven
    //boolean dataDriven() default false;
}

package com.nullin.testrail.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestRailClass {

    String suiteName();
}

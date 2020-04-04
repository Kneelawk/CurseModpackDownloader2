/**
 * Created by Kneelawk on 3/31/20.
 */
module com.kneelawk.cmpdl2 {
    exports com.kneelawk.cmpdl2;
    exports com.kneelawk.cmpdl2.curse;
    exports com.kneelawk.cmpdl2.curse.data;
    exports com.kneelawk.cmpdl2.curse.data.curseapi;
    exports com.kneelawk.cmpdl2.curse.data.manifest;
    exports com.kneelawk.cmpdl2.curse.modpack;
    exports com.kneelawk.cmpdl2.net;
    exports com.kneelawk.cmpdl2.tasks;
    exports com.kneelawk.cmpdl2.util;

    requires kotlin.stdlib;

    requires com.google.common;
    requires java.json;
    requires java.xml;

    requires commons.codec;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    requires tornadofx;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
}
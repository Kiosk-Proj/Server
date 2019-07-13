open module KioskServer.main {
    requires spring.web;
    requires spring.websocket;
    requires spring.context;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.sql;
    requires java.xml.crypto;
    requires spring.boot;
    requires spring.boot.autoconfigure;

}
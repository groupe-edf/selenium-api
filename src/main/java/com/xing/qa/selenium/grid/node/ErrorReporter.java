package com.xing.qa.selenium.grid.node;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openqa.grid.common.exception.RemoteException;

/**
* ErrorReporter
*
* @author Jens Hausherr (jens.hausherr@xing.com)
*/
class ErrorReporter extends BaseSeleniumReporter {

    private final RemoteException exception;

    public ErrorReporter(String remoteHostName, InfluxDB influxdb, String database, RemoteException ex) {
        super(remoteHostName, influxdb, database);
        this.exception = ex;
    }

    @Override
    protected void report() {
        Point point = Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("host", remoteHostName).field("error", exception.getClass().getName()).field("message", exception.getMessage()).build();
        write(point);
    }

}
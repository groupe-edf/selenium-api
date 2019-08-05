package com.xing.qa.selenium.grid.node;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openqa.grid.internal.ExternalSessionKey;
import org.openqa.grid.internal.TestSession;

/**
 * CommandReporter
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
class CommandReporter extends BaseSeleniumReporter {

    protected final TestSession session;
    protected final ContentSnoopingRequest request;
    protected final HttpServletResponse response;
    protected final ReportType type;

    public CommandReporter(String remoteHostName, InfluxDB influxdb, String database, TestSession session,
            ContentSnoopingRequest request, HttpServletResponse response, ReportType type) {
        super(remoteHostName, influxdb, database);
        this.type = type;
        this.request = request;
        this.session = session;
        this.response = response;
    }

    protected void report() {
        ExternalSessionKey esk = session.getExternalKey();
        String sessionKey = null;
        if (esk != null) {
            sessionKey = esk.getKey();
        }

        Point point = Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("host", remoteHostName).field("ext_key", sessionKey).field("int_key", session.getInternalKey())
                .field("forwarding", session.isForwardingRequest()).field("orphaned", session.isOrphaned())
                .field("inactivity", session.getInactivityTime()).field("cmd_method", request.getMethod())
                .field("cmd_action", request.getPathInfo()).field("cmd", request.getContent()).build();

        write(point);
    }
}
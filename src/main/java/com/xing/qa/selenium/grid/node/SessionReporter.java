package com.xing.qa.selenium.grid.node;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.openqa.grid.internal.ExternalSessionKey;
import org.openqa.grid.internal.TestSession;

/**
 * Session Reporter
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
class SessionReporter extends BaseSeleniumReporter {
    private final TestSession session;
    private final ReportType type;

    public SessionReporter(String remoteHostName, InfluxDB influxdb, String database, TestSession session,
            ReportType type) {
        super(remoteHostName, influxdb, database);
        this.session = session;
        this.type = type;
    }

    @Override
    protected void report() {
        ExternalSessionKey esk = session.getExternalKey();
        String sessionKey = null;

        if (esk != null) {
            sessionKey = esk.getKey();
        }

        Builder srep = Point.measurement("session.event.measure");

        final Boolean forwardingRequest = session.isForwardingRequest();
        final Boolean orphaned = session.isOrphaned();
        final Long inactivityTime = session.getInactivityTime();
        final long time = System.currentTimeMillis();
        if (ReportType.timeout != type) {
            srep.time(time, TimeUnit.MILLISECONDS).field("host", remoteHostName).field("type", type.toString())
                    .field("ext_key", sessionKey).field("int_key", session.getInternalKey())
                    .field("forwarding", forwardingRequest).field("orphaned", orphaned)
                    .field("inactivity", inactivityTime);
        } else {
            srep.time(time, TimeUnit.MILLISECONDS).field("host", remoteHostName).field("type", type.toString())
                    .field("ext_key", sessionKey).field("int_key", session.getInternalKey())
                    .field("forwarding", forwardingRequest).field("orphaned", orphaned)
                    .field("inactivity", inactivityTime)
                    .field("browser_starting", String.valueOf(session.getInternalKey() == null));
        }

        Builder req = Point.measurement(format("session.cap.requested.%s.measure", type));

        for (Map.Entry<String, Object> rcap : session.getRequestedCapabilities().entrySet()) {
            req.time(time, TimeUnit.MILLISECONDS).field("host", remoteHostName).field("ext_key", sessionKey)
                    .field("int_key", session.getInternalKey()).field("forwarding", forwardingRequest)
                    .field("orphaned", orphaned).field("inactivity", inactivityTime).field("capability", rcap.getKey())
                    .field("val", rcap.getValue());
        }

        Builder prov = Point.measurement(format("session.cap.provided.%s.measure", type));

        for (Map.Entry<String, Object> scap : session.getSlot().getCapabilities().entrySet()) {
            req.time(time, TimeUnit.MILLISECONDS).field("host", remoteHostName).field("ext_key", sessionKey)
                    .field("int_key", session.getInternalKey()).field("forwarding", forwardingRequest)
                    .field("orphaned", orphaned).field("inactivity", inactivityTime).field("capability", scap.getKey())
                    .field("val", scap.getValue());
        }
        write(srep.build(), req.build(), prov.build());
    }

}
package com.xing.qa.selenium.grid.node;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Serie;

/**
* NodeReporter
*
* @author Jens Hausherr (jens.hausherr@xing.com)
*/
class NodeReporter extends BaseSeleniumReporter {

    private final MonitoringWebProxy proxy;

    public NodeReporter(String remoteHostName, InfluxDB influxdb, String database, MonitoringWebProxy monitoringWebProxy) {
        super(remoteHostName, influxdb, database);
        this.proxy = monitoringWebProxy;
    }

    @Override
    protected void report() {
        log.finer(String.format("Reporting: node.%s.measure", SerieNames.utilization));

        Serie load = new Serie.Builder(String.format("node.%s.measure", SerieNames.utilization))
                .columns(
                        "time",
                        "host",
                        "used",
                        "total",
                        "normalized"
                ).values(
                        System.currentTimeMillis(),
                        remoteHostName,
                        proxy.getTotalUsed(),
                        proxy.getMaxNumberOfConcurrentTestSessions(),
                        proxy.getResourceUsageInPercent()
                ).build();
        write(TimeUnit.MILLISECONDS, load);
    }
}
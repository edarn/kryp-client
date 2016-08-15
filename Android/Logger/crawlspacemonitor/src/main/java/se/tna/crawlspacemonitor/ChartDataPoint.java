package se.tna.crawlspacemonitor;


import com.jjoe64.graphview.series.DataPointInterface;

import java.util.Date;

/**
 * Created by A500298 on 2016-08-15.
 */

public class ChartDataPoint implements DataPointInterface {

    Date timeOfCreation = new Date(System.currentTimeMillis());
    double value = 0;

    public ChartDataPoint(double val)
    {
        value = val;
    }
    @Override
    public double getX() {
        return timeOfCreation.getTime();
    }

    @Override
    public double getY() {
        return value;
    }
}

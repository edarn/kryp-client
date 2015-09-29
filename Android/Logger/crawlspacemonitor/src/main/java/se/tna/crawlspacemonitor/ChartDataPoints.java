package se.tna.crawlspacemonitor;

import android.support.v4.util.CircularArray;

import com.jjoe64.graphview.series.DataPoint;

import java.util.Date;

import se.tna.commonloggerservice.KrypgrundStats;

/**
 * Created by a500298 on 2015-09-24.
 */
public class ChartDataPoints {

    int size;
    public CircularArray<DataPoint> absolutMoistureDatapointsInside;
    public CircularArray<DataPoint> absolutMoistureDatapointsOutside;
    //public CircularArray<DataPoint> absolutMoistureDatapoints;
    //public CircularArray<DataPoint> absolutMoistureDatapoints;
    public ChartDataPoints(int iSize)
    {
        size = iSize;
        absolutMoistureDatapointsInside = new CircularArray<>(size);
        absolutMoistureDatapointsOutside = new CircularArray<>(size);
    }

    public void insertData(KrypgrundStats measurement)
    {
        if (absolutMoistureDatapointsOutside.size() > size)
        {
            absolutMoistureDatapointsOutside.popFirst();
            absolutMoistureDatapointsOutside.addLast(new DataPoint(new Date(measurement.time),measurement.absolutFuktUte));

            absolutMoistureDatapointsInside.popFirst();
            absolutMoistureDatapointsInside.addLast(new DataPoint(new Date(measurement.time),measurement.absolutFuktInne));
        }
    }
}
package se.tna.crawlspacemonitor;

//import android.support.v4.util.CircularArray;

import android.support.v4.util.CircularArray;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Date;

import se.tna.commonloggerservice.KrypgrundStats;
import se.tna.commonloggerservice.StatusOfService;

/**
 * Created by a500298 on 2015-09-24.
 */
public class ChartDataPoints {

    int size;
    public CircularArray<DataPoint> absolutMoistureDatapointsInside;
    public CircularArray<DataPoint> absolutMoistureDatapointsOutside;

    public ChartDataPoints(int iSize) {
        size = iSize;
        absolutMoistureDatapointsInside = new CircularArray<>(size);
        absolutMoistureDatapointsOutside = new CircularArray<>(size);
    }

    public void insertData(StatusOfService measurement) {
        if (absolutMoistureDatapointsOutside.size() >= size) {
            absolutMoistureDatapointsOutside.popFirst();
            absolutMoistureDatapointsInside.popFirst();
        } else {
            absolutMoistureDatapointsOutside.addLast(new DataPoint(new Date(System.currentTimeMillis()), measurement.absolutFuktUte));
            absolutMoistureDatapointsInside.addLast(new DataPoint(new Date(System.currentTimeMillis()), measurement.absolutFuktInne));
        }
    }
    //Todo
    //public getDataSeries()
}

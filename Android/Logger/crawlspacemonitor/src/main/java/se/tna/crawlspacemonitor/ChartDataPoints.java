package se.tna.crawlspacemonitor;

//import android.support.v4.util.CircularArray;

import android.support.v4.util.CircularArray;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import se.tna.commonloggerservice.KrypgrundStats;
import se.tna.commonloggerservice.StatusOfService;

/**
 * Created by a500298 on 2015-09-24.
 */
public class ChartDataPoints {

    int size;
    public LineGraphSeries<DataPoint> absolutMoistureDatapointsInside;
    public LineGraphSeries<DataPoint> absolutMoistureDatapointsOutside;

    public ChartDataPoints(int iSize) {
        size = iSize;
        absolutMoistureDatapointsInside = new LineGraphSeries<DataPoint>();
        absolutMoistureDatapointsOutside = new LineGraphSeries<DataPoint>();
    }

    public void insertData(StatusOfService measurement) {
            absolutMoistureDatapointsOutside.appendData(new DataPoint(new Date(System.currentTimeMillis()), measurement.absolutFuktUte),false,200);
            absolutMoistureDatapointsInside.appendData(new DataPoint(new Date(System.currentTimeMillis()), measurement.absolutFuktInne),false,200);
    }
    public enum SeriesType
    {
        AbsolutFuktUte,
        AbsolutFuktInne
    }
    public LineGraphSeries<DataPoint> getDataSeries(SeriesType type)
    {

        if (type == SeriesType.AbsolutFuktInne) return absolutMoistureDatapointsInside;
        else if (type == SeriesType.AbsolutFuktUte) return absolutMoistureDatapointsOutside;
        /*if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                serie.appendData(array.get(i), false, 200);
            }
        }
        */
        return null;
    }
}

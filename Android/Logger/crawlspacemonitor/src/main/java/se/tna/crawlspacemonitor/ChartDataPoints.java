package se.tna.crawlspacemonitor;

//import android.support.v4.util.CircularArray;

import android.graphics.Color;
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
    public LineGraphSeries<ChartDataPoint> absolutMoistureDatapointsInside;
    public LineGraphSeries<ChartDataPoint> absolutMoistureDatapointsOutside;

    public ChartDataPoints(int iSize) {
        size = iSize;
        absolutMoistureDatapointsInside = new LineGraphSeries<>();
       // absolutMoistureDatapointsInside.setDrawDataPoints(true);
        absolutMoistureDatapointsInside.setColor(Color.RED);
        absolutMoistureDatapointsOutside = new LineGraphSeries<>();
       // absolutMoistureDatapointsOutside.setDrawDataPoints(true);
        absolutMoistureDatapointsOutside.setColor(Color.BLUE);

    }

    public void insertData(StatusOfService measurement) {
            absolutMoistureDatapointsOutside.appendData(new ChartDataPoint(measurement.absolutFuktUte),false,200);
            absolutMoistureDatapointsInside.appendData(new ChartDataPoint(measurement.absolutFuktInne),false,200);
    }

    public enum SeriesType
    {
        AbsolutFuktUte,
        AbsolutFuktInne
    }
    public LineGraphSeries<ChartDataPoint> getDataSeries(SeriesType type)
    {

        if (type == SeriesType.AbsolutFuktInne) return absolutMoistureDatapointsInside;
        else if (type == SeriesType.AbsolutFuktUte) return absolutMoistureDatapointsOutside;
        return null;
    }
}

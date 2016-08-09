package se.tna.commonloggerservice;

import java.util.ArrayList;
import java.util.ArrayList;

/**
 * Created by A500298 on 2016-08-07.
 */
public class CrawlSpacePacket {
    public String id;
    public String version;
   // public SurfvindStats[] surfvindMeasurements;

    public ArrayList<String> TimeStamp = new ArrayList<>();

    public ArrayList<Integer> AbsolutFuktInne  = new ArrayList<>();

    public ArrayList<Integer> AbsolutFuktUte  = new ArrayList<>();

    public ArrayList<Float> FuktUte  = new ArrayList<>();

    public ArrayList<Float> FuktInne  = new ArrayList<>();

    public ArrayList<Float> TempInne  = new ArrayList<>();

    public ArrayList<Float> TempUte  = new ArrayList<>();

    public ArrayList<Integer> FanOn  = new ArrayList<>();
}

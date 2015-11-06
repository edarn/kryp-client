using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

/// <summary>
/// Summary description for Location
/// </summary>
public class Location : IComparable
{
    public String Name = "";
    public String imei = "";
    public double Latitud;
    public double Longitude;
    public Location(String i, String j, String Long, String Lat)
    {
        Name = i;
        imei = j;
        Longitude = Convert.ToDouble(Long);
        Latitud = Convert.ToDouble(Lat);
    }

    public int CompareTo(object o)
    {
        Location target = (Location)o;
        return this.Name.CompareTo(target.Name);
    }
}


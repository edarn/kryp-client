using System;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;

using System.Collections.Generic;
using WindInfo;

using System.Diagnostics;

public partial class Default : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        Response.Cache.SetCacheability(System.Web.HttpCacheability.NoCache);

        /* Check which location has been requested */
        string location = Request.QueryString["Location"];
        update(location);
    }

    private void update(String imei)
    {

        String dbToUse = "";
        dbToUse = "Surfvind_data";

        WindData wd = new WindData(true, dbToUse);
        bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

        List<Location> loc = Helper.getLocations();

        if (imei == null)
        {
            Location l = loc[0];
            imei = l.imei;
        }

        if (imei == null)
        {
            /* Default to something */
            imei = "12345";
        }

        wd.SetImei(imei);

        /* Get pre-stored direction and speed arrows */
        imgSpeed.ImageUrl = "http://www.surfvind.se/Images/" + imei + "_img_speed.png";
        imgCompass.ImageUrl = "http://www.surfvind.se/Images/" + imei + "_img_compass.png";

        // Set temp images
        //  water_temp.ImageUrl = "http://www.surfvind.se/Images/" + imei + "_img_water_temp.png";
        //  air_temp.ImageUrl = "http://www.surfvind.se/Images/" + imei + "_img_air_temp.png";

        float w_temp;
        float a_temp;

        WindRecord wr = wd.GetCurrentWind();
        w_temp = wr.AverageWaterTemp;
        a_temp = wr.AverageAirTemp;

        /* Set google map location */
        Location loca = getLocation(imei, loc);

        setAppletLocation(imei);

        Title_loc.Text = loca.Name;

        Title = "Weather info - ";
        if (loca != null)
        {
            addGMap(loca);
            Title += loca;
        }

        if (wr.Moisture != 0)
        {
            // Set temp images
            air_temp.ImageUrl = "~/Images/" + imei + "_img_air_temp.png";
            power.Text = wr.AverageWaterTemp + " V";
            moisture.Text = wr.Moisture + " %";

            air_temp.ToolTip = "Air temperature: " + wr.AverageAirTemp + " °C";
            temperature_container.Visible = true;
            water.Visible = true;
        }
        else
        {
            temperature_container.Visible = false;
            water.Visible = false;
        }

        LocationPage.Visible = true;
        graphview.Attributes["src"] = "http://www.surfvind.se/GenGraphs.aspx?Location=" + imei+"&Duration=2";
    }

    /* 
     * Add a google map pointing to the selected location 
     */
    private void addGMap(Location loc)
    {
        String sMapKey = ConfigurationManager.AppSettings["googlemaps.subgurim.net"];

        Subgurim.Controles.GLatLng gLatLng = new Subgurim.Controles.GLatLng(loc.Latitud, loc.Longitude);

        GMap1.setCenter(gLatLng, 12, Subgurim.Controles.GMapType.GTypes.Hybrid);
        Subgurim.Controles.GMarker marker = new Subgurim.Controles.GMarker(gLatLng);
        GMap1.addGMarker(marker);
    }

    private Location getLocation(String location, List<Location> loc)
    {
        foreach (Location l in loc)
        {
            if (l.imei.Equals(location))
            {
                return l;
            }
        }

        /* Something is f***ed up */
        return null;
    }

    private void setAppletLocation(String imei)
    {
        String html;
        html = "http://www.vindinfo.se/slideshow.aspx?Location=" + imei;
    }
}

using System;
using System.Web.Caching;
using System.Collections.Generic;
using System.Configuration;

using ZedGraph;
using System.Drawing;
using System.Drawing.Imaging;
using System.Web.UI;
using Artem.Web.UI.Controls;

using System.Diagnostics;
using System.Web.UI.WebControls;

namespace Surfvind_2011
{
    public partial class Default : System.Web.UI.Page
    {
        #region declarations
        const int CountIntervals = 100;
        DateTime Start;
        private List<Location> locations;
        #endregion

        protected void LogTime()
        {
            Logger.LogInfo("Duration: " + (DateTime.Now - Start).ToString());
            Start = DateTime.Now;
        }
       public String imei = "";
       private int OldSelectedIndex = -1;
        protected void Page_Load(object sender, EventArgs e)
        {
            String location = Request.QueryString["location"];
         
            Start = DateTime.Now;
            Response.Cache.SetCacheability(System.Web.HttpCacheability.NoCache);
            try
            {
               
                
                LogTime();
                String dbToUse = "";
                dbToUse = "Surfvind_data";

                LogTime();

                WindData wd = new WindData(true, dbToUse);
                bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

                List<Location> loc = wd.GetLocations();
                loc.Sort();

                locations = loc;
                /* Populate location scrollbar */
                populateLocationScrollbar(loc);
                if (loc.Count > 0)
                {
                    imei = loc.ToArray()[ddlWhere.SelectedIndex].imei.ToString();
                    wd.SetImei(imei);
                    /* Set google map location */
                    addGMap(loc[ddlWhere.SelectedIndex]);

                }

                GenGraphs t = new GenGraphs();
                t.update2(imei);
                

                if (location != null)
                {
                    OldSelectedIndex = ddlWhere.SelectedIndex;
                    int index = getIndexForLocation(loc, location);
                    if (index >= 0)
                    {
                        ddlWhere.SelectedIndex = index;
                        imei = location;
                        wd.SetImei(imei);
                    }
                }

                /* Get pre-stored direction and speed arrows */
                imgSpeed.ImageUrl = "~/Images/" + imei + "_img_speed.png";
                imgCompass.ImageUrl = "~/Images/" + imei + "_img_compass.png";

                if (imei == "12345") //Set this to the IMEI nbr that you use for developement of water air and humidity temp
                {
                    // Set temp images
                    water_temp.ImageUrl = "~/Images/" + imei + "_img_water_temp.png";
                    air_temp.ImageUrl = "~/Images/" + imei + "_img_air_temp.png";

                    int w_temp;
                    int a_temp;

                    WindRecord wr = wd.GetCurrentWind();
                    w_temp = wr.AverageWaterTemp;
                    a_temp = wr.AverageAirTemp;

                    water_temp.ToolTip = "Water temperature: " + w_temp + " °C";
                    air_temp.ToolTip = "Air temperature: " + a_temp + " °C";
                    Label1.Text = "Moisture: " + wr.Moisture + "%";
                }
                else
                {
                    air.Visible = false;
                    moisture.Visible = false;
                    water.Visible = false;
                }

                /* Set the applet location */
                setAppletLocation();

              
                twentyFourHGraph.ImageUrl = "~/Applet/" + imei + "/graph_2.png";
                fiveHGraph.ImageUrl = "~/Applet/" + imei + "/graph_1.png";
            }
            catch (Exception eee)
            {
                debug.Width = 200;
                debug.Height = 200;
                debug.Text = eee.Message +"\n";
                debug.Text += eee.StackTrace;
                //Response.Redirect("~/ErrorPage.aspx");
            }
        }

        private int getIndexForLocation(List<Location> locs, String location)
        {
            int i = 0;
            foreach (Location l in locs)
            {
                if (l.imei.Equals(location))
                {
                    return i;
                }
                i++;
            }
            return -1;
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

        private void populateLocationScrollbar(List<Location> loc)
        {
            int backup = ddlWhere.SelectedIndex;
            ddlWhere.Items.Clear();
            int IndexToSelect = 255;
            foreach (Location l in loc)
            {
                ddlWhere.Items.Add(l.Name);
            }
            if (IndexToSelect != 255)   // ehm, what?
            {
                ddlWhere.SelectedIndex = IndexToSelect;
            }
            else
            {
                ddlWhere.SelectedIndex = backup;
            }
        }

        /* Set the correct arguments to the applet */
        private void setAppletLocation()
        {
            String dbToUse = "";
            dbToUse = "Surfvind_data";
            WindData wd = new WindData(true, dbToUse);
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

            List<Location> loc = wd.GetLocations();
            loc.Sort();

            String html;
            if (loc.Count > 0)
            {
                html = "http://www.surfvind.se/Applet.aspx?location=" + loc.ToArray()[ddlWhere.SelectedIndex].imei.ToString();

                applet.Attributes["src"] = html;
            }
        }
        /*
        public void ddlWhere_SelectedIndexChanged(object sender, EventArgs e)
        {
            
            DropDownList drop = sender as DropDownList;
            String sensorToShow = "";
            if (locations.Count > 0 & drop.SelectedIndex != -1 && drop.SelectedIndex < locations.Count && OldSelectedIndex != -1)
            {
                
                sensorToShow = locations.ToArray()[OldSelectedIndex].imei.ToString();
               //Server.tr
            }
            //Response.RedirectLocation = "http://www.surfvind.se/location=12345";
            //Response.
            Response.Redirect("./Default.aspx?location=" +sensorToShow);
        }
        */
    }
}
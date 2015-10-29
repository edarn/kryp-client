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
        #endregion

        protected void LogTime()
        {
            Logger.LogInfo("Duration: " + (DateTime.Now - Start).ToString());
            Start = DateTime.Now;
        }
       public String imei = "";
        protected void Page_Load(object sender, EventArgs e)
        {
            String location = Request.QueryString["location"];
         
            Start = DateTime.Now;
            Response.Cache.SetCacheability(System.Web.HttpCacheability.NoCache);
            try
            {
               SurfvindDataConnection windData = new SurfvindDataConnection();
                
                List<Location> allWeatherStations = windData.GetLocations();
                allWeatherStations.Sort();

                /* Populate location scrollbar */
                populateLocationScrollbar(allWeatherStations);
                if (allWeatherStations.Count > 0)
                {
                    imei = allWeatherStations.ToArray()[ddlWhere.SelectedIndex].imei.ToString();
                }
                if (location != null)
                {
                    int index = getIndexForLocation(allWeatherStations, location);
                    if (index >= 0)
                    {
                        ddlWhere.SelectedIndex = index;
                        imei = location;
                    }
                }
                windData.SetImei(imei);
                /* Set google map location */
                addGMap(allWeatherStations[ddlWhere.SelectedIndex]);
       
                GenGraphs graphGenerator = new GenGraphs();
                graphGenerator.generateSensorImages(imei, windData);
               

                /* Get pre-stored direction and speed arrows */
                imgSpeed.ImageUrl = "~/Images/" + imei + "_img_speed.png";
                imgCompass.ImageUrl = "~/Images/" + imei + "_img_compass.png";

                WindRecord wr = windData.GetCurrentWind();
                
                if (wr.Moisture != 0)
                {
                    // Set temp images
                    air_temp.ImageUrl = "~/Images/" + imei + "_img_air_temp.png";
                    power.Text = wr.AverageWaterTemp + " V";
                    moisture.Text = wr.Moisture + " %";

                    air_temp.ToolTip = "Air temperature: " + wr.AverageAirTemp + " °C";

                    air_temp_text.Text = wr.AverageAirTemp + " °C";
                    moisture_container.Visible = true;
                  
                    temperature_container.Visible = true;
                }
                else
                {
                    moisture_container.Visible = false;
                
                    temperature_container.Visible = false;
                }

                // Graphs are now generated on demand. 
                graphGenerator.fetchData(2, windData);
                twentyFourHGraph.ImageUrl = graphGenerator.generateGraphOnServer(2,1050,250);
                graphGenerator.fetchData(1, windData);
                fiveHGraph.ImageUrl = graphGenerator.generateGraphOnServer(1,1050,250);
                /* Set the applet location */
                setAppletLocation(windData);

            }
            catch (Exception eee)
            {
                debug.Width = 200;
                debug.Height = 200;
                debug.Text = eee.Message +"\n";
                debug.Text += eee.StackTrace;
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
        private void setAppletLocation(SurfvindDataConnection wd)
        {
            List<Location> loc = wd.GetLocations();
            loc.Sort();
            if (loc.Count > 0)
            {
                applet.Attributes["src"] = "http://www.surfvind.se/Applet.aspx?location=" + loc.ToArray()[ddlWhere.SelectedIndex].imei.ToString(); ;
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
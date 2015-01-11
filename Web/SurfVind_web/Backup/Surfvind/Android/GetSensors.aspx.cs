using System;
using System.Collections;
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
using Surfvind_2011;
using System.Collections.Generic;


    public partial class GetSensors : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            String dbToUse = "Surfvind_data";

            //string test = this.ClientQueryString;

            WindData wd = new WindData(true, dbToUse);
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);
            List<Location> loc = wd.GetLocations();
            foreach (Location l in loc)
            {
                Response.Write("Name:" + l.Name +" Imei:"+l.imei+"\n");
            }
            Response.Write("---\n");
            Response.Write("0=\"1 Hour\"\n");
            Response.Write("1=\"5 Hour\"\n");
            Response.Write("2=\"1 Day\"\n");
            Response.Write("3=\"1 Week\"\n");
            Response.Write("4=\"1 Month\"\n");
            Response.Write("5=\"1 Year\"\n");
            Response.Write("--END--");

        }
    }

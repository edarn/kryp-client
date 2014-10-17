using System;
using System.Collections;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Xml.Linq;
using System.Collections.Generic;
using WindInfo;

using System.Diagnostics;

public partial class Vindinfo : System.Web.UI.MasterPage
{
    protected void Page_Load(object sender, EventArgs e)
    {
        List<Location> loc = getLocations();

         // locations.Items.Clear();
        ularens.InnerHtml = "";
          foreach (Location l in loc)
          {
              ularens.InnerHtml += "<li><a href='./Default.aspx?Location=" + l.imei +"'>" + l.Name + "</a></li>";
          }

        HttpCookie pageLoadedCookie = new HttpCookie("pageLoaded");
        Response.Cookies.Add(pageLoadedCookie);
    }

    private List<Location> getLocations()
    {
        String dbToUse = "";
        dbToUse = "Surfvind_data";

        WindData wd = new WindData(true, dbToUse);
        bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);

        List<Location> loc = wd.GetLocations();
        loc.Sort();

        return loc;
    }

    protected void logoutBtn_Click(object sender, EventArgs e)
    {
        Response.Cookies["login"].Expires = DateTime.Now.AddHours(-1);
        Response.Redirect("~/Login.aspx");
    }
}

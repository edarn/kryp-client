using System;
using System.Collections.Generic;
using System.Web;


public partial class Vindinfo : System.Web.UI.MasterPage
{
    public int testur;
    protected void Page_Load(object sender, EventArgs e)
    {
        List<Location> loc = Helper.getLocations();

        ularens.InnerHtml = "";
        foreach (Location l in loc)
        {
            ularens.InnerHtml += "<li><a href='./Default.aspx?Location=" + l.imei + "'>" + l.Name + "</a></li>";
        }

        HttpCookie pageLoadedCookie = new HttpCookie("pageLoaded");
        Response.Cookies.Add(pageLoadedCookie);
    }

    protected void logoutBtn_Click(object sender, EventArgs e)
    {
        Response.Cookies["login"].Expires = DateTime.Now.AddHours(-1);
        Response.Redirect("~/Login.aspx");
    }
}

using System;

public partial class contacts : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        Response.Cache.SetCacheability(System.Web.HttpCacheability.NoCache);

        /* Which about page */
        string page = Request.QueryString["page"];

        if(page != null && page.Equals("info")) {
            contactsPanel.Visible = false;
        } else {
            contactsPanel.Visible = true;
        }
    }
}

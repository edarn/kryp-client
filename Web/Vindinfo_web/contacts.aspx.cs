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

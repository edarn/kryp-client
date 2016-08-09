using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

namespace Surfvind_2011.CrawlSpace
{
    public partial class ViewCrawlspace : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            CrawlSpaceDatabaseConnection connection = new CrawlSpaceDatabaseConnection("");
            List<Location> list = connection.GetLocations();
            foreach (Location l in list)
            {
                ListItem item = ddlWhere.Items.FindByText(l.Name);
                if (item != null) continue;
                item = new ListItem(l.Name, l.imei);
                ddlWhere.Items.Add(item);
            }
        }
    }
}
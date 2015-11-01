using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Drawing;

namespace Surfvind_2011
{
	public partial class ImageHandler_ImagePage : System.Web.UI.Page
	{
		/*protected void Page_Load(object sender, EventArgs e)
		{
			bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);
			SurfvindDataConnection wd = new SurfvindDataConnection(isMySQL);
			WindRecord currentWind = wd.GetCurrentWind();
			Response.Clear();
			Response.ContentType = "image/png";
			using (Bitmap bmp = Helper.GetWindSpeedPic(currentWind.AverageSpeed, currentWind.MinSpeed, currentWind.MaxSpeed, Server))
			{
				bmp.Save(Response.OutputStream, System.Drawing.Imaging.ImageFormat.Png);
			}
		}*/
	}
}
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;

using System.Drawing;
using System.IO;
using System.Drawing.Imaging;
using System.Configuration;





namespace Surfvind_2011
{
    /// <summary>
    /// Summary description for SmallEmbededDisplay
    /// </summary>
    public class SmallEmbededDisplay : IHttpHandler
    {

        public void ProcessRequest(HttpContext context)
        {
            String imei = context.Request.QueryString["imei"];
            


            SurfvindDataConnection wd = new SurfvindDataConnection();
            //bool isMySQL = true;
            wd.SetImei(imei);
            WindRecord wr = wd.GetCurrentWind();
            
            Bitmap bitmap = new Bitmap(250,100);
            Graphics g = Graphics.FromImage(bitmap);
            
            String text = "Speed: " + wr.AverageSpeed.ToString("F2") + " m/s";
            String text2 = "Direction: " +wr.AverageDirection + " degrees";
            String text3 = "Surfvind.se";

            RectangleF rectH = new RectangleF(1, 5, 250, 30);
            
            RectangleF rect = new RectangleF(1, 40, 250, 30);
            RectangleF rect2 = new RectangleF(1, 70,250, 30);
            StringFormat format = new StringFormat();
            format.Alignment = StringAlignment.Center;
            Font font = new Font(FontFamily.GenericSansSerif, 15, FontStyle.Bold);
            Font font2 = new Font(FontFamily.GenericSansSerif, 20, FontStyle.Bold);
            g.DrawString(text, font, Brushes.Red, rect, format);
            g.DrawString(text2, font, Brushes.Red, rect2, format);
            g.DrawString(text3, font2, Brushes.Blue, rectH, format);
            Image img = null;
           /*
            try
            {
                img = Image.FromFile(HttpContext.Current.Server.MapPath("~/Images/" + imei + "_img_compass.png"));
                
            }
            catch { }
            if (img != null)
            {
                g.DrawImage(img, 10, 10);
            }*/
      

        MemoryStream mem = new MemoryStream();
        bitmap.Save(mem,ImageFormat.Png);

        byte[] buffer = mem.ToArray();

            context.Response.ContentType = "image/png";
            context.Response.BinaryWrite(buffer);
            context.Response.Flush();
        }

        public bool IsReusable
        {
            get
            {
                return false;
            }
        }
    }
}
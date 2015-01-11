<%@ WebHandler Language="C#" Class="WebImage" %>

using System;
using System.Web;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

public class WebImage : IHttpHandler
{
    //png decode: ms-help://MS.MSDNQTR.v90.en/MS.MSDN.v90/MS.NETDEVFX.v35.en/wpf_conceptual/html/3d31d186-af73-47f0-b5a7-c26ae46409a6.htm
    Image GetBitmap(int alpha, HttpRequest Request)
    {
        Image speed = Image.FromFile(Request.MapPath("~/Images/ws_speed.png"));        
        Graphics speedGr = Graphics.FromImage(speed);

        Image arrow = Image.FromFile(Request.MapPath("~/Images/ws_speed_arrow.png"));
        Graphics arrowGr = Graphics.FromImage(arrow);

        Bitmap tempBmp = new Bitmap(speed.Width, speed.Height);
        Graphics tempGr = Graphics.FromImage(tempBmp);

        Matrix X = new Matrix();
        X.RotateAt(alpha, new PointF(speed.Width / 2, speed.Height / 2));
        tempGr.Transform = X;
        tempGr.DrawImage(arrow, new PointF(speed.Width / 2 - arrow.Width / 2 + 2, speed.Height / 2 - arrow.Height + 20));

        speedGr.DrawImage(tempBmp, new Point(0, 0));
        return speed;
    }

    Image GetWindSpeedPic(int windSpeed, HttpRequest Request)
    {        
        // -104 .. 104 == 0..40
        return GetBitmap(-105 + (206) * windSpeed / 40, Request);
    }
    
    public void ProcessRequest (HttpContext context) {
        HttpRequest Request = context.Request;
        HttpResponse Response = context.Response;

        /*
        Response.Clear();
        Response.ContentType = "image/gif";
        
        using (Bitmap bmp = new Bitmap(70, 24))
        {
            using (Graphics graph = Graphics.FromImage(bmp))
            using (Font font = new Font("Tahoma", 13))
            {
                graph.FillRectangle(Brushes.LightGray, 0, 0, 70, 24);
                graph.DrawString("s3dw2", font, Brushes.Black, 10, 1);
                using (Pen pen = new Pen(Brushes.DarkGray, 1))
                {
                    Random rnd = new Random();
                    for (int i = 0; i < 5; i++)
                    {
                        Point[] pnts = new Point[] { new Point(rnd.Next(70), rnd.Next(24)), new Point(rnd.Next(70), rnd.Next(24)) };
                        graph.DrawLines(pen, pnts);
                    }
                }
            }
            bmp.Save(Response.OutputStream, System.Drawing.Imaging.ImageFormat.Gif);            
        }        
        Response.End();
        */
        Response.Clear();
        Response.ContentType = "image/png";       

        using (Image bmp = GetWindSpeedPic(40, Request))
        {            
            //bmp.Save(Response.OutputStream, ImageFormat.Jpeg);
            bmp.Save(Request.MapPath("~/Images/test.png"));
            //bmp.Save(Response.OutputStream, ImageFormat.Tiff);
        }
        Response.End();        
        
    }
 
    public bool IsReusable {
        get {
            return false;
        }
    }

}
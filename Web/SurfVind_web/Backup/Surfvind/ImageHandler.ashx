<%@ WebHandler Language="C#" Class="ImageHandler" %>


using System;
using System.Web;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
public class ImageHandler : IHttpHandler {

    //png decode: ms-help://MS.MSDNQTR.v90.en/MS.MSDN.v90/MS.NETDEVFX.v35.en/wpf_conceptual/html/3d31d186-af73-47f0-b5a7-c26ae46409a6.htm
    Image GetBitmap(int alpha, HttpRequest Request)
    {
        Bitmap b = new Bitmap(Request.MapPath("~/Images/zzz.gif"));
        Graphics grb = Graphics.FromImage(b);
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
        Response.Clear();
        Response.ContentType = "image/png";

        using (Image bmp = GetWindSpeedPic(40, Request))
        {
            bmp.Save(Response.OutputStream, ImageFormat.Gif);
        }
        Response.End();   
    }
 
    public bool IsReusable {
        get {
            return true;
        }
    }

}
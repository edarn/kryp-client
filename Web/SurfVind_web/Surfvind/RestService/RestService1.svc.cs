using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using System.IO;
using System.ServiceModel.Web;
using System.Drawing;

namespace Surfvind_2011

{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "RestService1" in code, svc and config file together.
    public class RestService1 : IRestService1
    {
        public string JsonData(string imei)
        {
            return "Dicken " + imei;
        }
        public Stream GetImage()
        {
            var m = new MemoryStream();
            //Fill m

            Bitmap bmp = new Bitmap(50, 50);
            Graphics g = Graphics.FromImage(bmp);
            g.FillRectangle(Brushes.Green, 0, 0, 50, 50);
            g.Dispose();

            
            bmp.Save(m, System.Drawing.Imaging.ImageFormat.Png);
            bmp.Dispose();
            


            // very important!!! otherwise the client will receive content-length:0
            m.Position = 0;

            WebOperationContext.Current.OutgoingResponse.ContentType = "image/png";
            WebOperationContext.Current.OutgoingResponse.ContentLength = m.Length;
            return m;
        }

        public void SendImage(RequestData request, string imei)
        {
            Console.Out.WriteLine(request);

            /*
            image.Position = 0;
            Bitmap bmp = new Bitmap(image);

            bmp.Save(System.Web.HttpContext.Current.Server.MapPath("~/Images/" + imei + "_ScreenShot.png"), System.Drawing.Imaging.ImageFormat.Png);
            bmp.Dispose();
            */
        }
    }
}

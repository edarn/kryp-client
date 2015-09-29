using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using System.IO;
using System.ServiceModel.Web;
using System.Drawing;
using System.Configuration;
using Surfvind_2011.CrawlSpace;

namespace Surfvind_2011

{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "RestService1" in code, svc and config file together.
    public class RestService1 : IRestService1
    {
        public CrawlSpaceMeasurements GetWindData(string imei)
        {
            CrawlSpaceMeasurements result = new CrawlSpaceMeasurements();

            String dbToUse = "";
            dbToUse = "Krypgrund_data";
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);
            CrawlSpaceDatabaseConnection csdc = new CrawlSpaceDatabaseConnection(isMySQL, dbToUse);

            List<Location> loc = csdc.GetLocations();
            loc.Sort();
            csdc.SetImei(imei);

            DateTime endInterval = DateTime.Now;
            DateTime beginInterval = DateTime.Now.AddDays(-365);
            result =  csdc.GetListBetweenDate(beginInterval,endInterval);

            return result;
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

        public string PostSurfvindMeasurements(SurfvindData request, string imei)
        {
            String dbToUse = "";
            dbToUse = "Surfvind_data";
            bool isMySQL = Convert.ToBoolean(ConfigurationManager.AppSettings["isMySQL"]);
            WindData wd = new WindData(isMySQL, dbToUse);

            String result = wd.InsertData(request);
            return result;
        }
    }
}

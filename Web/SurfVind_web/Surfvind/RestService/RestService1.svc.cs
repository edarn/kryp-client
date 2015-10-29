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
        private String ONE_HOUR = "OneHour";
        private String FIVE_HOURS = "FiveHours";
        private String ONE_DAY ="OneDay";
        private String ONE_MONTH ="OneMonth";
        private String ONE_YEAR ="OneYear";
       
        private TimeInterval ValidateInput(string timeInterval)
        {
            if (timeInterval.Equals(ONE_HOUR))
            { return TimeInterval.OneHour; }
            if (timeInterval.Equals(FIVE_HOURS))
            { return TimeInterval.FiveHours; }
            if (timeInterval.Equals(ONE_DAY))
            { return TimeInterval.OneDay; }
            if (timeInterval.Equals(ONE_MONTH))
            { return TimeInterval.OneMonth; }
            if (timeInterval.Equals(ONE_YEAR))
            { return TimeInterval.OneYear; }
            else
            {
                return TimeInterval.Invalid;
            }
        }

        public CrawlSpaceMeasurements GetCrawlSpaceData(string imei, string timeInterval)
        {
            CrawlSpaceMeasurements result = new CrawlSpaceMeasurements();

            TimeInterval interval = ValidateInput(timeInterval);
            if (interval == TimeInterval.Invalid) return null;
  
            CrawlSpaceDatabaseConnection csdc = new CrawlSpaceDatabaseConnection();

            csdc.SetImei(imei);
            result =  csdc.GetMeasurements(interval);

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
            SurfvindDataConnection wd = new SurfvindDataConnection();

            String result = wd.InsertData(request);
            return result;
        }

        public SurfvindMeasurements GetWeatherData(string imei, string timeInterval)
        {
            SurfvindMeasurements result = new SurfvindMeasurements();

            TimeInterval interval = ValidateInput(timeInterval);
            if (interval == TimeInterval.Invalid) return null;

            SurfvindDataConnection sdc = new SurfvindDataConnection();

            sdc.SetImei(imei);
            result = sdc.GetMeasurements(interval);

            return result;
        }
    }
}

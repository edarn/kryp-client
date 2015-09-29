using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using System.ServiceModel.Web;
using System.IO;

namespace Surfvind_2011
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "IRestService1" in both code and config file together.
    [ServiceContract]
    public interface IRestService1
    {
        [OperationContract]
        [WebInvoke(Method = "GET",
                    ResponseFormat = WebMessageFormat.Json,
                    UriTemplate = "{imei}/GetMeasurements")]
        CrawlSpaceMeasurements GetWindData(string imei);

        [OperationContract]
        [WebInvoke(Method = "POST",
                    ResponseFormat = WebMessageFormat.Json,
                    RequestFormat = WebMessageFormat.Json,
                    UriTemplate = "{imei}/PostSurfvindMeasurements")]
        string PostSurfvindMeasurements(SurfvindData request, string imei);
    }


    [DataContract]
    public class SurfvindData
    {

        [DataMember]
        public List<SurfvindMeasurement> surfvindMeasurements;
        [DataMember]
        public string id;
        [DataMember]
        public string version;
    }


    [DataContract]
    public class SurfvindMeasurement
    {
        [DataMember]
        public float windSpeedMin { get; set; }
        [DataMember]
        public float windSpeedAvg { get; set; }
        [DataMember]
        public float windSpeedMax { get; set; }
        [DataMember]
        public float windDirectionMin { get; set; }
        [DataMember]
        public float windDirectionAvg { get; set; }
        [DataMember]
        public float windDirectionMax { get; set; }

        [DataMember]
        public float onBoardHumidity { get; set; }
        [DataMember]
        public float onBoardTemperature { get; set; }

        [DataMember]
        public float firstExternalHumidity { get; set; }
        [DataMember]
        public float firstExternalTemperature { get; set; }

        [DataMember]
        public float batteryVoltage { get; set; }
        [DataMember]
        public string timeStamp { get; set; }

        [DataMember]
        public float rainSensor { get; set; }
    }

    [DataContract]
    public class CrawlSpaceMeasurements
    {
        public CrawlSpaceMeasurements()
        {
            TimeStamp = new List<string>();
            AbsolutFuktInne = new List<int>();
            AbsolutFuktUte = new List<int>();
            FuktInne = new List<float>();
            FuktUte = new List<float>();
            TempInne = new List<float>();
            TempUte = new List<float>();
            FanOn = new List<int>();
        }
        [DataMember]
        public List<String> TimeStamp { get; set; }

        [DataMember]
        public List<int> AbsolutFuktInne { get; set; }

        [DataMember]
        public List<int> AbsolutFuktUte { get; set; }

        [DataMember]
        public List<float> FuktUte { get; set; }

        [DataMember]
        public List<float> FuktInne { get; set; }

        [DataMember]
        public List<float> TempInne { get; set; }

        [DataMember]
        public List<float> TempUte { get; set; }

        [DataMember]
        public List<int> FanOn { get; set; }


    }

}


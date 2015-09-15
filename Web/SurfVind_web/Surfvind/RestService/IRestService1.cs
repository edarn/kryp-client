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
                    UriTemplate = "{imei}/json")]
        string JsonData(string imei);

        [OperationContract]
        [WebInvoke(Method = "POST",
                    ResponseFormat = WebMessageFormat.Json,
                    RequestFormat = WebMessageFormat.Json,
                    UriTemplate = "{imei}/send")]
        void SendImage(RequestData request, string imei);


    }
    [DataContract]
    public class RequestData
    {
        [DataMember]
        String First { get; set; }
        [DataMember]
        String Second { get; set; }
    }
}


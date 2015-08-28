<%@ Page Language="C#" AutoEventWireup="True" CodeBehind="Default.aspx.cs" Inherits="Surfvind_2011.Default"
    Trace="false" EnableSessionState="true" EnableViewState="true" %>

<%@ Register Assembly="GMaps" Namespace="Subgurim.Controles" TagPrefix="cc1" %>
<%@ Register Assembly="System.Web.Extensions" Namespace="System.Web.UI" TagPrefix="asp" %>
<%@ OutputCache Location="None" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head id="Head1" runat="server">
    <title>Windsurfing. Weather report</title>
    <meta name="viewport" http-equiv="cache-control" content="no-cache, user-scalable=no" />
    <link rel="stylesheet" href="css/StyleSheet1.css" type="text/css" />
</head>
<body onunload="GUnload()">
    <form id="form1" runat="server">
    <%-- set AsyncPostBackTimeOut="86400" (== 1st Day) second to sesolve problem with "Sys.WebForms.PageRequestManagerTimeoutException: The server request timed out." error  --%>
    <asp:ScriptManager ID="MainScriptManager" runat="server" EnablePartialRendering="true"
        EnablePageMethods="true" ScriptMode="Auto" AsyncPostBackTimeout="86400">
    </asp:ScriptManager>
  
    <div style="position: absolute; left: 0; top: 0; z-index: 0;">
        <img src="design/ws_head.png" alt="Windsurfing. Weather report" />
    </div>
    <div style="position: absolute; left: 0; top: 250px; z-index: 0;">
        <%--<img src="design/ws_background_sea.png" />--%>
    </div>
    <div style="position: absolute; left: 0; top: 1250px; z-index: 0;">
        <asp:TextBox ID="debug" runat="server" />
    </div>
            <div style="position: absolute; left: 19px; top: 203px; z-index: 1;">
                <table style="background: url('Images/ws_compass.png') 0 0 no-repeat; width: 127px;
                    height: 127px;">
                    <tr>
                        <td>
                            <asp:Image ID="imgCompass" runat="server" AlternateText="Wind direction" />
                        </td>
                    </tr>
                </table>
            </div>
            <div style="position: absolute; left: 230px; top: 203px; z-index: 1;">
                <table style="background: url('Images/ws_speed.png') 0 0 no-repeat; width: 177px;
                    height: 177px;">
                    <tr>
                        <td>
                            <asp:Image ID="imgSpeed" runat="server" AlternateText="Wind speed" />
                        </td>
                    </tr>
                </table>
            </div>
            <div id="temperature_container" runat="server" style="position: absolute; left: 450px; top: 203px;
                z-index: 1;">
                <table  runat="server" >
                    <tr>
                                <td>
                                    Air Temperature
                                </td>
                            </tr>
                    <tr style="background: url('Images/temp_air_new.PNG') 0 0 no-repeat; width: 53px;
                    height: 180px;">
                        <td>
                            <asp:Image ID="air_temp" runat="server" AlternateText="Air temp" />
                        </td>
                    </tr>
                    <tr>
                                <td>
                                    <asp:Label ID="air_temp_text" runat="server" />
                                </td>
                            </tr>
                </table>
            </div>
            <div id="moisture_container" runat="server" 
                style="position: absolute;  left:  580px; top: 200px; width: 140px;"          >
                <table>
                    <tr> 
                       <td> Humidity</td>
                    </tr>
                    
                    <tr >
                        <td align=center>
                            <asp:Image ID="moisture_image" runat="server" AlternateText="moisture" Height="50px"
                                ImageUrl="design/water_drop.png"/>
                        </td>
                        <td>
                            <asp:Label ID="moisture" runat="server" Text="52 %" />
                        </td>
                    </tr>
                    </div>
                    <div ID="battery_container" runat="server">
                    </tr>
                                        <tr>
                        <td>
                            Battery</td>
                                            <tr >
                                                <td>
                                                    <asp:Image ID="water_temp" runat="server" AlternateText="Water temp" 
                                                        ImageUrl="Images/battery_icon.png" Width="50px" />
                                                </td>
                                                <td>
                                                    <asp:Label ID="power" runat="server" Text="12 V" />
                                                </td>
                                            </tr>
                    </tr>
                    </div>

                </table>
            </div>
        
    
    <div style="position: absolute; left: 750px; top: 200px; width: 430px; z-index: 1;">
        <cc1:GMap ID="GMap1" runat="server" Height="270" Width="410" />
    </div>
    <div style="position: absolute; left: 950px; top: 10px; width: 470px; z-index: 2;">
        <span style="color: red">Contact:</span> thomas@tna.se
    </div>
    <div style="position: absolute; left: 50px; top: 424px; z-index: 2;">
        Location:
        <asp:DropDownList ID="ddlWhere" runat="server" AutoPostBack="true" />
    </div>
    <!--OnSelectedIndexChanged=ddlWhere_SelectedIndexChanged-->
    <!--        <IFRAME id="applet" runat="server" title="Applet" style="position: absolute; left: 32px; top: 500px; z-index: 2; text-align: center;" 
            height="100%" width="100%" frameborder="0"
            src="">
            your browser does not support IFRAMEs!
        </IFRAME>
-->
    <div style="position: absolute; left: 32px; top: 500px;">
        <asp:Image ID="twentyFourHGraph" runat="server" AlternateText="24h interval" />
    </div>
    <div style="position: absolute; left: 32px; top: 750px;">
        <asp:Image ID="fiveHGraph" runat="server" AlternateText="5h interval" />
    </div>
    <div style="position: absolute; left: 1100px; top: 203px; z-index: 1;">
        <%--<img src="design/banner.png" alt="Banner" />--%>
        <script type="text/javascript">
            google_ad_client = "pub-0138803691600797";
            /* Surfvind */
            google_ad_slot = "5711094823";
            google_ad_width = 160;
            google_ad_height = 600;    
        </script>
        <script type="text/javascript" src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
        </script>
    </div>
    <div style="position: absolute; left: 132px; top: 1000px; z-index: 1;">
        <%--<img src="design/banner.png" alt="Banner" />--%>
        <script type="text/javascript"><!--
            google_ad_client = "pub-0138803691600797";
            /* 728x90, skapad 2009-04-05 */
            google_ad_slot = "0510411231";
            google_ad_width = 728;
            google_ad_height = 90;
                //-->
        </script>
        <script type="text/javascript" src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
        </script>
    </div>
    <div style="width: 640px; position: absolute; left: 450px; top: 1100px; z-index: 3;
        font-size: 14px;">
        &copy; 2008 Surfvind.se
        <br />
        <br />
        <br />
        <br />
        <br />
   <!--     <asp:Timer ID="tmrUpdateCurrentValues" Enabled="true" Interval="30000" runat="server" /> -->
    </div>
   <!-- <script type="text/javascript">
        var c = 0;
        if (window.attachEvent)
            window.attachEvent("onload", PageLoad);
        else if (window.addEventListener)
            window.addEventListener("load", PageLoad, false);
        function PageLoad() {
            window.setTimeout(initpr, 30000);
        }

        function initpr() {
            var prm = Sys.WebForms.PageRequestManager.getInstance();
            prm.add_pageLoaded(pageLoaded);
            //prm.add_endRequest(requestEndHandler);
        }

        function pageLoaded(sender, args) {
            window.setTimeout(pageLoadedDelayed, 30000);
        }

        function pageLoadedDelayed() {
            var imgCompass = $get("imgCompass");
            var imgSpeed = $get("imgSpeed");
            c++;
            imgCompass.src = imgCompass.src + "?" + c;
            imgSpeed.src = imgSpeed.src + "?" + c;
        }	
    </script> -->
    </form>
    <script type="text/javascript">
        var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
        document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>
    <script type="text/javascript">
        var pageTracker = _gat._getTracker("UA-4530751-1");
        pageTracker._initData();
        pageTracker._trackPageview();
    </script>
</body>
</html>

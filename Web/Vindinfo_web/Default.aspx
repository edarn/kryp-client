<%@ Page Language="C#" MasterPageFile="~/Vindinfo.master" AutoEventWireup="true"
    CodeFile="Default.aspx.cs" Inherits="Default" Title="Weather info" %>

<%@ Register Assembly="GMaps" Namespace="Subgurim.Controles" TagPrefix="cc1" %>
<%@ OutputCache Location="None" %>
<asp:Content ID="mainPage" ContentPlaceHolderID="contentMain" runat="server">
    <br />
    <br />
    <br />
    <br />
    <br />
    <div>
        <asp:Panel ID="WelcomePage" runat="server">
            <img id="flower" src="./Images/welcome_pic.jpg" alt="" style="position: absolute;
                left: 150px; border-style: none; top: 100px; height: 570px;" onload="setOpacity('flower', 0.2);" />
            <a href="./Default.aspx">
                <img id="Locations" src="./Images/weather_btn.png" alt="" style="position: absolute;
                    left: 760px; width: 100px; border-style: none" onmouseover="zoomIn('Locations'); show('qm_weather_info', 1000, true)"
                    onmouseout="zoomOut('Locations'); hide('qm_weather_info');" onmousemove="setPosition('qm_weather_info')" />
            </a>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <a href="./Contacts.aspx">
                <img id="Contacts" src="./Images/contacts_btn.png" alt="" style="position: absolute;
                    left: 850px; width: 100px; border-style: none;" onmouseover="zoomIn('Contacts'); show('qm_contacts_info', 1000, true);"
                    onmouseout="zoomOut('Contacts'); hide('qm_contacts_info');" onmousemove="setPosition('qm_contacts_info')" />
            </a>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <a href="./Download.aspx">
                <img id="Download" src="./Images/download_btn.png" alt="" style="position: absolute;
                    left: 800px; width: 100px; border-style: none;" onmouseover="zoomIn('Download'); show('qm_download_info', 1000, true);"
                    onmouseout="zoomOut('Download'); hide('qm_download_info')" onmousemove="setPosition('qm_download_info');" />
            </a>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <!-- <label id="qm_weather_info" style="background-color: Transparent; color:Blue; visibility: hidden;">Quick menu to Weather information</label>
            <label id="qm_download_info" style="background-color: Transparent; color:Blue; visibility: hidden;">Quick menu to Downloads area</label>
            <label id="qm_contacts_info" style="background-color: Transparent; color:Blue; visibility: hidden;">Quick menu to Contacts area</label> -->
        </asp:Panel>
    </div>
    <asp:Panel ID="LocationPage" runat="server">
        <center>
            <asp:Label ID="Title_loc" runat="server" Style="font-size: xx-large"></asp:Label></center>
        <table runat="server" id="graphics" width="75%">
            <tr>
                <td> 
                <table>
                    <tr>
                        <td align="center" style="background: url('Images/ws_compass.png') no-repeat center; width: 177px;
                            height: 127px; vertical-align: top;">
                            <asp:Image align="center" ID="imgCompass" runat="server" AlternateText="Wind direction" />
                        </td>
                    </tr>
                    <tr>
                        <td align="center" style="background: url('Images/ws_speed.png') 0 0 no-repeat; width: 177px; height: 177px;
                            vertical-align: top;">
                            <asp:Image ID="imgSpeed" runat="server" AlternateText="Wind speed" />
                        </td>
                    </tr>
                    </table>
                </td>
                    <td>
                        <cc1:GMap ID="GMap1" runat="server" Height="300" Width="600" />
                    </td>
                
            </tr>
        </table>
        <!-- <div onmouseover="show('gmap_help', 2500, true);" onmouseout="hide('gmap_help')" onmousemove="setPosition('gmap_help');">
       <label id="gmap_help" style="background-color: Transparent; color:Blue; visibility: hidden;">Double click left mouse button to zoom in<br /> Double click right mouse button to zoom out</label>
    </div> -->
        <br />
        <div style="width: 100%; height: 100%;">
            <iframe id="slideshow" runat="server" title="slideshow" height="300px" width="1000px"
                frameborder="0" src="">your browser does not support IFRAMEs! </iframe>
        </div>
        <br />
        <br />
        <center>
        <iframe src="http://www.facebook.com/plugins/like.php?href=<%Response.Write(Request.Url.ToString());%>&amp;layout=standard&amp;show_faces=true&amp;width=450&amp;action=like&amp;colorscheme=light&amp;height=80&fb_source=Vindinfo&fb_ref=Vindinfo.se"
            scrolling="no" frameborder="0" style="border: none; overflow: hidden; width: 450px;
            height: 80px;" allowtransparency="true"></iframe></center>
        <br />
        <br />
    </asp:Panel>
</asp:Content>

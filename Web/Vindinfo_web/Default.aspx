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
    </div>
    <asp:Panel ID="LocationPage" runat="server">
        <table runat="server" id="graphics" width="85%">
            <tr>
                <td>
                    <table>
                        <tr>
                            <td align="center" style="background: url('Images/ws_compass.png') no-repeat center;
                                width: 177px; height: 127px; vertical-align: top;">
                                <asp:Image align="center" ID="imgCompass" runat="server" AlternateText="Wind direction" />
                            </td>
                        </tr>
                        <tr>
                            <td align="center" style="background: url('Images/ws_speed.png') 0 0 no-repeat; width: 177px;
                                height: 177px; vertical-align: top;">
                                <asp:Image ID="imgSpeed" runat="server" AlternateText="Wind speed" />
                            </td>
                        </tr>
                    </table>
                </td>
                <td>
                    <table>
                        <tr>
                            <td>
                                <center>
                                    <asp:Label ID="Title_loc" runat="server" Style="font-size: xx-large"></asp:Label></center>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <cc1:GMap ID="GMap1" runat="server" Height="300" Width="500" />
                            </td>
                        </tr>
                    </table>
                </td>
                <td>
                    <div id="temperature_container" runat="server">
                        <table id="Table1" runat="server" >
                             <tr>
                                <td>
                                    Air Temperature
                                </td>
                            </tr>
                            <tr style="background: url('Images/temp_air_new.PNG') 0 0 no-repeat;
                            width: 53px; height: 180px;">
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
                </td>
                <td>
                    <div id="water" runat="server">
                        <table>
                            <tr>
                                <td>
                                    Humidity
                                </td>
                            </tr>
                            <tr>
                                <td align="center">
                                    <asp:Image ID="moisture_image" runat="server" AlternateText="moisture" Height="50px"
                                        ImageUrl="design/water_drop.png" />
                                </td>
                                <td>
                                    <asp:Label ID="moisture" runat="server" Text="52 %" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    Battery
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <asp:Image ID="water_temp" runat="server" AlternateText="Water temp" ImageUrl="design/battery_icon.png"
                                        Width="50px" />
                                </td>
                                <td>
                                    <asp:Label ID="power" runat="server" Text="12 V" />
                                </td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
        </table>
        <br />
        <br />
        <br />

        <iframe id="graphview" runat="server" 
                scrolling="no" frameborder="0" style="border: none; overflow: hidden; width: 890px;
                height: 240px;"></iframe>
        <center>
         <br />
        <br />
        <br /> <br />
        <br />
        
            <iframe src="http://www.facebook.com/plugins/like.php?href=<%Response.Write(Request.Url.ToString());%>&amp;layout=standard&amp;show_faces=true&amp;width=450&amp;action=like&amp;colorscheme=light&amp;height=80&fb_source=Vindinfo&fb_ref=Vindinfo.se"
                scrolling="no" frameborder="0" style="border: none; overflow: hidden; width: 450px;
                height: 80px;" ></iframe>
        </center>
        <br />
        <br />
    </asp:Panel>
</asp:Content>

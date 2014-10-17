<%@ Page Language="C#" MasterPageFile="~/Vindinfo.master" AutoEventWireup="true" CodeFile="contacts.aspx.cs" Inherits="contacts" Title="Vindinfo::Contacts" %>
<asp:Content ID="mainPage" ContentPlaceHolderID="contentMain" runat="server">
    <br />
    <br />
    <br />
    <br />
    <br />
    <br />
    <br />
    <!-- <div>
        <asp:Panel ID="commercialPanel" runat="server">
            <div style="text-align:center; font-size: xx-large;">
                <asp:Label ID="WindMasterTitle" runat="server" Text="Wind Master"></asp:Label> <br /> <br />
                <img src="./Images/Windmaster1.PNG" alt="" />
                <br /> <br />
                <img src="./Images/Windmaster2.PNG" alt="" />
            </div>
        </asp:Panel>
    </div> -->

    <asp:Panel ID="contactsPanel" runat="server">
        <div style="text-align: center;">
        <center>
        <h2>Contacts:</h2>
        <table border="4" style="text-align:center;"> 
            <tr><td>Thomas Hermansson </td></tr>
            <tr><td>TNA Software AB </td></tr>
            <tr><td>Jakthornsgränden 22 </td></tr>
            <tr><td>226 52  Lund </td></tr>
            <tr><td>Mail: <a href="mailto:thomas@tna.se">thomas@tna.se</a> </td></tr>
            <tr><td>Phone: +46 (0) 702-313255 </td></tr>
        </table>
        </center>
        </div>
    </asp:Panel>
    <br /> <br />
</asp:Content>


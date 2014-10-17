<%@ Page Language="C#" AutoEventWireup="true" Inherits="ErrorPage" Codebehind="ErrorPage.aspx.cs" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Windsurfing. oops...</title>
    <link rel="stylesheet" href="css/css.css" type="text/css"/>
</head>
<body onload="waitForRedirect()">
    <form id="form1" runat="server">
    <div>
    
        <asp:ImageButton ID="Image" runat="server"
            ImageUrl="~/design/ws_head.png"
            PostBackUrl="~/Default.aspx" />
    
    </div>
        <asp:Label ID="ErrorLabel" runat="server" BorderStyle="None" Font-Bold="True" 
            Font-Size="Large" ForeColor="Black"
            Text="Sorry, we're experiencing some problems right now..." 
            Height="22px" Width="556px"></asp:Label>
    
    <script type="text/javascript">
    <!--
    function waitForRedirect() {
        setTimeout("window.location = './Default.aspx'", 70000);
    }
    //-->
    </script>
    
    </form>
</body>
</html>

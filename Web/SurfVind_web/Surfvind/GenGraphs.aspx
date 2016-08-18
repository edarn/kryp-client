<%@ Page Language="C#" AutoEventWireup="true" Inherits="Surfvind_2011.GenGraphs" Codebehind="GenGraphs.aspx.cs" %>
<%@ Register Assembly="System.Web.Extensions" Namespace="System.Web.UI" TagPrefix="asp" %>

<html>
<head id="Head1" runat="server">
    <title>Weather report</title>
</head>
<body>
     <div  id="placeholder" runat="server"></div>
</body>

    <script src="https://www.gstatic.com/firebasejs/3.2.1/firebase.js"></script>
<script>
  // Initialize Firebase
  var config = {
    apiKey: "AIzaSyAQ93BuMzxnGt_rFR01xTVhHo0TQTBEp1E",
    authDomain: "surfvind.firebaseapp.com",
    databaseURL: "https://surfvind.firebaseio.com",
    storageBucket: "surfvind.appspot.com",
  };
  firebase.initializeApp(config);
</script>
</html>
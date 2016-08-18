<html>
<head id="head1" runat="server">
    <title>Download As...</title>
    <meta http-equiv="cache-control" content="no-cache"/>
</head>
<body>
    <% 
        Dim doc As String
        doc = request.querystring("file")

        if doc Is Nothing Then
            Exit Sub
        Else
            On Error Resume Next
            Response.ContentType = "application/pdf"
            Response.AppendHeader("Content-Disposition", "attachment; filename=" +doc)
            Response.TransmitFile(Server.MapPath("~/Documents/" +doc))
            If Err.Number = 0 Then
                Response.End()
            End If
        End If

        

    %>
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

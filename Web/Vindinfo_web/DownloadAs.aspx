<html>
<head id="head1" runat="server">
    <title>Download As...</title>
    <meta http-equiv="cache-control" content="no-cache"/>
</head>
<body>
    <% 
        Dim file As String
        file = Request.QueryString("file")
        
        If file Is Nothing Then
            Exit Sub
        Else
            On Error Resume Next
            If (file.EndsWith(".pdf")) Then
                Response.ContentType = "application/pdf"
            Else
                Response.ContentType = "application/force-download"
            End If
            Response.AppendHeader("Content-Disposition", "attachment; filename=" + file)
            Response.TransmitFile(Server.MapPath("~/Downloadable/" + file))
            If Err.Number = 0 Then
                Response.End()
            End If
            End If

        

    %>
</body>
</html>

<html>
<head id="Head1" runat="server">
    <title>Windsurf applet</title>
    <meta http-equiv="cache-control" content="no-cache"/>
</head>
<body>

<title>Windsurfing applet</title>

<APPLET ARCHIVE="Applet/Applet.jar" CODE="com/surfvind/applet/rendering/View.class" width=900 Height=250 alt="Vindsurf applet">
<param name= "location" value= "<%response.write(request.querystring("location"))%>" name="fade_speed" value="<%response.write(request.querystring("fade_effect"))%>">
</APPLET>

</body>
</html>
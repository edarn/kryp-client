<html>
<head>

<title> Slideshow </title>
    <script language="javascript" type="text/javascript" src="js/dropdown.js"></script>
    <script language="javascript" type="text/javascript" src="js/cool.js"></script>
</head>
<body onload="start()">
    <img id="image0" src="http://www.surfvind.se/Applet/<%response.write(request.querystring("location"))%>/graph_2.png" alt="" style="position:absolute; left:0px; top:0px; visibility:hidden" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>
    <img id="image1" src="http://www.surfvind.se/Applet/<%response.write(request.querystring("location"))%>/graph_1.png" alt="" style="position:absolute; left:0px; top:0px; visibility:hidden" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>
    <img id="image2" src="http://www.surfvind.se/Applet/<%response.write(request.querystring("location"))%>/graph_0.png" alt="" style="position:absolute; left:0px; top:0px; visibility:hidden" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>
    <img id="image3" src="http://www.surfvind.se/Applet/<%response.write(request.querystring("location"))%>/graph_3.png" alt="" style="position:absolute; left:0px; top:0px; visibility:hidden" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>
    <img id="image4" src="http://www.surfvind.se/Applet/<%response.write(request.querystring("location"))%>/graph_4.png" alt="" style="position:absolute; left:0px; top:0px; visibility:hidden" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>
    
    <img src="./design/mediaBar_stop.PNG" id="mediaBar" alt="" style="visibility: hidden; position: absolute; left:50px; top: 150px; width: 900px" onmousedown="handleKeyEvent()" onmouseover="showIcon(); show('mediaBar', 0, false);"  onmouseout="hideAfterDelay('mediaBar')"/>

</body>
</html>
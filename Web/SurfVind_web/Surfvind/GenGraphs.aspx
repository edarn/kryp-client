<%@ Page Language="C#" AutoEventWireup="true" Inherits="Surfvind_2011.GenGraphs" Codebehind="GenGraphs.aspx.cs" %>

<%@ Register Assembly="ZedGraph.Web" Namespace="ZedGraph.Web" TagPrefix="zgw"%>
<zgw:ZedGraphWeb id="zgwCtl" runat="server" width="900" height="250" rendermode="RawImage" onrendergraph="zgwCtl_OnRenderGraph">
</zgw:ZedGraphWeb>

<html>
<head id="Head1" runat="server">
    <title>Windsurfing. Weather report</title>
    <meta http-equiv="cache-control" content="no-cache"/>
</head>
<body>

</body>
</html>
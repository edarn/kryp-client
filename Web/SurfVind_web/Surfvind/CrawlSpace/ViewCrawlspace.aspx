<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="ViewCrawlspace.aspx.cs" Inherits="Surfvind_2011.CrawlSpace.ViewCrawlspace" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>test</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <!--Load the AJAX API-->
    <script type="text/javascript" src="../JavaScript/jquery-1.11.3.js"></script>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">

        // Load the Visualization API and the piechart package.
        google.load('visualization', '1', { 'packages': ['corechart'] });
        // Set a callback to run when the Google Visualization API is loaded.
        google.setOnLoadCallback(drawChart);


        function drawChart() {
            $(document).ready(function () {
                d = document.getElementById("ddlWhere").value;
                //document.getElementById("debug").innerHTML = jsonData.AbsolutFuktInne;

                var jsonData = $.ajax({
                    url: "../RestService/RestService1.svc/" + d + "/CrawlSpaceMeasurements",
                    dataType: "json",
                    async: false
                }).responseText;

                jsonData = JSON.parse(jsonData);

                // Create our data table out of JSON data loaded from server.
                var dataTable1 = new google.visualization.DataTable();
                var dataTable2 = new google.visualization.DataTable();

                dataTable1.addColumn('datetime', 'Tid');
                dataTable1.addColumn('number', 'AbsolutFuktInne');
                dataTable1.addColumn('number', 'AbsolutFuktUte');
                dataTable1.addColumn('number', "FanOn");

                dataTable2.addColumn('datetime', 'Tid');
                dataTable2.addColumn('number', 'FuktInne');
                dataTable2.addColumn('number', 'FuktUte');
                dataTable2.addColumn('number', 'TempInne');
                dataTable2.addColumn('number', 'TempUte');


                for (var i = 0; i < jsonData.AbsolutFuktInne.length; i++) {
                    dataTable1.addRow([new Date(jsonData.TimeStamp[i]), jsonData.AbsolutFuktInne[i], jsonData.AbsolutFuktUte[i], jsonData.FanOn[i]]);
                    dataTable2.addRow([new Date(jsonData.TimeStamp[i]), jsonData.FuktInne[i], jsonData.FuktUte[i], jsonData.TempInne[i], jsonData.TempUte[i]]);
                }

                // Instantiate and draw our chart, passing in some options.
                var chart = new google.visualization.LineChart(document.getElementById('chart1'));
                chart.draw(dataTable1, {
                    width: 1600, height: 800, title: 'Absolut fuktmängd i krypgrund och utomhus - samt fläktinfo.',
                    animation: { duration: 1000, easing: 'out' },
                    legend: { position: 'bottom' },
                    hAxis: { title: 'Time' },
                    colors: ['#FF0000', '#006600', 'yellow', '#00FF00'],
                    vAxes: {
                        0: { logScale: false, title: 'Absolut fuktmängd (centigram/kubikmeter)' },
                        1: { logScale: false, title: 'Fläkt På(=90)/Av(=0)' }
                    },
                    series: {
                        0: { targetAxisIndex: 0 },
                        1: { targetAxisIndex: 0 },
                        2: { targetAxisIndex: 1 }

                    }
                });

                var chart2 = new google.visualization.LineChart(document.getElementById('chart2'));
                chart2.draw(dataTable2, {
                    width: 1600, height: 800, title: 'Relativ luftfuktighet och temperatur i krypgrund och utomhus',
                    hAxis: { title: 'Time' },
                    legend: { position: 'bottom' },
                    colors: ['#0000CC', '#3399FF', '#FF0000', '#FF6600'],
                    vAxes: {
                        0: { logScale: false, title: 'Relativ luftfuktighet (%) ' },
                        1: { logScale: false, title: 'Temperatur (Grader C)' }
                    },
                    series: {
                        0: { targetAxisIndex: 0 },
                        1: { targetAxisIndex: 0 },
                        2: { targetAxisIndex: 1 },
                        3: { targetAxisIndex: 1 }
                    }
                });
            });
        }


    </script>
</head>

<body>
    <center>
        <h2>Krypgrundsstatistik Kollandsvik Stuga 20</h2>
    </center>
    <ul>
        <li>Absolut fuktmängd, i diagram ett, visar total mängd vatten i luften. Enheten är centigram/kubikmeter</li>
        <li>FanOn, i diagram ett, visar om ventilationsfläken är på eller av. Värdet är 90 är fläkten på, om värdet är 10 är fläkten av. (högra Y-axeln)</li>
        <li>FuktInne och FuktUte, i diagram två, är relativ luftfuktighet i procent (vänstra Y-axeln) </li>
        <li>TempInne och TempUte, i diagram två, är grader Celcius (högra Y-axeln) </li>
    </ul>
    <form id="test" runat="server">
        <asp:DropDownList ID="ddlWhere" runat="server" AutoPostBack="true" onclick="drawChart()" />
    </form>
    
    <div id="chart1"></div>
    <div id="chart2"></div>

    <div id="debug"></div>
    <div id="debug2"></div>

</body>
</html>

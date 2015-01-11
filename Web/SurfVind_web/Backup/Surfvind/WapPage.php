<?php
function TNA_QuerySQL($question)
{
	$rs=mysql_query($question);
	if (!$rs)
    {
      exit("Error in SQL $question");
    }
  return $rs;
}
$db_user = 'surfvi_se@s10724';
$db_pass = 'kbi3i6dex9jgmolr';
$db_name = 'surfvind_se';
$db_host = 'mysql118.surfvind.se';

$conn = mysql_connect($db_host,$db_user,$db_pass, "");
if (!$conn)
{
  exit("Connection Failed2: " . $conn);
}
mysql_select_db($db_name);


$rs = TNA_QuerySQL("SELECT * FROM `test_data` ORDER BY `test_data`.`time`  DESC LIMIT 0,1");
$row =mysql_fetch_array($rs);

    $dir = $row['averageDir']*365/1500;
    $dir = round($dir);
    $speed =  $row['averageSpeed'];
    $speed = round($speed,1);
 	 echo "<html><body><H3>V&auml;lkommen till waptj&auml;nsten f&ouml;r surfvind.se!</H3><br>";
   echo "Vindriktning i grader: $dir. <br>0 grader = Norr <br>";
   echo "Vindhastighet i medel: $speed m/s<br> Copyright 2008 Surfvind.se";
?>
<script type=\"text/javascript\">
var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
var pageTracker = _gat._getTracker("UA-4530751-1");
pageTracker._initData();
pageTracker._trackPageview();
</script></body></html>

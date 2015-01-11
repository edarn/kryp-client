<?php
function TNA_QuerySQL($question)
{
    $rs=mysql_query($question);
    if (!$rs)
    {
      echo "Error in SQL $question";
    }
  return $rs;
}
$db_user = '129649_yc97877';
$db_pass = 'Thomtris1';
$db_name = '129649-surfvind';
$db_host = 'surfvind-129649.mysql.binero.se';
$conn = mysql_connect($db_host,$db_user,$db_pass, "");
if (!$conn)
{
  exit("Connection Failed2: " . $conn);
}
mysql_select_db($db_name);
$string = file_get_contents('php://input');

/*
$string = "{\"id\":\"356845053868954\",\"measure\":[{\"TimeStamp\":\"20131027T182412\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":8,\"WindDirectionAvg\":180,\"WindDirectionMin\":180},{\"TimeStamp\":\"20131027T182422\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":16,\"WindDirectionAvg\":360,\"WindDirectionMin\":180},{\"TimeStamp\":\"20131027T182432\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":24,\"WindDirectionAvg\":540,\"WindDirectionMin\":180},{\"TimeStamp\":\"20131027T182442\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":32,\"WindDirectionAvg\":720,\"WindDirectionMin\":180},{\"TimeStamp\":\"20131027T182452\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":40,\"WindDirectionAvg\":900,\"WindDirectionMin\":180},{\"TimeStamp\":\"20131027T182502\",\"WindSpeedMax\":8,\"WindSpeedMin\":8,\"WindDirectionMax\":180,\"WindSpeedAvg\":48,\"WindDirectionAvg\":1080,\"WindDirectionMin\":180}],\"version\":\"IOIO_R1A\"}";
*/

if (!empty($string)){
	$json_a = json_decode($string, true);
	if (!empty($json_a))
	{
		$name = $json_a['SensorName'];
		$latitude = $json_a['Latitude'];
		$longitude =$json_a['Longitude'];
		$imei  = $json_a['Imei'];
		$version =  $json_a['Version'];

		if (!empty($imei) && !empty($name))
		{
			echo TNA_QuerySQL("DELETE FROM `Surfvind_location` WHERE imei=$imei");
			echo TNA_QuerySQL("INSERT INTO `Surfvind_location` SET imei='$imei', Location='$name', Latitiud='$latitude', Longitud='$longitude'");
		}
	}
}
?>


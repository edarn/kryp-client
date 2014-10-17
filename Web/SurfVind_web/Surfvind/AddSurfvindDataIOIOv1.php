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
		$measureArray = $json_a['measure'];
		$imei  = $json_a['id'];
    echo "version: " . $json_a['version']."<br>";
    $version =  $json_a['version'];

		if (!empty($imei) && !empty($measureArray))
		{		
			foreach ($measureArray as $key => $value) {
			    echo "===========================<br>";
			    echo "Timestamp: " . $value['TimeStamp'] ."<br>";
			    $timestamp = $value['TimeStamp'];

          echo "WindDirectionAvg: " . $value['WindDirectionAvg']."<br>";
          $averageDir = $value['WindDirectionAvg'];

          echo "WindDirectionMax: " . $value['WindDirectionMax']."<br>";
			    $maxDir =  $value['WindDirectionMax'];

          echo "WindDirectionMin: " . $value['WindDirectionMin']."<br>";
			    $minDir =  $value['WindDirectionMin'];

          echo "WindSpeedAvg: " . $value['WindSpeedAvg']."<br>";
          $averageSpeed = $value['WindSpeedAvg'];

          echo "WindSpeedMax: " . $value['WindSpeedMax']."<br>";
			    $maxSpeed =  $value['WindSpeedMax'];

          echo "WindSpeedMin: " . $value['WindSpeedMin']."<br>";
			    $minSpeed =  $value['WindSpeedMin'];

          
          echo TNA_QuerySQL("INSERT INTO `Surfvind_data` SET imei=$imei, time='$timestamp',averageDir=$averageDir,maxDir=$maxDir,minDir=$minDir,averageSpeed=$averageSpeed,maxSpeed=$maxSpeed,minSpeed=$minSpeed,version='$version'");
			    echo "<br>";
			}
		}
	}
}
?>


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
function strstr2($haystack, $needle, $before_needle=FALSE) {
 //Find position of $needle or abort
 if(($pos=strpos($haystack,$needle))===FALSE) return FALSE;

 if($before_needle) return substr($haystack,0,$pos+strlen($needle));
 else return substr($haystack,$pos);
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


if (isset($_GET['s2Avg']))
{
  $WaterTemperature =$_GET['s2Avg'];
}
if (isset($_GET['s8Avg']))
{
  $AirTemperature =$_GET['s8Avg'];
}
if (isset($_GET['s7Avg']))
{
  $Moisture =$_GET['s7Avg'];
}

//To get the Habo sensor to work
if (isset($_GET['ad']))
{
  $ad =$_GET['ad'];
}
if (isset($_GET['maxd']))
{
  $maxd =$_GET['maxd'];
}
if (isset($_GET['mind']))
{
  $mind =$_GET['mind'];
}
//end habo





if (isset($_GET['s1Avg']))
{
  $ad =$_GET['s1Avg'];
}
if (isset($_GET['s1Max']))
{
  $maxd =$_GET['s1Max'];
}
if (isset($_GET['s1Min']))
{
  $mind =$_GET['s1Min'];
}
if (isset($_GET['as']))
{
  $as =$_GET['as'];
}
if (isset($_GET['maxs']))
{
  $maxs =$_GET['maxs'];
}
if (isset($_GET['mins']))
{
  $mins =$_GET['mins'];
}
if (isset($_GET['ver']))
{
  $version =$_GET['ver'];
}
if (isset($_GET['imei']))
{
  $imei =$_GET['imei'];
}
if (isset($_GET['batt']))
{
  $batt =$_GET['batt'];
}
else {
  $batt = "";
}


if (isset($_GET['s1Avg']))
{
  $s1 =$_GET['s1Avg'];
}
if (isset($_GET['s2Avg']))
{
  $s2 =$_GET['s2Avg'];
}
if (isset($_GET['s3Avg']))
{
  $s3 =$_GET['s3Avg'];
}
if (isset($_GET['s4Avg']))
{
  $s4 =$_GET['s4Avg'];
}
if (isset($_GET['s1Avg']))
{
  $s5 =$_GET['s5Avg'];
}
if (isset($_GET['s6Avg']))
{
  $s6 =$_GET['s6Avg'];
}
if (isset($_GET['s7Avg']))
{
  $s7 =$_GET['s7Avg'];
}
if (isset($_GET['s8Avg']))
{
  $s8 =$_GET['s8Avg'];
}


//Transform mV values to degrees.

//$maxd = ($maxd * 360) / 2600;
//$mind = ($mind * 360) / 2600;
//$ad = ($ad * 360) / 2600;

$maxd = ($maxd * 360) / 4900;
$mind = ($mind * 360) / 4900;
$ad = ($ad * 360) / 4900;


// average dir calculation is incorrect from sensor. Instead we set it to be in between max and min
if (($maxd - $mind) >180)
{
 $delta = 360 - ($maxd-$mind);
 $delta = $delta/2;
 $ad = ($maxd + $delta) % 360;
}

if (isset($_GET['s1Avg']))
{
  $AirTemperature = $AirTemperature*44.76439791/1000 - 57.3298;
  $WaterTemperature = $WaterTemperature*38.9671/1000 - 39.57;
 // $Moisture = $Moisture*44.62934947/1000 - 41.3192133;
  $Moisture = ($Moisture*0.22943038/1000 - 0.076)*100;
}
$dag = date('Y-m-d');
$klocka = date('H:i:s');

if (isset($_GET['s1Avg']))
{
  TNA_QuerySQL("INSERT INTO `Surfvind_data` SET imei=$imei, time='$dag $klocka',averageDir=$ad,maxDir=$maxd,minDir=$mind,averageSpeed=$as,maxSpeed=$maxs,minSpeed=$mins,version='$version', waterTemp='$WaterTemperature', airTemp='$AirTemperature', moisture='$Moisture', s1='$s1',s2='$s2',s3='$s3',s4='$s4',s5='$s5',s6='$s6',s7='$s7',s8='$s8'");
}
else
{
  TNA_QuerySQL("INSERT INTO `Surfvind_data` SET time='$dag $klocka',averageDir=$ad,maxDir=$maxd,minDir=$mind,averageSpeed=$as,maxSpeed=$maxs,minSpeed=$mins,version='$version'");
}
?>
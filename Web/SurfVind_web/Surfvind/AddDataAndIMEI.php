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


//Transform mV values to degrees.

$maxd = ($maxd * 360) / 1500;
$mind = ($mind * 360) / 1500;
$ad = ($ad * 360) / 1500;


// average dir calculation is incorrect from sensor. Instead we set it to be in between max and min
if (($maxd - $mind) >180)
{
 $delta = 360 - ($maxd-$mind);
 $delta = $delta/2;
 $ad = ($maxd + $delta) % 360;
}

$ad = 360 - $ad;
$maxd = 360 - $maxd;
$mind = 360 - $mind;

$dag = date('Y-m-d');
$klocka = date('H:i:s');
/*TNA_QuerySQL("INSERT INTO debugLog SET log='$logtext',time='$dag $klocka'");
$speed = strchr($logtext,'S');
$speed = substr($speed,1);
$direction = strstr2($logtext,'S',true);
$direction = substr($direction,0,-1);
echo $direction;
$direction = substr($direction,1);
echo $direction;

,battery='$batt'
*/

TNA_QuerySQL("INSERT INTO `Surfvind_data` SET imei=$imei, time='$dag $klocka',averageDir=$ad,maxDir=$maxd,minDir=$mind,averageSpeed=$as,maxSpeed=$maxs,minSpeed=$mins,version='$version'");

?>
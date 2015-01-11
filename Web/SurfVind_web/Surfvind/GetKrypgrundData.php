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
/*
function detectRequestBody(){
    $rawInput = fopen('php://input', 'r');
    $tempStream = fopen('php://temp','r+');
    stream_copy_to_stream($rawInput, $tempStream);
    rewind($tempStream);
    return $tempStream;
}
*/
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

$cols1 = array('id' => 'Time', 'label' =>'Time', 'pattern' => "", 'type' =>'number');
$cols2 = array('id' => 'AbsolutFuktInne', 'label' =>'AbsolutFuktInne', 'pattern' => "", 'type' =>'number');
$cols3 = array('id' => 'AbsolutFuktUte', 'label' =>'AbsolutFuktUte', 'pattern' => "", 'type' =>'number');
$cols4 = array('id' => 'FuktInne', 'label' =>'FuktInne', 'pattern' => "", 'type' =>'number');
$cols5 = array('id' => 'FuktUte', 'label' =>'FuktUte', 'pattern' => "", 'type' =>'number');
$cols6 = array('id' => 'TempInne', 'label' =>'TempInne', 'pattern' => "", 'type' =>'number');
$cols7 = array('id' => 'TempUte', 'label' =>'TempUte', 'pattern' => "", 'type' =>'number');
$cols8 = array('id' => 'FanOn', 'label' =>'FanOn', 'pattern' => "", 'type' =>'number');

$cols = array($cols1,$cols2,$cols3,$cols4,$cols5,$cols6,$cols7,$cols8);

//$twoDaysAgo = date("c" , time() - (3 * 24 * 60 * 60));
$twoDaysAgo = date("Y-m-d" , time() - (60*24* 60 * 60));

if (isset($_GET["imei"]))
{
  $imei = $_GET["imei"];
}
else
{
  $imei = "004402142992548";
}
$imei = "\"" . trim($imei, "\"") . "\"";

$nya = "SELECT * 
FROM ( 
    SELECT 
        @row := @row +1 AS rownum, TimeStamp,AbsolutFuktInne,AbsolutFuktUte,FuktInne,FuktUte,TempInne,TempUte,FanOn
    FROM ( 
        SELECT @row :=0) r, Krypgrund_data WHERE TimeStamp > '$twoDaysAgo' AND Imei='$imei' 
    ) ranked 
WHERE rownum % 50 = 1 ORDER BY TimeStamp DESC";

$rs = TNA_QuerySQL($nya);
//$rs = TNA_QuerySQL("SELECT TimeStamp,AbsolutFuktInne,AbsolutFuktUte,FuktInne,FuktUte,TempInne,TempUte,FanOn FROM `Krypgrund_data` WHERE  TimeStamp > '$twoDaysAgo' AND Imei='$imei' ORDER BY TimeStamp DESC LIMIT 6000");
$rows = array();
while($row = mysql_fetch_array($rs)) {
$dack = strtotime($row['TimeStamp'])*1000;
$date =array('v' => $dack);
$absolutInne = array('v' => $row['AbsolutFuktInne']);
$absolutUte = array('v' => $row['AbsolutFuktUte']);
$fuktInne = array('v' => $row['FuktInne']);
$fuktUte = array('v' => $row['FuktUte']);
$tempInne = array('v' => $row['TempInne']);
$tempUte = array('v' => $row['TempUte']);
if ($row['FanOn'] == '1')
{
    $fanOn = array('v' => '90');
}
else
{
    $fanOn = array('v' => '10');
}

 $rows[] = array('c' => array($date,$absolutInne, $absolutUte, $fuktInne, $fuktUte, $tempInne, $tempUte, $fanOn));
 // do something with the $row
}

$googleData = array('cols' => $cols, 'rows' => $rows);

$json = json_encode($googleData);

echo $json;

?>

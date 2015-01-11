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
$cols2 = array('id' => 'MaxVind', 'label' =>'MaxVind', 'pattern' => "", 'type' =>'number');
$cols3 = array('id' => 'MinVind', 'label' =>'MinVind', 'pattern' => "", 'type' =>'number');
$cols4 = array('id' => 'MedelVind', 'label' =>'MedelVind', 'pattern' => "", 'type' =>'number');

$cols = array($cols1,$cols2,$cols3,$cols4);

//$twoDaysAgo = date("c" , time() - (3 * 24 * 60 * 60));
$twoDaysAgo = date("c" , time() - (8* 60 * 60));

if (isset($_GET["imei"]))
{
  $imei = $_GET["imei"];
}
else
{
  $imei = "358848043355882";
}

$rs = TNA_QuerySQL("SELECT * FROM `Surfvind_data` WHERE  time > '$twoDaysAgo' AND imei='$imei' ORDER BY time");
$rows = array();
$interval = mysql_num_rows($rs)/100;
$a =0;

$a1=0;
$a2=0;
$a3=1000;
$a4=0;


while($row = mysql_fetch_array($rs)) {
 $a1= $row['time'];
 if ($a < $interval)
 {
 if($row['maxSpeed']>$a2)
 {
   $a2= $row['maxSpeed'];
 }
 if($row['minSpeed']<$a3){
   $a3= $row['minSpeed'];
 }
 $a4+= $row['averageSpeed'];
 }
 else
 {
 $a = 0;
 $a4/=$interval;
$dack = strtotime($a1)*1000;
$date =array('v' => $dack);
$b2 = array('v' => $a2);
$b3 = array('v' => $a3);
$b4 = array('v' => $a4);

$a2=0;
$a3=1000;
$a4=0;



 $rows[] = array('c' => array($date,$b2,$b3,$b4));
 // do something with the $row
}
$a=$a+1;
}

$googleData = array('cols' => $cols, 'rows' => $rows);

$json = json_encode($googleData);

echo $json;

?>

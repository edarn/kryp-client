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

$tid = date("Y-m-d",time()-30*24*60*60);

$rs = TNA_QuerySQL("SELECT DISTINCT Krypgrund_data.Imei, Surfvind_location.Location FROM `Krypgrund_data` INNER JOIN Surfvind_location ON Krypgrund_data.imei=Surfvind_location.imei WHERE TimeStamp > '$tid'");
$rows = array();
while($row = mysql_fetch_array($rs)) {
$rows[] = array('Imei' => trim($row['Imei'],"\""), 'Location' => $row['Location']);

 // do something with the $row
}

$json = json_encode($rows);

echo $json;

?>

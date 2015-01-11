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


$rs = TNA_QuerySQL("SELECT * FROM `wind_users`");
while ($row =mysql_fetch_array($rs))
  {
    $name = $row['name'];
    $nbr =  $row['phonenbr'];
    $nbrSMSLeft =  $row['SMS'];
	$nbrSMSLeft--;
	echo ";$name$nbr&\n";
	TNA_QuerySQL("UPDATE `wind_users` SET SMS='$nbrSMSLeft' WHERE phonenbr ='$nbr'");
  }   

?>
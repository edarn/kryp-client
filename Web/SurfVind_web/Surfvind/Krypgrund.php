<?php
function TNA_QuerySQL($question){
    $rs=mysql_query($question);    
	if (!$rs)    { 
	echo "Error in SQL $question";   
	} 
	return $rs;
}
$db_user = '129649_yc97877';
$db_pass = 'Thomtris1';
$db_name = '129649-surfvind';
$db_host = 'surfvind-129649.mysql.binero.se';
$conn = mysql_connect($db_host,$db_user,$db_pass, "");
if (!$conn){  exit("Connection Failed2: " . $conn);}
mysql_select_db($db_name);
$string = file_get_contents('php://input');
/*$string = "{\"measure\":[{\"TimeStamp\":\"20130312T182210\",\"MoistureUte\":25,\"AbsolutFuktUte\":150,\"TempInne\":3,\"FanOn\":true,\"TempUte\":1,\"AbsolutFuktInne\":324,\"MoistureInne\":54},{\"TimeStamp\":\"20130312T182223\",\"MoistureUte\":25,\"AbsolutFuktUte\":550,\"TempInne\":3,\"FanOn\":false,\"TempUte\":1,\"AbsolutFuktInne\":324,\"MoistureInne\":54},{\"TimeStamp\":\"20130312T182224\",\"MoistureUte\":25,\"AbsolutFuktUte\":150,\"TempInne\":3,\"FanOn\":true,\"TempUte\":1,\"AbsolutFuktInne\":324,\"MoistureInne\":54}]}";
*/
if (!empty($string)){	
$json_a = json_decode($string, true);	
if (!empty($json_a))	{		
$measureArray = $json_a['measure'];	
	$imei  = trim($json_a['id'],"\"");	
	if (!empty($imei) && !empty($measureArray))		{
	foreach ($measureArray as $key => $value) {
			   
			   // echo "Timestamp: " . $value['TimeStamp'] ."<br>";
			    $timestamp = $value['TimeStamp'];
			   // echo "MoistInne: " . $value['MoistureInne']."<br>";
			    $mInne = $value['MoistureInne'];
			   // echo "MoistUte: " . $value['MoistureUte']."<br>";
			    $mUte =  $value['MoistureUte'];
			   // echo $value['AbsolutFuktInne']."<br>";
			    $aFuktInne= $value['AbsolutFuktInne'];
			   // echo $value['AbsolutFuktUte']."<br>";
			    $aFuktUte = $value['AbsolutFuktUte'];
			   // echo $value['TempInne']."<br>";
			    $tempInne = $value['TempInne'];
			   // echo $value['TempUte']."<br>";
			    $tempUte = $value['TempUte'];
			    $fanOn = $value['FanOn'];
			   // echo (int)$fanOn ."<br>";*/
			    echo "Result" . TNA_QuerySQL("INSERT INTO `Krypgrund_data` SET TimeStamp='$timestamp', AbsolutFuktInne='$aFuktInne', AbsolutFuktUte='$aFuktUte', FuktInne='$mInne', FuktUte='$mUte', TempInne='$tempInne', TempUte='$tempUte', FanOn='$fanOn', Imei='$imei'");
			    
			}
		}
	}
}


?>

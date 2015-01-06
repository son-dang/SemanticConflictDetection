<?php

$root = "E:/Project/UndergraduateThesis/SemanticConflictDetection/realData/DesignPatternsPHP";
__VERSION__("B1", "$root/B0");
__VERSION__("B2", "$root/B1");
__VERSION__("B3", "$root/B2");
__VERSION__("B4", "$root/B3");
__VERSION__("B5", "$root/B4");

$a = __CHECK__("$root/B0");
echo "Total case: $a[nCase] </br>";


echo "</br> Executable: " . count($a[executableFiles]) . "</br>";
echo "Executable files: </br>";
foreach($a[executableFiles] as $value)
	echo "$value</br>";


echo "</br>Unexecutable: " . count($a[unexecutableFiles]) . "</br>";
echo "Unexecutable files: </br>";
foreach($a[unexecutableFiles] as $value)
	echo "$value</br>";

__RUN__("$root/result", "$root/report.txt");
?>
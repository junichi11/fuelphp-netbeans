#!/usr/bin/env php
<?php
// for nb74 : https://gist.github.com/nojimage/7655837
array_shift($argv);
foreach ($argv as $idx => $arg) {
	if (preg_match('/NetBeansSuite.php$/', $arg)) {
		$argv[$idx] = __DIR__ . DIRECTORY_SEPARATOR . 'NetBeansSuite.php' ;
	}
}
$command = '%s';
$args = join(' ', $argv);

passthru($command . ' ' . $args);
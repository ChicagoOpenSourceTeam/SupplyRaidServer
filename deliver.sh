#!/bin/sh

if !(git pull -r origin master); then
	echo Failed to pull
	exit 1
fi
if !(./gradlew test); then
	echo Please fix tests before delivering
	exit 1
fi
if !(git push origin master); then
	echo Failed to push git repository
	exit 1
fi
echo Deploy successful!
exit 0;



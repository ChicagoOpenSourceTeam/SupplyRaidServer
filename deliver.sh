#!/bin/sh

if [ -z "git pull origin master != 0" ]; then
	echo Failed to pull
	exit -1
fi

if [ -z "./gradlew test != 0" ]; then
	echo Please fix tests before delivering
	exit -1
fi
if [ -z "git push origin master != 0" ]; then
	echo Failed to push git repository
	exit -1
fi
if [ -z "git push heroku master != 0" ]; then
	echo Failed to deploy to heroku
	exit -1;
fi
echo Deploy successful!
exit 0;



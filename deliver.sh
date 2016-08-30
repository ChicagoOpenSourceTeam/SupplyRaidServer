#!/bin/sh

if [ "$(git pull origin master)" ]; then
	echo Failed to pull
	exit -1
fi

if [ "$(./gradlew test)" ]; then
	echo Please fix tests before delivering
	exit -1
fi
if [ "$(git push origin master)" ]; then
	echo Failed to push git repository
	exit -1
fi
if [ "$(git push heroku master)" ]; then
	echo Failed to deploy to heroku
	exit -1;
fi
echo Deploy successful!
exit 0;



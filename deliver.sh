#!/bin/sh

if !(git pull origin master); then
	echo Failed to pull
	read -p "Press [Enter] key to exit..."
	exit 1
fi
if !(./gradlew test); then
	echo Please fix tests before delivering
	read -p "Press [Enter] key to exit..."
	exit 1
fi
if !(git push origin master); then
	echo Failed to push git repository
	read -p "Press [Enter] key to exit..."
	exit 1
fi
if !(git push heroku master); then
	echo Failed to deploy to heroku
	read -p "Press [Enter] key to exit..."
	exit 1;
fi
echo Deploy successful!
read -p "Press [Enter] key to exit..."
exit 0;



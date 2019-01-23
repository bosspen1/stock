#!/bin/bash

App='stock-service'
ProjectHome='/home/wild/github/stock/src/service/'$App
RunHome='/opt/stock/'
Version='0.0.1'
Time=date "+%Y%m%d%H%M%S"

cd $ProjectHome
git pull
gradle build -x test

kill -9 $(ps -ef|grep java|awk '/$App.jar/{print $2}')

cp -f $RunHome/$App.jar $RunHome/$App_$Time.jar
mv -f $ProjectHome/build/libs/$App-$Version.jar $RunHome/$App.jar

cd $RunHome
nohup java -jar $RunHome/$App.jar > $App.out &

tail -f $RunHome/$App.out

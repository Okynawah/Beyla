#!/bin/bash
currentPwd=$PWD
cd src/query 
mvn clean package spring-boot:repackage
java -jar target/query-1.0-SNAPSHOT.jar &
cd $currentPwd
cd src/client
pnpm start
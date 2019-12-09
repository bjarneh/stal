#!/bin/bash
# 
#
# mvn dependency:copy-dependencies
# mkdir lib
# cp target/dependency/* lib

#CP='target/embedded-jetty-jsp-1-SNAPSHOT.jar'
CP='dst/a.jar'

for f in lib/*;
do
    CP="${CP}:${f}"
done

java -cp "${CP}" com.github.bjarneh.stal.main.Main

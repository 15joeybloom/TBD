#! /usr/bin/bash
fuser -k 1099/tcp

javac -Xlint:unchecked -cp . simpledb/*/*.java simpledb/*/*/*.java
javac -cp . studentClient/simpledb/*.java

java simpledb.server.Startup studentdb &

jar cf simpledb.jar  simpledb/*/*.class simpledb/*/*/*.class
cp simpledb.jar studentClient/simpledb/
cd studentClient/simpledb 
java -cp simpledb.jar:. CreateStudentDB

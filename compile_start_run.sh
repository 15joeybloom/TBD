fuser -k 1099/tcp

javac -Xlint:unchecked -cp . simpledb/*/*.java simpledb/*/*/*.java
javac -cp . studentClient/simpledb/*.java

java simpledb.server.Startup studentdb &


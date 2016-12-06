#! /usr/bin/bash
fuser -k 1099/tcp

rm -rf ~/studentdb

java simpledb.server.Startup studentdb &
./create_largedb.sh


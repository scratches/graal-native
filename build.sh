#!/bin/sh

mkdir -p target

docker rm listener 2>/dev/null || echo No pre-existing container
docker build -t listener .
docker create --name listener listener
docker cp listener:/workspace/app/target/libnettylistener.so target

docker rm listener
docker build -t listener --build-arg LISTENER=rabbit .
docker create --name listener listener
docker cp listener:/workspace/app/target/librabbitlistener.so target

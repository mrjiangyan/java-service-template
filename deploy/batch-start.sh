#!/bin/sh

version=$1
h5Image=registry.cn-hangzhou.aliyuncs.com/touchbiz/cscec-h5:$version
backendImage=registry.cn-hangzhou.aliyuncs.com/touchbiz/cscec-backend:$version

echo "begin to docker pull image"

docker pull $h5Image
docker pull $backendImage

echo "end to docker pull image"
echo "------------------------"

echo "begin to stop container"
docker stop backend-nameplate
docker stop h5-nameplate
docker stop backend-material
docker stop h5-material
echo "end to stop container"
echo "------------------------"

echo "begin to docker rm container"
docker rm backend-nameplate
docker rm h5-nameplate
docker rm backend-material
docker rm h5-material
echo "end to docker rm container"
echo "------------------------"

echo "begin to docker run"

docker run -d --restart=always --name backend-nameplate  --net mynetwork --ip 172.18.0.100 -e TZ="Asia/Shanghai" -v /etc/hosts:/etc/hosts   -v /touchbiz/logs:/opt/logs -p 8080:8080 --entrypoint  java $backendImage -server  -jar /backend-service.jar  --spring.profiles.active=nameplate
docker run -d --restart=always --name h5-nameplate  --net mynetwork --ip 172.18.0.101 -e TZ="Asia/Shanghai" -v /etc/hosts:/etc/hosts   -v /touchbiz/logs:/opt/logs -p 8081:8080 --entrypoint  java $h5Image -server  -jar /h5-service.jar  --spring.profiles.active=nameplate
docker run -d --restart=always --name backend-material  --net mynetwork --ip 172.18.0.102 -e TZ="Asia/Shanghai" -v /etc/hosts:/etc/hosts   -v /touchbiz/logs:/opt/logs -p 8082:8080 --entrypoint  java $backendImage -server  -jar /backend-service.jar  --spring.profiles.active=material
docker run -d --restart=always --name h5-material  --net mynetwork --ip 172.18.0.103 -e TZ="Asia/Shanghai" -v /etc/hosts:/etc/hosts   -v /touchbiz/logs:/opt/logs -p 8083:8080 --entrypoint  java $h5Image -server  -jar /h5-service.jar  --spring.profiles.active=material

echo "end to docker run"
echo "------------------------"

echo "begin to docker start"

docker start backend-nameplate
docker start h5-nameplate
docker start backend-material
docker start h5-material

echo "end to docker run"
echo "------------------------"

docker logs --tail=100 h5-nameplate
docker logs --tail=100 backend-nameplate
docker logs --tail=100 h5-material
docker logs --tail=100 backend-material
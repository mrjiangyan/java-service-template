FROM anapsix/alpine-java:8_server-jre_unlimited

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#RUN mkdir -p /jeecg-boot/config/jeecg/

RUN ls -l /

RUN ls -l ./

EXPOSE 8080

#ADD ./src/main/resources/jeecg ./config/jeecg
# ADD ./target/backend-service.jar ./

# 移动文件到目标文件夹
ARG APP_LOCATION=target/backend-service.jar

ADD ${APP_LOCATION} /



CMD sleep 60;java -Djava.security.egd=file:/dev/./urandom -jar backend-service.jar
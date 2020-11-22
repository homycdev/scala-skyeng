FROM openjdk:11-jre-slim

COPY target/scala-2.12/SCP2020-assembly-0.1.jar apps/server.jar
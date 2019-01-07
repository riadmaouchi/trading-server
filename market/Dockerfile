FROM  openjdk:11-jdk-slim

USER root

EXPOSE 9996

COPY ./target/market-dist-assembly.tar.gz /tmp/

RUN rm -rf /tmp/MARKET && mkdir -p /tmp/MARKET && cd /tmp/MARKET && tar -xvf /tmp/market-dist-assembly.tar.gz

RUN whoami && ls -l /tmp/MARKET/market-startup.sh && chmod +x /tmp/MARKET/market-startup.sh

HEALTHCHECK CMD curl --fail http://localhost:9996 || exit 1

ENTRYPOINT [ "/tmp/MARKET/market-startup.sh" ]
FROM  openjdk:11-jdk-slim

USER root

EXPOSE 8980 8982 8983 8984 80 8080 9998

HEALTHCHECK CMD curl --fail http://localhost:9998 || exit 1

COPY ./target/trade-execution-dist-assembly.tar.gz /tmp/

RUN rm -rf /tmp/TRADE-EXECUTION && mkdir -p /tmp/TRADE-EXECUTION && cd /tmp/TRADE-EXECUTION && tar -xvf /tmp/trade-execution-dist-assembly.tar.gz

RUN whoami && ls -l /tmp/TRADE-EXECUTION/trade-execution-startup.sh && chmod +x /tmp/TRADE-EXECUTION/trade-execution-startup.sh

ENTRYPOINT [ "/tmp/TRADE-EXECUTION/trade-execution-startup.sh" ]
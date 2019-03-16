FROM  openjdk:11-jdk-slim

USER root

EXPOSE 8980 8981 8982 8983 8984 80 9997

VOLUME /var/lib/ldb/data

COPY ./target/matching-engine-dist-assembly.tar.gz /tmp/

RUN rm -rf /tmp/MATCHING-ENGINE && mkdir -p /tmp/MATCHING-ENGINE && cd /tmp/MATCHING-ENGINE && tar -xvf /tmp/matching-engine-dist-assembly.tar.gz

RUN whoami && ls -l /tmp/MATCHING-ENGINE/matching-engine-startup.sh && chmod +x /tmp/MATCHING-ENGINE/matching-engine-startup.sh

ENTRYPOINT [ "/tmp/MATCHING-ENGINE/matching-engine-startup.sh" ]
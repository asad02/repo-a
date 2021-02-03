# kafka - examples

This repo contains all about kafka examples and hands on different kafka concepts.

* Basic kafka
* Kafka producer, consumer
* Kafka connect
    * read data from a source using kafka connect
    * publish data into a kafka topic
    * consume data from a kafka topic
* Kafka stream
    * read data from multiple kafka topics
    * apply application logic
    * publish into another kafka topic
* twitter project
    * read data from twitter
    * publish twitter data into kafka
    * consume from kafka and put into elasticsearch
* creating a kafka cluster in aws
    * create an EC2 instances with security groups that allow port 9092 as TCP
    * ssh to EC2 instance `ssh -i Key.pem ec2-user@ec2-ip`
    * download kafka and extract `wget https://downloads.apache.org/kafka/2.5.0/kafka_2.12-2.5.0.tgz`
    * install java `yum install java-1.8.0-openjdk`
    * extract kafka using `tar -xvf kafka`
    * execute `export KAFKA_HEAP_OPTS="-Xmx256M -Xms128M"`
    * start zookeeper `bin/zookeeper-server-start.sh -daemon config/zookeeper.properties`
    * start kafka `bin/kafka-server-start.sh -daemon config/serve.properties`
    * update advertised port with ec2 public ip in `server.properties`
    * run producer `kafka-console-producer --broker-list ec2-ip:9092 --topic my-topic`
    * run consumer `kafka-console-consumer --bootstrap-server ec2-ip:9092 --topic my-topic`

Publish messages and consume messages from local machine using above two commands.

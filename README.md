# cola
A light rpc framework. 

Grab a cola and drink it, out of the box.
(mainly due to a cats' name :),  variable naming is the hardest part aha)

Simple: easy to learn, easy to develop, easy to integrate and easy to deploy

## Features
- Support multiple protocol such as JDK, Protostuff, hessian, JSON(fastjson), kyro.
- Support features like load-balance(random, Round-Robin, Least-active, Consistent Hash).
- Support service discovery services like ZooKeeper.
- Support oneway, synchronous, asynchronous invoking and callback.
- Easy integrated with provided Spring boot starter

### Requirements
The minimum requirements to run the quick start are:
* JDK 1.8 or above


### Quick Start









docker run --privileged=true -d --name zookeeper --publish 2181:2181  -d zookeeper:latest
docker exec -it 338838f5fb66 /bin/bash
./zkcli.sh
ls


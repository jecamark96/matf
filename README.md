# NeoReborn

NeoReborn is a student project, affiliated with Faculty of Maths in Belgrade. NeoReborn is a gui application with a purpose of visualizing graphs and algorithms performed on those graphs. It is written in scala with a java-fx frontend and built with sbt. 


### Features:

-Visualize directed, undirected, and edge-weighted graphs easily with a simple interface which allows one-click adding of vertices and two-click adding of edges

-Supports working on multiple graphs, up to 13 at a time

-Save graphs to text files and load graphs from text files

-Visualize graph algorithms easily and with interactive options


### Supported algorithms:

-Dfs (directed & undirected graphs) with dfs path simulation

-Bfs (directed & undirected graphs) with bfs path simulation

-Transitive closure (directed & undirected graphs, dfs algorithm) with dfs path simulation

-Dijkstra (directed & undirected weighted graphs) with priority queue and distance matrix simulation


## Prerequisites

To install NeoReborn, you will need to install sbt 1.1.5 with scala 2.12.6. as well as java runtime environment 8 and java-fx libraries. 


If you're using windows, download and install:

1. http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html - Java runtime environment comes pre-bundled with java-fx libraries so you will not need to separately install them. Additionally, you can install java runtime environment 10, but it's redundant for NeoReborn.

2. https://www.scala-sbt.org/download.html


If you're using ubuntu or debian linux distributions, run the following commands:

1. sudo apt update 

2. sudo apt install openjdk-8-jre \ openjfx

2. echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list

3. sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823

4. sudo apt-get update

5. sudo apt-get install sbt


## Installation & Running

sbt is a cross-platform build tool and installation doesn't depend on an OS you are running. Steps to installing NeoReborn are as follows:


1. navigate to the /NeoReborn/ folder and run sbt from command line (if you are on a linux distribution DO NOT run with root privileges - NeoReborn is a gui application and as such can't be run with root privileges so there will be permission conflicts when running the application if it's compiled with root privileges)

2. execute 'compile' from sbt command line to compile the application

3. execute 'package' from sbt command line to package the application in a .jar files

4. execute 'run' to run the application

5. to run the application in the future, repeat step 4


## How to use

![alt text](https://i.imgur.com/cZmAbuD.png)


### Authors

Jelena Marković, Faculty of Maths in Belgrade, e-mail: jelena.markovic.10.11@gmail.com

Miloš Ivanović, Faculty of Maths in Belgrade. e-mail: nsxxchtrs@gmail.com


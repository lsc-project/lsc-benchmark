# lsc-benchmark

Benchmark tool for LSC based on jmh

## Prerequisite
Install lsc engine in your local mvn repository :
`$ mvn -Popendj clean install`

## How to run
Run these tests via the command line, for example :
`$ mvn clean install
$ java -jar target/benchmarks.jar LdapToLdapSyncBenchmark -p entries=500 -p workers=5 -p timelimit=3600`

## Read more
Find more about JMH :
 * http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
 * http://java-performance.info/jmh/

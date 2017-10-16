# Parser

## Presets
1. [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
2. Java 8
3. [Apache Ant](https://ant.apache.org/)

## Install
1. Clone this repo with: `git clone https://github.com/nikkollaii/main.java.ru.old.parser.git`
2. Install: `ant only-jar`

## Run an example: method 1.1
1. `java -jar Parser.jar`
2. `<Parser>: example_start_working.cm -o out.txt -s`

## Run an example: method 1.2
1. `java -jar Parser.jar`
2. `<Parser>: -o out.txt`
3. `<Parser>: example_start_working.cm`
4. `<Parser>: -s`

## Run an example: method 2
1. `java -jar Parser.jar example_start_working.cm -o out.txt`

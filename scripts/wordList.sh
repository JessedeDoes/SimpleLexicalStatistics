export CLASSPATH="../target/SimpleLexicalStatistics-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

JAVA=java
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java

INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index
$JAVA -Xmx20g test.MakeWordList $INDEX WordLemmaTag "-" "$1"


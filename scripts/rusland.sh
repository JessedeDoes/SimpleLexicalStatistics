export CLASSPATH="../target/SimpleLexicalStatistics-0.0.1-SNAPSHOT-jar-with-dependencies.jar"


JAVA=java
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java

#JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
#export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
$JAVA -Xmx20g test.MakeWordList $INDEX WordLemmaTag "-" "(titleLevel1:russisch|rusland|poetin|moskou|kermlin)"


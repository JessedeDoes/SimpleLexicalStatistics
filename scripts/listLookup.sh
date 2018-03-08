export CLASSPATH=".:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

echo $CLASSPATH

JAVA=java
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java

#JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
#export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHNI2014Index
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index
$JAVA -Xmx20g test.ListLookupKeepingIds $INDEX $1 $2 $3


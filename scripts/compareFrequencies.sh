export CLASSPATH=".:./dist/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

JAVA=java
#JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
#export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHNI2014Index
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
$JAVA -Xmx20g test.CompareFrequencies $INDEX word $1


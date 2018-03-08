# filter languageVariant:NN tokens: 220936140 docs: 435297

export CLASSPATH=".:./lib/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

# chni nn of bn: 1194476334
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java
#JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
#export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
#INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
#INDEX=/datalokaal/Corpus/Indexes/CHNDecember2014Index
INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
#INDEX=/datalokaal/Corpus/CHN2014Index
#INDEX=/datalokaal/Corpus/Indexes/KBKranten
$JAVA -Xmx20g test.SubCorpusSize $INDEX  "$1"


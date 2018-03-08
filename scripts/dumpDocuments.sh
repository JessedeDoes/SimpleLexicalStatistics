
JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
export CLASSPATH=".:./lib/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHNI2014Index
#INDEX=/datalokaal/Corpus/Indexes/OpenSonar
#INDEX=/datalokaal/Corpus/Indexes/KBKranten
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
$JAVA -Xmx20g test.DumpDocuments $INDEX "$1"


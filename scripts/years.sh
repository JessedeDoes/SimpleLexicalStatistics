export CLASSPATH=".:./lib/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done


JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java

INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHN++Index
INDEX=/datalokaal/Corpus/Indexes/CHNDecember2014Index
#INDEX=./zeebrieven-index
$JAVA -Xmx20g test.TokensAndDocsPerYear $INDEX 


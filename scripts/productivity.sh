JAVA=/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java
export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHNIndex
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
$JAVA -Xmx20g test.CheckProductivity $INDEX  "$1"


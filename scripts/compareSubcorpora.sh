export CLASSPATH=".:./dist/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

JAVA=java

INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
INDEX=/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndex/
INDEX=/datalokaal/Corpus/Indexes/CHN2014Index
#INDEX=/datalokaal/Corpus/Indexes/TwitterIndex
#INDEX=./zeebrieven-index
$JAVA -Xmx24g test.TestSubcorpusComparison  $INDEX  "$1" "$2"


export CLASSPATH="../target/SimpleLexicalStatistics-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index/
##/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
DATA=lemma,word,pos
java -Xmx20g test.TestFrequencyDevelopment $INDEX $1 $2


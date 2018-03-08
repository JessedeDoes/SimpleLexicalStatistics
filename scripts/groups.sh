export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
INDEX=/datalokaal/Corpus/Indexes/CHNIndex/
##/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
METADATA="zzz"
#witnessYear_from
DATA=lemma,word,pos
corpusQuery="'[lemma='krokodil'\ \&\ pos=\"N.*\"]'"
java -Xmx20g test.GroupCollector $INDEX $METADATA $DATA $1


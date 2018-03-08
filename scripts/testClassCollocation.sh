export CLASSPATH=./bin/:../BlackLab/lib/:../BlackLab/dist/BlackLab.jar 
INDEX=/datalokaal/Corpus/Indexes/TwitterIndex/
#INDEX=/datalokaal/Corpus/Indexes/CHNIndex/
##/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
#witnessYear_from
words=Straattaal/swb.words
words=Straattaal/swb.notSoFrequentInCHN.words
java -Xmx20g stats.colloc.ClassCollocation $INDEX $words witnessYear_from:2014


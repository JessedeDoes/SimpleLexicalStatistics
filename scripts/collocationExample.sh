
export CLASSPATH=".:./lib/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

##INDEX=/datalokaal/Corpus/Indexes/DelftseBijbel/
INDEX=/datalokaal/Corpus/Indexes/CHNDecember2014Index/
##/datalokaal/Scratch/Corpus/GrootModernCorpus/TestIndexFull/index/
METADATA="zzz"
#witnessYear_from
DATA=lemma,word,pos
corpusQuery="'[lemma='krokodil'\ \&\ pos=\"N.*\"]'"
java -Xmx20g stats.colloc.CollocationExample $INDEX 


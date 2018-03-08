export CLASSPATH=".:./dist/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

DIR=~/Werkfolder/Projecten/Spelling/Data/


LOGS=$DIR/logFileLemmata.tab
LEMMATOPBN=$DIR/lemma_top_.BN
LEMMATOPNN=$DIR/lemma_top_.NN
WORDTOPBN=$DIR/lemma_top_.word.BN
WORDTOPNN=$DIR/lemma_top_.word.NN
ADVPRON=$DIR/advPron.tab
TEL=$DIR/telwoorden.tab
VOORZ=$DIR/voorzetsels.tab
VOEGW=$DIR/voegwoorden.tab
VNW=$DIR/voornaamwoorden.tab
SELECTION=$DIR/selectableWords
FWDB=$DIR/match_frequentiewoordenboek_molex.tab

JAVA=java

COLUMNS="lemma/lemma_freq_nn/lemma_freq_bn/wordform_freq_nn/wordform_freq_bn/lemma_rank_nn/lemma_rank_bn/wordform_rank_nn/wordform_rank_bn/inLogfiles/inFrequencyDictionary/functieOfTelWoord"

COLUMNS_MET_ID="id/$COLUMNS"

mkdir $DIR/noIds;
for f in `find $DIR -maxdepth 1 -type f`;
do
  base=`basename $f`;
  echo "$f -- $base";
  perl -pe 's/^[^\t]*\t//' $f | uniq > $DIR/noIds/$base;
done


$JAVA -Xmx20g util.TabSeparatedToExcel voorRikSchutz.xls chni_bn_not_in_molex:$DIR/chnitf_lemma_bn.notfoundinmolex chni_nn_not_in_molex:$DIR/chnitf_lemma_nn.notfoundinmolex

$JAVA -Xmx20g util.TabSeparatedToExcel test.metIds.xls selection:$SELECTION:$COLUMNS_MET_ID logFileWords:$LOGS Frequentiewoordenboek:$FWDB BNLemmaFrequency:$LEMMATOPBN NNLemmaFrequency:$LEMMATOPNN BNWordFrequency:$WORDTOPBN NNWordFrequency:$WORDTOPNN PronominaleBijwoorden:$ADVPRON Telwoorden:$TEL Voorzetsels:$VOORZ Voegwoorden:$VOEGW Voornaamwoorden:$VNW

DIR=$DIR/noIds;

LOGS=$DIR/logFileLemmata.tab
LEMMATOPBN=$DIR/lemma_top_.BN
LEMMATOPNN=$DIR/lemma_top_.NN
WORDTOPBN=$DIR/lemma_top_.word.BN
WORDTOPNN=$DIR/lemma_top_.word.NN
ADVPRON=$DIR/advPron.tab
TEL=$DIR/telwoorden.tab
VOORZ=$DIR/voorzetsels.tab
VOEGW=$DIR/voegwoorden.tab
VNW=$DIR/voornaamwoorden.tab
SELECTION=$DIR/selectableWords
FWDB=$DIR/match_frequentiewoordenboek_molex.tab

$JAVA -Xmx20g util.TabSeparatedToExcel test.noIds.xls selection:$SELECTION:$COLUMNS logFileWords:$LOGS Frequentiewoordenboek:$FWDB BNLemmaFrequency:$LEMMATOPBN NNLemmaFrequency:$LEMMATOPNN BNWordFrequency:$WORDTOPBN NNWordFrequency:$WORDTOPNN PronominaleBijwoorden:$ADVPRON Telwoorden:$TEL Voorzetsels:$VOORZ Voegwoorden:$VOEGW Voornaamwoorden:$VNW

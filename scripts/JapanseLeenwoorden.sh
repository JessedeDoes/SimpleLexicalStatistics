#LIST=~/Werkfolder/Projecten/Spelling/Data/allLemmata.tab
DIR=/mnt/Projecten/Taalbank/Werkfolder_Redactie/Jesse/Projecten/Spelling/Japans
LIST=$DIR/kale_es.txt
#LIST=sample.list
bash scripts/listLookup.sh word $LIST  '' > $DIR/word_just_es.out

DIR=~/Werkfolder/Projecten/Spelling/UpdateDB
LIST=$DIR/nulletjes.txt.iso
bash listLookup.sh lemma $LIST  '' > $DIR/nulletjes.withFrequency.lemma.tab
bash listLookup.sh word  $LIST '' > $DIR/nulletjes.withFrequency.word.tab

DIR=/home/INL/does/mount/Projecten/Taalbank/Werkfolder_Redactie/Jesse/Projecten/Spelling/TaalTik
LIST=$DIR/basisWoorden
bash scripts/listLookup.sh lemma $LIST '' > $DIR/lemma.freq
bash scripts/listLookup.sh word  $LIST '' > $DIR/word.freq

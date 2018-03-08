DIR=/home/INL/does/mount/Projecten/Taalbank/Werkfolder_Redactie/Jesse/Projecten/Spelling/Hulky/2017
LIST=$DIR/hulk.withIds.lemmalist
bash scripts/listLookup.sh lemma $LIST '' > $DIR/lemma.freq
bash scripts/listLookup.sh word  $LIST '' > $DIR/word.freq

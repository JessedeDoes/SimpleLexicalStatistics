LIST=~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.tab
LIST=Examples/zeurwoorden.tab
bash listLookup.sh lemma $LIST  '' > ~/Werkfolder/Projecten/Spelling/Data/zeurwoorden.withFrequency.tab
bash listLookup.sh word  $LIST '' > ~/Werkfolder/Projecten/Spelling/Data/zeurwoorden.withFrequency.word.tab

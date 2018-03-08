LIST=~/Werkfolder/Projecten/Spelling/Data/allLemmata.tab
#LIST=sample.list
bash listLookup.sh lemma $LIST  'languageVariant:BN' > ~/Werkfolder/Projecten/Spelling/Data/allLemmata.withFrequency.tab.BN
bash listLookup.sh lemma $LIST  'languageVariant:NN' > ~/Werkfolder/Projecten/Spelling/Data/allLemmata.withFrequency.tab.NN
bash listLookup.sh word  $LIST 'languageVariant:BN' > ~/Werkfolder/Projecten/Spelling/Data/allLemmata.withFrequency.tab.word.BN
bash listLookup.sh word $LIST  'languageVariant:NN' > ~/Werkfolder/Projecten/Spelling/Data/allLemmata.withFrequency.tab.word.NN

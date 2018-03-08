LIST=~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.tab
LIST=sample.list
LIST=Examples/spatiewoorden.csv
bash listLookup.sh lemma $LIST  'languageVariant:BN' > ~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.withFrequency.tab.BN
bash listLookup.sh lemma $LIST  'languageVariant:NN' > ~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.withFrequency.tab.NN
bash listLookup.sh word  $LIST 'languageVariant:BN' > ~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.withFrequency.tab.word.BN
bash listLookup.sh word $LIST  'languageVariant:NN' > ~/Werkfolder/Projecten/Spelling/Data/spatiewoorden.withFrequency.tab.word.NN

bash compareLists.sh "witnessYear_from:[2011 TO 2013]" "witnessYear_from:[0000 TO 2010]" > /tmp/compare.out
grep -v '[A-Z]' /tmp/compare.out | pcregrep '\t0=' | grep 'lemma=[a-z]* ' > /tmp/obviousCandidates.txt

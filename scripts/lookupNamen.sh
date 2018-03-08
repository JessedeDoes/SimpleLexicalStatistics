LIST=./aap.txt
#LIST=Examples/sample.list

export CLASSPATH=".:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

echo $CLASSPATH

JAVA=java
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java

INDEX=/datalokaal/Corpus/Indexes/CHNI2016Index
$JAVA -Xmx20g test.ListLookupKeepingIds $INDEX word $LIST  'witnessYear_from:[2000 TO 2016]'

export CLASSPATH=".:./dist/BlackLab.jar:./bin"
for x in `ls lib/*.jar`;
do
  export CLASSPATH="$CLASSPATH:./$x";
done

java -Xmx20g fuzzy.VindBijnaHomoos $1

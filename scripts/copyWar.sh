cp dist/lib/NER.war /var/lib/tomcat5/webapps/
sleep 10
wget -O - 'http://svowim01:8090/NER/NERService?tagger=impact-ner&inputText=Nee! Klaas+is+gek'

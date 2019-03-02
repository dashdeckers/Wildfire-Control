export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=128m"
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="cosyne_gui"
#cd target/classes
#java Main cosyne_gui

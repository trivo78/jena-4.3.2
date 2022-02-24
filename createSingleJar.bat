mvn clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true
mvn clean compile assembly:single -Dmaven.javadoc.skip=true -Dmaven.test.skip=true
#single jar path--> apache-jena\target\apache-jena-4.3.2-jar-with-dependencies.jar
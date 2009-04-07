rm stacktrace.log &
cd ..
./update_jar
rm *.zip &
grails clean
grails package-plugin
cd Demo
rm -rf ./migrations/* ./plugins/grails-autobase* ./plugins/autobase* &
grails uninstall-plugin autobase
grails install-plugin ../grails-auto*.zip
grails clean
cp -vf TestingMigration.groovy ./migrations/TestingMigration.groovy &
cp -vf changelog.groovy ./migrations/changelog.groovy &
grails convert-xml-changelog common.tests.changelog.xml ./migrations/common.tests.changelog.groovy
grails run-war

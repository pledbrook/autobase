import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.diff.Diff

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << new File("${autobasePluginDir}/scripts/_MigrationBase.groovy")

target(default: "Generates a baseline migration for the current database schema.") {
  depends(parseArguments)

  // Get the data source from the Grails application.
//  def dataSource = grailsApp.mainContext.getBean("dataSource")
//  println ">>> DataSource: ${dataSource} (${dataSource.class})"
  def dataSourceConfig = configSlurper.parse(classLoader.loadClass("DataSource")).dataSource
  def driverClassName = dataSourceConfig.driverClassName
  def url = dataSourceConfig.url

  // Load the JDBC driver for the data source configuration.
  def driver = loadDbDriver(driverClassName, url)
  if (!driver) return 1

  // Create the DB connection. For the password, we want to set an
  // empty string if that's the given value, so rather than do a Groovy
  // Truth test on it, we check it's a string.
  def info = new Properties()
  info.setProperty("user", dataSourceConfig.username)
  if (dataSourceConfig.password instanceof CharSequence) {
    info.setProperty("password", dataSourceConfig.password)
  }

  def connection
  try {
    event("StatusUpdate", ["Connecting to database with URL: ${url}"])
    connection = driver.connect(url, info)
    if (!connection) {
      event("StatusError", ["Connection could not be created to ${url} with driver ${driverClassName}. Possibly the wrong driver for the given database URL"])
      return 1
    }
  }
  catch (Exception e) {
    event("StatusError", ["Could not initialise Liquibase: " + e.getMessage()])
    return 1
  }

  // Generate the changelog.
  def database = DatabaseFactory.instance.findCorrectDatabaseImplementation(connection)
  try {
    final outStream = new PipedOutputStream()
    final inStream = new PipedInputStream(outStream)

    // Start a thread to generate the XML changelog.
    Thread.start("XML Writing") {
      def diffResult = new Diff(database, null).compare()
      
      new PrintStream(outStream).withStream { out ->
        diffResult.printChangeLog(out, database, classLoader.loadClass("liquibase.xml.DefaultXmlWriter").newInstance())
      }
    }

    // Read from the piped input stream and create the Groovy migration
    // scripts from the piped XML.
    xmlToGroovyMigration(inStream, "baseline-${grailsEnv}.groovy")

    event("StatusFinal", ["Baseline migration successfully created."])
  }
  catch (Exception e) {
    logError("Failed to migrate database ${grailsEnv}", e)
    return 1
  }
  finally {
    database.connection.close()
  }
}

loadDbDriver = { driverClassName, url ->
  try {
    driverClassName = driverClassName ?: DatabaseFactory.instance.findDefaultDriver(url)

    if (!driverClassName) {
      event("StatusError", ["Driver class was not specified and could not be determined from the url"])
      return null
    }

    return Class.forName(driverClassName, true, classLoader).newInstance()
  }
  catch (Exception e) {
    event("StatusError", ["Cannot get database driver: " + e.getMessage()])
    return null
  }
}

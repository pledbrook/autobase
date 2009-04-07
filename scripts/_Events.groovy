import grails.util.GrailsUtil

private doWar = { name, stagingDir ->
    try {
      Ant.zip(
        destfile: new File("${stagingDir}/WEB-INF/classes", 'autobase.zip').absolutePath,
        basedir: '.',
        includes: 'migrations/**,**/*.csv,**/*.properties',
        whenempty: 'fail',
        duplicate: 'fail',
        comment: 'The Autobase migrations to execute'
      )
  } catch(Exception e) {
    Ant.fail(message: "Could not store WAR: " + e.class.simpleName + ": " + e.message)
  }
}

eventCreateWarStart = { name, stagingDir ->
    doWar(name, stagingDir)
}

eventWarStart = { name ->
    if(GrailsUtil.grailsVersion.startsWith("1.0")) {
        if(!(new File(stagingDir).exists())) {
            Ant.fail("Could not find staging directory")
          }
        doWar(name, stagingDir)
    }
}

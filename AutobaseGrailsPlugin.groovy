/*
  Copyright 2008 Robert Fischer

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
import autobase.Autobase
import autobase.change.GroovyScriptChange
import grails.util.GrailsUtil
import java.util.logging.Handler
import liquibase.parser.factory.OpenChangeFactory
import org.apache.log4j.*
import org.slf4j.bridge.*


class AutobaseGrailsPlugin {

    private static final Logger log = Logger.getLogger(AutobaseGrailsPlugin);

    def version = '0.8.3'
    def dependsOn = [hibernate:GrailsUtil.grailsVersion]
		def observe = []
		def watchedResources = []

    // TODO Fill in these fields
    def author = "Robert Fischer"
    def authorEmail = "robert.fischer@smokejumperit.com"
    def title = "Automate your database work as much as possible"
    def description = '''\
This plugin marries the established Liquibase core with Grails development processes in order to to minimze the amount of database code you have to think about.

The approach to this plugin is to leave the database update mode ("hbm2ddl.auto" value) as "update", and to manage alterations to the database schema through checking in changesets to a folder.  The changesets are made up of Liquibase's many "refactorings": http://www.liquibase.org/manual/home#available_database_refactorings
'''

    // URL to the plugin's documentation
    def documentation = "http://github.com/RobertFischer/autobase/wikis"
		//"http://grails.org/Autobase+Plugin"

    private static final Closure doInstallSlf4jBridge = {
      try {
        SLF4JBridgeHandler.install()
        //here we do a little hack. The SLF4JBridgeHandler wants to be the only handler registered with the logger
        //but for some reason we get an extra console handler in the array, so we remove it
        def logParent = liquibase.log.LogFactory.getLogger().parent
        Handler slf4jHandler = (Handler) logParent.handlers.find { it instanceof SLF4JBridgeHandler }
        if (logParent.handlers.length > 1 && slf4jHandler) {
          def handlersToRemove = logParent.getHandlers() - slf4jHandler
          handlersToRemove.each { Handler handler ->
            logParent.removeHandler(handler)
          }
        }
      } catch (Throwable e) {
        log.error("Error setting up slf4j bridge, message: ${e.getMessage()}", e)  
      }
    }

    //TODO: Formalize how we want to register change/precondition extensions conventionally
    private static final Closure doRegisterExtensions = {
      try {
        OpenChangeFactory.instance.registerChange(GroovyScriptChange.TAG_NAME, GroovyScriptChange.class)
      } catch (Throwable e) {
        GrailsUtil.deepSanitize(e)
        log.error("Error registering extensions, message: ${e.getMessage()}", e)
      }
    }

		private static final Closure doMigrate = {application, appCtx ->
			try {
          def runOnCreateDrop = application.config.autobase.runOnCreateDrop
			    if (runOnCreateDrop == false && application.config.dataSource.dbCreate == 'create-drop') {
            log.info("Skipping Autobase migration due to create-drop (set 'autobase.runOnCreateDrop' to 'false' in Config.groovy to run anyway)")
          } else {
            log.info("---- Starting Autobase migrations  ----")
            Autobase.migrate(appCtx)
            log.info("---- Autobase migrations completed ----")
          } 
			} catch(Exception e) {
				GrailsUtil.deepSanitize(e)
				log.error("Error during Autobase migration", e)
			}
		}

    def doWithSpring = { }

    def doWithApplicationContext = { appCtx ->
      doInstallSlf4jBridge()
      doRegisterExtensions()
      doMigrate(application, appCtx)
    }

    def doWithWebDescriptor = {}

    // Do at the very last moment of app start-up
    def doWithDynamicMethods = {}

    // Implements code that is executed when any artefact that this plugin is
    // watching is modified and reloaded. The event contains: event.source,
    // event.application, event.manager, event.ctx, and event.plugin.
    def onChange = {}

		// Implements code that is executed when the project configuration changes.
   	// The event is the same as for 'onChange'.
    def onConfigChange = {}
}

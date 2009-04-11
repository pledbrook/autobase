package liquibase
//
//    This file is part of Liquibase-DSL.
//
//    Liquibase-DSL is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquibase-DSL is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public License
//    along with Liquibase-DSL.  If not, see <http://www.gnu.org/licenses/>.
//

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.lock.LockHandler;
import liquibase.parser.ChangeLogIterator;
import liquibase.parser.filter.*;
import liquibase.parser.visitor.*;

import liquibase.parser.factory.ChangeLogParserFactory;
import liquibase.parser.xml.XMLChangeLogParser;
import liquibase.dsl.properties.*;
import liquibase.parser.groovy.*;
import org.apache.commons.lang.StringUtils
import liquibase.dsl.parser.groovy.GroovyChangeLogParser
import java.util.logging.Logger
import java.util.logging.Level

/**
*  Executes the Liquibase command with appropriate mangling to handle the DSLs.
*/
class LiquibaseDsl extends Liquibase {
  private static final Logger log = liquibase.log.LogFactory.logger
	private static final String PARSER_SUFFIX_PREFIX = "lbdsl.parser.suffix"
  private final String changeLogPath;

	public LiquibaseDsl(String changeLogFile, FileOpener opener, Database db) {
		super(changeLogFile, opener, db)
    changeLogPath = changeLogFile
  
		// Now prep the factory
		//ChangeLogParserFactory.register("xml", XMLChangeLogParser.class); -- XMLChangeLogParser is-not-a ChangeLogParserImpl
		ChangeLogParserFactory.register("groovy", GroovyChangeLogParser.class);
		LbdslProperties.instance.pluginParsers.each { 
			ChangeLogParserFactory.register((String)it.key, (String)it.value)
		}
	}

  public void update(String contexts) throws LiquibaseException {

      LockHandler lockHandler = LockHandler.getInstance(database);
      lockHandler.waitForLock();

      try {
          database.checkDatabaseChangeLogTable();

          def parser = ChangeLogParserFactory.getParser(StringUtils.substringAfterLast(changeLogPath, "."))
          if(parser == null) { // Couldn't find a match: fall back to default!
            System.err.println "===FALLING BACK TO DEFAULT==="
            parser = new XMLChangeLogParser()
          }

          DatabaseChangeLog changeLog = parser.parse(changeLogPath, fileOpener, [:], database);
          changeLog.validate(database);
          ChangeLogIterator logIterator = new ChangeLogIterator(changeLog, 
            [
              new ShouldRunChangeSetFilter(database),
              new ContextChangeSetFilter(contexts),
              new DbmsChangeSetFilter(database)
            ] as liquibase.parser.filter.ChangeSetFilter[]);
          logIterator.run(new UpdateVisitor(database), database);
      } catch (ValidationFailedException e) {
        //TODO: Figure out what we do with the log level here, we get here from a failed precondition on the databaseChangeLog level (and perhaps from elsewhere) 
        log.log(Level.INFO, e.getMessage())

      } catch(PreconditionFailedException e) {
        log.log(Level.SEVERE, "Terminating run due to failed Precondition")
        
      } catch (LiquibaseException e) {
          throw e;
      } finally {
          try {
              lockHandler.releaseLock();
          } catch (LockException e) {
              log.log(Level.SEVERE, "Could not release lock", e);
          }
      }
  }


}

package liquibase.dsl.parser.groovy
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
import liquibase.*
import liquibase.parser.*
import liquibase.exception.*
import liquibase.database.Database

/**
*		Provides access to the properties set in the directory denoted by the "lbdsl.home" property.
*/
class GroovyChangeLogParser implements ChangeLogParserImpl {

	public DatabaseChangeLog parse(String physicalChangeLogLocation, FileOpener fileOpener, Map changeLogProperties, Database db) {
		if(!fileOpener) {
      throw new IllegalArgumentException("Need to specify a fileOpener")
		}
		def txt = fileOpener.getResourceAsStream(physicalChangeLogLocation).text
		def out = new GroovyDatabaseChangeLog(physicalChangeLogLocation, db);
		out.fileOpener = fileOpener;
		try {
			Eval.me("databaseChangeLog", { Map props=[:], Closure closure -> 
        props.entrySet().each { out[it.key] = it.value }
        closure.delegate = out
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure() 
      }, txt)
		} catch(org.codehaus.groovy.control.CompilationFailedException fail) {
			throw new ChangeLogParseException("Groovy Change Log at ${physicalChangeLogLocation} is not proper Groovy", fail);
		} catch(Exception e) {
			throw new ChangeLogParseException("Unknown error while executing file at ${physicalChangeLogLocation}", e);
		}
		return (DatabaseChangeLog)out;
	}

}

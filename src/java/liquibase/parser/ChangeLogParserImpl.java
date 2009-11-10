package liquibase.parser;
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

import java.util.Map;

/**
* Interface that must be implemented for a Liquibase ChangeLogParser implementation.  A ChangeLogParser must also have a no-argument constructor.
*/
public interface ChangeLogParserImpl {

	/**
	*	 Method responsible for parsing a particular file.
	*/
	public DatabaseChangeLog parse(String physicalChangeLogLocation, FileOpener fileOpener, Map changeLogProperties, Database db);

}

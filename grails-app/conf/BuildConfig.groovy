//We do this since grails by default stores the settings for the project under the users home directory 
//in a sub/sub/../ directory named by the project name which in this case was determined to be 'trunk'
//in other words, all projects checked in to subversion with the standard trunk/branches/tags convention
//will be sharing the same 'project', namely 'trunk, which in turn leads to nastiness. We set the
//plugins dir to a local directory to isolate ourselves here
grails.project.plugins.dir="./plugins"
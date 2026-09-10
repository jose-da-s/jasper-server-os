/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.nio.file.*
import com.jaspersoft.buildomatic.masterlist.Scanner

// generateMasterList.groovy
// Use Scanner to get a list of JRS JARs from a build tree, then use it again to scan the WAR and figure out what other third-party JARs were added.
// Save that to a file.
// Arguments:
// - WAR file to check
// - output file for master list
// - one or more build directories to search for built JARs 

// save the list of JARs that were built in JRS if you want to debug the "include" test 
def saveOurJars = false
// Assuming here that we are running from ant, and we can call ant.fail to fail gracefully.
// If we are getting run from groovysh we could do something else
def fail(msg) {
	ant.fail msg
}

def echo(msg) {
	ant.echo msg
}

if (args.length < 3) {
	fail("usage: generateMasterList.groovy <WAR file> <master list output file> <build dir> ...")
}
def warPath = Paths.get(args[0])
if (! Files.isReadable(warPath)) {
	fail("couldn't read file $warPath")
}

def version = args[1]

String includeDistrPaths = args[2]
String excludeDistrPaths = args[3]
String outputDir = args[4]

def scanFoldersList = []
boolean isDistrFile = false
if (includeDistrPaths != "") {
	isDistrFile = true
}
if (isDistrFile) {
	def scanFoldersPath = Paths.get(includeDistrPaths)
	if (!Files.isReadable(scanFoldersPath)) {
		fail("couldn't read exclusion filter file ${scanFoldersPath}")
	}
	scanFoldersList = scanFoldersPath.toFile() as List<String>
} else {
	scanFoldersList.add(version == "ce" ? "jasperserver.war" : "jasperserver-pro.war")
}

def localSourceRootPaths = args[5].split("[, ]+")
		.collect { Paths.get(it) }
		.findAll { it } // filter out all empty or null values

localSourceRootPaths.each { if (! Files.isDirectory(it)) fail("$it isn't a directory") }

// let's get the JARs that we built by iterating over build paths with the scanner
def localJarDependencies = new HashSet()

localSourceRootPaths.each { buildPath ->
	localJarDependencies += new Scanner(
		top: buildPath, 
		recurse:  {false}, 
		include: {ext == "jar" && dir.endsWith("target") &&
			// these target dirs have third party jars
		 	! dir.contains("code-coverage") && ! dir.contains("buildomatic") &&
		 	// this target dir has both a built JAR and third-party jars 
		 	! ( dir.contains("jasperserver-ftp") && ! name.startsWith("jasperserver-ftp")) &&
				! (dir.contains("scripts")) && ! (dir.contains("optimized-scripts"))} )
		.scan()
		.collect { it.name }
}

scanFoldersList.each { String folderName ->

	def outputPath
	if (folderName.endsWith(".war")) {
		outputPath = Paths.get("${outputDir}/current-war-deps-${version}.txt")
	} else {
		outputPath = Paths.get("${outputDir}/current-${folderName}-deps-${version}.txt")
	}

	def excludeFolderPaths = Paths.get(excludeDistrPaths)
	if (!Files.isReadable(excludeFolderPaths)) {
		fail("couldn't read exclusion filter file ${excludeFolderPaths}")
	}
	def excludedList = excludeFolderPaths.toFile() as List<String>
	PathMatcher excludedFolderMatcher = FileSystems.getDefault().getPathMatcher("glob:$folderName/**")
	def excludedFolderSpecifiedList = excludedList.findAll { excludedFolderMatcher.matches(Paths.get(it))}

// create dir for output file if not present
	def depsDir = outputPath.parent
	Files.createDirectories(depsDir)

// save ourJars in a file so we can sanity check them
// the "include" above needed to be tweaked because some target dirs contain third-party JARs, so they are incorrectly excluded
	saveOurJars && depsDir.resolve(".exclude_${outputPath.fileName}").withWriter { w ->
		localJarDependencies.each { w.writeLine(it) }
	}

// now scan the WAR and get all the JARs not in ourJars
	PathMatcher scanDistrFolderMatcher = FileSystems.getDefault().getPathMatcher("glob:*/$folderName**")
	def notOurJars = new Scanner(
			top: warPath,
			recurse: { true },
			scanDistrFolder: { value ->
				isDistrFile ? scanDistrFolderMatcher.matches(Paths.get(value)) : true
			},
			entriesScanTypes: {
				folderName.endsWith(".war") && ext == "war" && name == folderName
			},
			include: {
				def excludedFolder = null
				if (dir != null) {
					excludedFolder = excludedFolderSpecifiedList.find {
						dir ==~ /([^\/]+\/$it.*)|(\\/$it.*)/
					}
				}
				ext == "jar" && !localJarDependencies.contains(name) && !excludedFolder
			})
			.scan()
			.sort { it.name }

// write names and hashes to a file
	outputPath.withWriter { w ->
		notOurJars.each {
			w.writeLine("${it.libName} ${it.libVersion} ${it.sha1}")
		}
	}
}

/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.ide.xcode

import org.gradle.ide.xcode.fixtures.AbstractXcodeIntegrationSpec
import org.gradle.nativeplatform.fixtures.app.CppApp
import org.gradle.nativeplatform.fixtures.app.CppLib

class XcodeSingleCppProjectIntegrationTest extends AbstractXcodeIntegrationSpec {
    def "create xcode project C++ executable"() {
        given:
        buildFile << """
apply plugin: 'cpp-executable'
"""

        def app = new CppApp()
        app.writeToProject(testDirectory)
        file('src/main/headers/ignore.cpp') << 'broken!'
        file('src/main/cpp/ignore.h') << 'broken!'
        file('src/main/cpp/ignore.swift') << 'broken!'

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeProjectWorkspaceSettings", ":xcodeSchemeAppExecutable", ":xcodeWorkspace", ":xcodeWorkspaceWorkspaceSettings", ":xcode")

        def project = xcodeProject("${rootProjectName}.xcodeproj").projectFile
        project.mainGroup.assertHasChildren(['Products', 'build.gradle'] + app.files*.name)
        project.targets.size() == 2
        project.assertTargetsAreTools()
        project.targets.every { it.productName == 'App' }
        project.targets[0].name == 'App Executable'
        project.targets[0].productReference.path == exe("build/exe/main/debug/app").absolutePath
        project.targets[1].name == '[INDEXING ONLY] App Executable'
        project.products.children.size() == 1
        project.products.children[0].path == exe("build/exe/main/debug/app").absolutePath

        assertProjectHasEqualsNumberOfGradleAndIndexTargets(project.targets)
    }

    def "create xcode project C++ library"() {
        given:
        buildFile << """
apply plugin: 'cpp-library'
"""

        def lib = new CppLib()
        lib.writeToProject(testDirectory)
        file('src/main/public/ignore.cpp') << 'broken!'
        file('src/main/headers/ignore.cpp') << 'broken!'
        file('src/main/cpp/ignore.h') << 'broken!'
        file('src/main/cpp/ignore.swift') << 'broken!'

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeSchemeAppSharedLibrary", ":xcodeProjectWorkspaceSettings", ":xcode")

        def project = xcodeProject("${rootProjectName}.xcodeproj").projectFile
        project.mainGroup.assertHasChildren(['Products', 'build.gradle'] + lib.files*.name)
        project.targets.size() == 2
        project.assertTargetsAreDynamicLibraries()
        project.targets.every { it.productName == "App" }
        project.targets[0].name == 'App SharedLibrary'
        project.targets[0].productReference.path == sharedLib("build/lib/main/debug/app").absolutePath
        project.targets[1].name == '[INDEXING ONLY] App SharedLibrary'
        project.products.children.size() == 1
        project.products.children[0].path == sharedLib("build/lib/main/debug/app").absolutePath

        assertProjectHasEqualsNumberOfGradleAndIndexTargets(project.targets)
    }

    def "new source files are included in the project"() {
        given:
        buildFile << """
apply plugin: 'cpp-executable'
"""

        when:
        def app = new CppApp()
        app.writeToProject(testDirectory)
        succeeds("xcode")

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle'] + app.files*.name)

        when:
        file("src/main/cpp/new.cpp") << "include <iostream>\n"
        succeeds('xcode')

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle', 'new.cpp'] + app.files*.name)
    }

    def "deleted source files are not included in the project"() {
        given:
        buildFile << """
apply plugin: 'cpp-executable'
"""

        when:
        def app = new CppApp()
        app.writeToProject(testDirectory)
        file("src/main/cpp/old.cpp") << "include <iostream>\n"
        succeeds("xcode")

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle', 'old.cpp'] + app.files*.name)

        when:
        file('src/main/cpp/old.cpp').delete()
        succeeds('xcode')

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle'] + app.files*.name)
    }

    def "executable source files in a non-default location are included in the project"() {
        given:
        buildFile << """
apply plugin: 'cpp-executable'

executable {
    source.from 'Sources'
    privateHeaders.from 'Sources/include'
}
"""

        when:
        def app = new CppApp()
        app.headers.writeToSourceDir(file('Sources/include'))
        app.sources.writeToSourceDir(file('Sources'))
        file("src/main/headers/ignore.h") << 'broken!'
        file('src/main/cpp/ignore.cpp') << 'broken!'
        succeeds("xcode")

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle'] + app.files*.name)
    }

    def "library source files in a non-default location are included in the project"() {
        given:
        buildFile << """
apply plugin: 'cpp-library'

library {
    source.from 'Sources'
    privateHeaders.from 'Sources/include'
    publicHeaders.from 'Includes'
}
"""

        when:
        def lib = new CppLib()
        lib.publicHeaders.writeToSourceDir(file('Includes'))
        lib.privateHeaders.writeToSourceDir(file('Sources/include'))
        lib.sources.writeToSourceDir(file('Sources'))
        file('src/main/headers/ignore.h') << 'broken!'
        file('src/main/public/ignore.h') << 'broken!'
        file('src/main/cpp/ignore.cpp') << 'broken!'
        succeeds("xcode")

        then:
        xcodeProject("${rootProjectName}.xcodeproj").projectFile
            .mainGroup.assertHasChildren(['Products', 'build.gradle'] + lib.files*.name)
    }

    def "honors changes to executable output locations"() {
        given:
        buildFile << """
apply plugin: 'cpp-executable'
buildDir = 'output'
executable.baseName = 'test_app'
"""

        def app = new CppApp()
        app.writeToProject(testDirectory)

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeProjectWorkspaceSettings", ":xcodeSchemeAppExecutable", ":xcodeWorkspace", ":xcodeWorkspaceWorkspaceSettings", ":xcode")

        def project = xcodeProject("${rootProjectName}.xcodeproj").projectFile
        project.targets.size() == 2
        project.targets.every { it.productName == 'App' }
        project.targets[0].name == 'App Executable'
        project.targets[0].productReference.path == exe("output/exe/main/debug/test_app").absolutePath
        project.targets[1].name == '[INDEXING ONLY] App Executable'
        project.products.children.size() == 1
        project.products.children[0].path == exe("output/exe/main/debug/test_app").absolutePath
    }

    def "honors changes to library output locations"() {
        given:
        buildFile << """
apply plugin: 'cpp-library'
buildDir = 'output'
library.baseName = 'test_lib'
"""

        def lib = new CppLib()
        lib.writeToProject(testDirectory)

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeSchemeAppSharedLibrary", ":xcodeProjectWorkspaceSettings", ":xcode")

        def project = xcodeProject("${rootProjectName}.xcodeproj").projectFile
        project.targets.size() == 2
        project.targets.every { it.productName == "App" }
        project.targets[0].name == 'App SharedLibrary'
        project.targets[0].productReference.path == sharedLib("output/lib/main/debug/test_lib").absolutePath
        project.targets[1].name == '[INDEXING ONLY] App SharedLibrary'
        project.products.children.size() == 1
        project.products.children[0].path == sharedLib("output/lib/main/debug/test_lib").absolutePath
    }
}

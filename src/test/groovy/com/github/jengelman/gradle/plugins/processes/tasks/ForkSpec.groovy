package com.github.jengelman.gradle.plugins.processes.tasks

import com.github.jengelman.gradle.plugins.processes.ProcessesPlugin
import com.github.jengelman.gradle.plugins.processes.util.TestFile
import com.github.jengelman.gradle.plugins.processes.util.TestNameTestDirectoryProvider
import org.gradle.testkit.functional.ExecutionResult
import org.gradle.testkit.functional.GradleRunner
import org.gradle.testkit.functional.GradleRunnerFactory
import org.junit.Rule
import spock.lang.Specification

class ForkSpec extends Specification {

    @Rule
    final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider()
    GradleRunner runner = GradleRunnerFactory.create()

    def setup() {
        buildFile << """
        apply plugin: ${ProcessesPlugin.name}
        """
        runner.directory = tmpDir.testDirectory
    }

    @SuppressWarnings('Println')
    def forkTask() {
        given:
        File testFile = tmpDir.testDirectory.file('someFile')

        buildFile << """
        task forkMain(type: ${Fork.name}) {
            executable = 'touch'
            workingDir = "${tmpDir.testDirectory}"
            args "${testFile.path}"
        }

        task waitForFinish() {
            doLast {
                forkMain.processHandle.waitForFinish().assertNormalExitValue()
                println 'Process completed'
            }
        }

        forkMain.finalizedBy waitForFinish
        """

        when:
        runner.arguments << 'forkMain'
        ExecutionResult result = runner.run()

        then:
        assert result.standardOutput.contains('Process completed')
    }

    private TestFile getBuildFile() {
        tmpDir.testDirectory.file('build.gradle')
    }
}

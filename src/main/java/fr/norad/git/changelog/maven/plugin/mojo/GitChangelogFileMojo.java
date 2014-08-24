/**
 *
 *     Copyright (C) norad.fr
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package fr.norad.git.changelog.maven.plugin.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Generate changelog file
 */
@Mojo(name = "gitchangelog")
public class GitChangelogFileMojo extends GitChangelogAbstractMojo {

    @Override
    public void run() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Plugin skip flag is true. Skipping execution");
            return;
        }
        if (getProject().getPackaging().equalsIgnoreCase("pom") && isSkipPomProjects()) {
            getLog().info("Project is pom. skipping execution");
            return;
        }

        File targetFile = getTargetFile();
        try {
            targetFile.createNewFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create changelog file : " + targetFile, e);
        }
        try (PrintStream fileWriter = new PrintStream(targetFile)) {
            writeChangeLog(fileWriter);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Cannot write to changelog file : " + targetFile, e);
        }
    }
}

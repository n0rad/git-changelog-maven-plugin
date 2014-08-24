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
package fr.norad.git.changelog.maven.plugin;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Constants;
import com.google.common.base.Optional;

/**
 * Encapsulates logic to locate a valid .git directory.
 *
 * @author <a href="mailto:konrad.malawski@java.pl">Konrad 'ktoso' Malawski</a>
 */
public class GitDirLocator {
    final MavenProject mavenProject;
    final List<MavenProject> reactorProjects;

    public GitDirLocator(MavenProject mavenProject, List<MavenProject> reactorProjects) {
        this.mavenProject = mavenProject;
        this.reactorProjects = reactorProjects;
    }

    public File lookupGitDirectory(File manuallyConfiguredDir) {

//        if (manuallyConfiguredDir.exists()) {
//
//            // If manuallyConfiguredDir is a directory then we can use it as the git path.
//            if (manuallyConfiguredDir.isDirectory()) {
//                return manuallyConfiguredDir;
//            }
//
//            // If the path exists but is not a directory it might be a git submodule "gitdir" link.
//            File gitDirLinkPath = processGitDirFile(manuallyConfiguredDir);
//
//            // If the linkPath was found from the file and it exists then use it.
//            if (isExistingDirectory(gitDirLinkPath)) {
//                return gitDirLinkPath;
//            }
//
//            /**
//             * FIXME: I think we should fail here because a manual path was set and it was not found
//             * but I'm leaving it falling back to searching for the git path because that is the current
//             * behaviour - Unluckypixie.
//             */
//        }

        return findProjectGitDirectory();
    }

    /**
     * Search up all the maven parent project heirarchy until a .git
     * directory is found.
     *
     * @return File which represents the location of the .git directory or NULL if none found.
     */
    private File findProjectGitDirectory() {
        MavenProject currentProject = this.mavenProject;

        while (currentProject != null) {
            File dir = getProjectGitDir(currentProject);

            if (isExistingDirectory(dir)) {
                return dir;
            }
            // If the path exists but is not a directory it might be a git submodule "gitdir" link.
            File gitDirLinkPath = processGitDirFile(dir);

            // If the linkPath was found from the file and it exists then use it.
            if (isExistingDirectory(gitDirLinkPath)) {
                return gitDirLinkPath;
            }

            /**
             * project.getParent always returns NULL for me, but if getParentArtifact returns
             * not null then there is actually a parent - seems like a bug in maven to me.
             */
            if (currentProject.getParent() == null && currentProject.getParentArtifact() != null) {
                Optional<MavenProject> maybeFoundParentProject = getReactorParentProject(currentProject);

                if (maybeFoundParentProject.isPresent())
                    currentProject = maybeFoundParentProject.get();

            } else {
                // Get the parent, or NULL if no parent AND no parentArtifact.
                currentProject = currentProject.getParent();
            }
        }

        return null;
    }

    /**
     * Find a project in the reactor by its artifact, I'm new to maven coding
     * so there may be a better way to do this, it would not be necessary
     * if project.getParent() actually worked.
     *
     * @return MavenProject parent project or NULL if no parent available
     */
    private Optional<MavenProject> getReactorParentProject(MavenProject project) {
        Artifact parentArtifact = project.getParentArtifact();

        if (parentArtifact != null) {
            for (MavenProject reactorProject : this.reactorProjects) {
                if (reactorProject.getArtifactId().equals(parentArtifact.getArtifactId())) {
                    return Optional.of(reactorProject);
                }
            }
        }

        return Optional.absent();
    }

    /**
     * Load a ".git" git submodule file and read the gitdir path from it.
     *
     * @return File object with path loaded or null
     */
    private File processGitDirFile(File file) {
        try {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(file));

                // There should be just one line in the file, e.g.
                // "gitdir: /usr/local/src/parentproject/.git/modules/submodule"
                String line = reader.readLine();

                // Separate the key and the value in the string.
                String[] parts = line.split(": ");

                // If we don't have 2 parts or if the key is not gitdir then give up.
                if (parts.length != 2 || !parts[0].equals("gitdir")) {
                    return null;
                }

                // All seems ok so return the "gitdir" value read from the file.
                return new File(file.getParentFile(), parts[1]);
            } catch (FileNotFoundException e) {
                return null;
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static File getProjectGitDir(MavenProject mavenProject) {
        // FIXME Shouldn't this look at the dotGitDirectory property (if set) for the given project?
        return new File(mavenProject.getBasedir(), Constants.DOT_GIT);
    }

    private static boolean isExistingDirectory(File fileLocation) {
        return fileLocation != null && fileLocation.exists() && fileLocation.isDirectory();
    }
}
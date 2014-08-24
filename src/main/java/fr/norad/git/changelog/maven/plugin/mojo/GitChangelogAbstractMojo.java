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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.jgit.revwalk.RevCommit;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import fr.norad.git.changelog.maven.plugin.ChangelogWriter;
import fr.norad.git.changelog.maven.plugin.Commit;
import fr.norad.git.changelog.maven.plugin.GitDirLocator;
import fr.norad.git.changelog.maven.plugin.GitLogs;

public abstract class GitChangelogAbstractMojo extends GitChangelogConfiguration {

    protected abstract void run() throws MojoExecutionException, MojoFailureException;

    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            run();
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("The maven-git-changelog-plugin encountered an exception: \n"
                    + stringify(e), e);
        }
    }

    void writeChangeLog(PrintStream out) throws MojoExecutionException {
        GitDirLocator gitLocator = new GitDirLocator(getProject(), getReactorProjects());
        File gitDirectory = gitLocator.lookupGitDirectory(null);
        if (gitDirectory == null) {
            throw new MojoExecutionException("Cannot found git directory");
        }

        Iterable<Commit> commits = new GitLogs(gitDirectory).logs(withoutCommitsToIgnore, transformToCommit);
        new ChangelogWriter(this).write(out, commits);
    }

    final Function<RevCommit, Commit> transformToCommit = new Function<RevCommit, Commit>() {


        @Override
        public Commit apply(RevCommit revCommit) {
            String message = revCommit.getShortMessage();

            Pair<Pattern, Set<String>> versionPatternAndCapture = getGitLogVersionPatternWithCaptureNames();
            Pair<Pattern, Set<String>> modifPatternAndCapture = getGitLogModifPatternWithCaptureNames();

            Matcher versionMatcher = versionPatternAndCapture.getLeft().matcher(message);
            Matcher modifMatcher = modifPatternAndCapture.getLeft().matcher(message);

            if (!modifMatcher.matches() && !versionMatcher.matches()) {
                return null;
            }

            Commit commit = new Commit(revCommit);
            if (versionMatcher.matches()) {
                commit.setRelease(true);
                matcherCaptureToMap(versionPatternAndCapture.getRight(), commit.getAttributes(), versionMatcher);
            } else {
                matcherCaptureToMap(modifPatternAndCapture.getRight(), commit.getAttributes(), modifMatcher);
            }
            return commit;
        }
    };

    final Predicate<RevCommit> withoutCommitsToIgnore = new Predicate<RevCommit>() {
        @Override
        public boolean apply(RevCommit revCommit) {
            for (Pattern pattern : getGitLogIgnorePatterns()) {
                if (pattern.matcher(revCommit.getShortMessage()).matches()) {
                    return false;
                }
            }
            return true;
        }
    };

    private String stringify(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private void matcherCaptureToMap(Set<String> captureNames, Map<String, String> map, Matcher matcher) {
        for (String name : captureNames) {
            try {
                map.put(name, matcher.group(name));
            } catch (IllegalArgumentException e) {
            }
        }
    }

}

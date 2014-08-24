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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class GitLogs {

    private final Repository repository;

    public GitLogs(File dotGitDirectory) {
        try {
            repository = FileRepositoryBuilder.create(dotGitDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read git repository");
        }
    }

    public Iterable<Commit> logs(Predicate<RevCommit> filter, Function<RevCommit, Commit> transform) {
        try {
            Git git = new Git(repository);
            LogCommand log = git.log();
            Iterable<RevCommit> revCommits = log.call();

            return transform(filter(revCommits, filter), transform);
        } catch (GitAPIException e) {
            throw new IllegalStateException("Cannot read git logs", e);
        }
    }
}

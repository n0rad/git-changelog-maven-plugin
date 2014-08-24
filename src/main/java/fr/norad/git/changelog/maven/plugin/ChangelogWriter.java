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

import static org.apache.commons.lang3.text.StrSubstitutor.replace;
import java.io.PrintStream;
import java.util.Date;
import fr.norad.git.changelog.maven.plugin.mojo.GitChangelogConfiguration;
import lombok.Data;

@Data
public class ChangelogWriter {

    private GitChangelogConfiguration config;

    public ChangelogWriter(GitChangelogConfiguration config) {
        this.config = config;
    }

    public void write(PrintStream out, Iterable<Commit> commits) {
        printHeader(out);
        printCurrentVersion(out);
        printModifications(out, commits);
        printFooter(out);
    }

    ////////////////////////////////////

    private void printModifications(PrintStream out, Iterable<Commit> commits) {
        for (Commit commit : commits) {
            if (commit == null) {
                continue;
            }
            if (commit.isRelease()) {
                out.println(replace(config.getChangelogVersionFormat(), commit.getAttributes()));
                printVersionHeader(out, commit);
            } else {
                out.println(replace(config.getChangelogFormat(), commit.getAttributes()));
            }
        }
    }

    private void printVersionHeader(PrintStream out, Commit commit) {
        String versionHeader = config.getChangelogVersionsHeader().get('V' + commit.getVersion());
        if (versionHeader != null) {
            out.println(versionHeader);
            out.println();
        }
    }

    private void printFooter(PrintStream out) {
        if (config.getChangelogFooter() == null) {
            return;
        }
        out.print(config.getChangelogFooter());
    }

    private void printHeader(PrintStream out) {
        if (config.getChangelogHeader() == null) {
            return;
        }
        out.print(config.getChangelogHeader());
    }

    private void printCurrentVersion(PrintStream out) {
        Commit commit = new Commit();
        commit.setCommitTime(new Date());
        commit.setVersion(config.getProject().getVersion());
        out.println(replace(config.getChangelogVersionFormat(), commit.getAttributes()));

        printVersionHeader(out, commit);
    }
}

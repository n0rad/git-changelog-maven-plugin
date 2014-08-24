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


import static fr.norad.git.changelog.maven.plugin.Commit.defaultKeyNames.*;
import static fr.norad.git.changelog.maven.plugin.Commit.defaultKeyNames.day;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Commit {

    private static final  SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Getter
    @Setter
    public boolean release;

    @Getter
    private final Map<String, String> attributes = new HashMap<>(12);

    public Commit() {
    }

    public Commit(RevCommit revCommit) {
        setAuthor(revCommit.getAuthorIdent());
        setCommitter(revCommit.getCommitterIdent());
        setShortMessage(revCommit.getShortMessage());
        setHash(revCommit.getName());
        setCommitTime(revCommit.getCommitTime());
    }

    public void setVersion(String version) {
        attributes.put(defaultKeyNames.version.name(), version);
    }

    public void setCommitTime(Date date) {
        setCommitTime((int) (date.getTime() / 1000));
    }

    public String getVersion() {
        return attributes.get(version.name());
    }

    //////////////////

    private void setHash(String hash) {
        attributes.put(defaultKeyNames.hash.name(), hash.substring(7));
    }

    private void setCommitTime(int time) {
        attributes.put(day.name(), dayFormatter.format(new Date((long) time * 1000)));
    }

    private void setShortMessage(String msg) {
        attributes.put(shortMessage.name(), msg);
    }

    private void setAuthor(PersonIdent author) {
        attributes.put(authorEmail.name(), author.getEmailAddress());
        attributes.put(authorName.name(), author.getName());
        attributes.put(authorTimezone.name(), author.getTimeZone().toString());
        attributes.put(authorWhen.name(), author.getWhen().toString());
    }

    private void setCommitter(PersonIdent committer) {
        attributes.put(committerEmail.name(), committer.getEmailAddress());
        attributes.put(committerName.name(), committer.getName());
        attributes.put(committerTimezone.name(), committer.getTimeZone().toString());
        attributes.put(committerWhen.name(), committer.getWhen().toString());
    }

    enum defaultKeyNames {
        version,
        hash,
        day,
        shortMessage,
        authorEmail,
        authorName,
        authorTimezone,
        authorWhen,
        committerEmail,
        committerName,
        committerTimezone,
        committerWhen,
    }

}

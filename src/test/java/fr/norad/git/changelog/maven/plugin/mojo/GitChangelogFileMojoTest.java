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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GitChangelogFileMojoTest {

    @Test
    public void should_detect_maven_release_plugin_log_and_capture_version() {
        Pattern compile = Pattern.compile("^\\[maven-release-plugin\\] prepare release .*-(?<version>\\d+[.\\d]*)");
        Matcher matcher = compile.matcher("[maven-release-plugin] prepare release server-3.0");
        Assertions.assertThat(matcher.matches()).isTrue();
        Assertions.assertThat(matcher.group("version")).isEqualTo("3.0");

//        Assertions.assertThat(compile.matcher("[maven-release-plugin] prepare release server-toto-3.0").group("version")).isEqualTo("3.0");

    }

}
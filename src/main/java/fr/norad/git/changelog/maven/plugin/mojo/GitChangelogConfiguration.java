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

import static java.util.regex.Pattern.compile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import lombok.Getter;

@SuppressWarnings("UnusedDeclaration")
public abstract class GitChangelogConfiguration extends AbstractMojo {

    @Getter
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Getter
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    private List<MavenProject> reactorProjects;

//    @Parameter(defaultValue = "${project.basedir}/.git")
//    private File dotGitDirectory;

    @Getter
    @Parameter(defaultValue = "${project.build.outputDirectory}/CHANGELOG.txt")
    private File targetFile;

    @Getter
    @Parameter(defaultValue = "true")
    private boolean skipPomProjects;

    @Getter
    @Parameter(defaultValue = "false")
    private boolean skip;


    @Parameter(defaultValue = "(?<message>.*)")
    private String gitLogCapturePattern;

    @Parameter(defaultValue = "^\\[maven-release-plugin\\] prepare for next development iteration")
    private List<String> gitLogIgnorePatterns;

    @Parameter(defaultValue = "^\\[maven-release-plugin\\] prepare release .*-(?<version>\\d+[.\\d]*)")
    private String gitLogVersionChangePattern;


    @Getter
    @Parameter(defaultValue = " * ${message}")
    private String changelogFormat;

    @Getter
    @Parameter(defaultValue = "\n  ${project.name} (${project.url})\n\n${project.description}\n\n")
    private String changelogHeader;

    @Getter
    @Parameter(defaultValue = "\n\n")
    private String changelogFooter;

    @Getter
    @Parameter(defaultValue = "\nV${version} (${day})\n===============")
    private String changelogVersionFormat;

    /**
     * Header to include on version before listing modifications.
     * Version tag start with 'V'
     */
    @Getter
    @Parameter
    private Map<String, String> changelogVersionsHeader = new HashMap<>();

    //////////////////////////////

    private Pair<Pattern, Set<String>> versionPatternWithCaptureNames;
    private Pair<Pattern, Set<String>> modifPatternWithCaptureNames;
    private List<Pattern> modifToIgnore;

    public Pair<Pattern, Set<String>> getGitLogVersionPatternWithCaptureNames() {
        if (versionPatternWithCaptureNames == null) {
            versionPatternWithCaptureNames = Pair.of(compile(gitLogVersionChangePattern),
                    getNamedGroupCandidates(gitLogVersionChangePattern));
        }
        return versionPatternWithCaptureNames;
    }

    public Pair<Pattern, Set<String>> getGitLogModifPatternWithCaptureNames() {
        if (modifPatternWithCaptureNames == null) {
            modifPatternWithCaptureNames = Pair.of(compile(gitLogCapturePattern),
                    getNamedGroupCandidates(gitLogCapturePattern));;
        }
        return modifPatternWithCaptureNames;
    }

    public List<Pattern> getGitLogIgnorePatterns() {
        if (modifToIgnore == null) {
            modifToIgnore = new ArrayList<>(gitLogIgnorePatterns.size());
            for (String modifIgnorePattern : gitLogIgnorePatterns) {
                modifToIgnore.add(compile(modifIgnorePattern));
            }
        }
        return modifToIgnore;
    }

    private Set<String> getNamedGroupCandidates(String regex) {
        Set<String> namedGroups = new TreeSet<>();
        Matcher m = compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }

}

package ai.foresignal.manifest;

import ai.foresignal.config.ForesignalVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

/**
 * The running build as manifests record it (prompt 116): the {@link ForesignalVersion} version and the git
 * commit, from {@code git.properties} when the build provides it, else {@code build-info.properties}
 * ({@code git.commit}), else the environment variable {@value #COMMIT_ENV}, else {@value #UNKNOWN}.
 */
@Component
public class BuildInfo {

    /** The environment variable a deployment may set to the commit it runs. */
    public static final String COMMIT_ENV = "FORESIGNAL_GIT_COMMIT";

    /** The commit when nothing provides one. */
    public static final String UNKNOWN = "unknown";

    private final String version;
    private final String gitCommit;

    /** Resolves the build once. */
    @Autowired
    public BuildInfo(ForesignalVersion version, ObjectProvider<GitProperties> git,
            ObjectProvider<BuildProperties> build) {
        this.version = version.version();
        this.gitCommit = commit(git.getIfAvailable(), build.getIfAvailable(), System.getenv(COMMIT_ENV));
    }

    /** A fixed build (tests). */
    public BuildInfo(String version, String gitCommit) {
        this.version = version;
        this.gitCommit = gitCommit;
    }

    static String commit(GitProperties git, BuildProperties build, String env) {
        if (git != null && git.getCommitId() != null && !git.getCommitId().isBlank()) {
            return git.getCommitId();
        }
        if (build != null && build.get("git.commit") != null && !build.get("git.commit").isBlank()) {
            return build.get("git.commit");
        }
        if (env != null && !env.isBlank()) {
            return env.strip();
        }
        return UNKNOWN;
    }

    /** The version. */
    public String version() {
        return version;
    }

    /** The git commit, or {@value #UNKNOWN}. */
    public String gitCommit() {
        return gitCommit;
    }
}

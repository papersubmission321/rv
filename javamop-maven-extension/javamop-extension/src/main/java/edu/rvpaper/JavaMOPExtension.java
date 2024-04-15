package edu.rvpaper;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Named
@Singleton
public class JavaMOPExtension extends AbstractEventSpy {
    private static final String MOP_AGENT_STRING = "-javaagent:${settings.localRepository}/javamop-agent/" +
            "javamop-agent/1.0/javamop-agent-1.0.jar";

    private void updateConfig(Xpp3Dom config) {
        Xpp3Dom argLine = config.getChild("argLine");

        boolean collectTraces = System.getenv("COLLECT_TRACES") != null && System.getenv("COLLECT_TRACES").equals("1");
        String jvmOptions = "-Xmx500g -XX:-UseGCOverheadLimit";

        String agentPath = System.getenv("MOP_AGENT_PATH") != null ? System.getenv("MOP_AGENT_PATH") : MOP_AGENT_STRING;

        if (argLine != null) {
            if (collectTraces) {
                argLine.setValue(argLine.getValue() + " " + agentPath + " " + jvmOptions);
            } else {
                argLine.setValue(argLine.getValue() + " " + agentPath);
            }
        } else {
            argLine = new Xpp3Dom("argLine");
            if (collectTraces) {
                argLine.setValue(agentPath + " " + jvmOptions);
            } else {
                argLine.setValue(agentPath);
            }
            config.addChild(argLine);
        }
    }

    private void updateSurefireVersion(Plugin plugin) {
        if (!plugin.getGroupId().equals("org.apache.maven.plugins") ||
                !plugin.getArtifactId().equals("maven-surefire-plugin")) {
            // Not Surefire
            return;
        }

        // getVersion will return null for project romix/java-concurrent-hash-trie-map
        String pluginVersion = plugin.getVersion() == null ? "0" : plugin.getVersion();
        ComparableVersion surefireVersion = new ComparableVersion(pluginVersion);
        ComparableVersion reasonableVersion = new ComparableVersion("3.1.2");
        if (surefireVersion.compareTo(reasonableVersion) < 0) {
            // Surefire is outdated, update it to `reasonableVersion`
            plugin.setVersion("3.1.2");
        }
    }

    private void checkAndUpdateConfiguration(ConfigurationContainer container) {
        Xpp3Dom configNode = (Xpp3Dom) container.getConfiguration();
        if (configNode == null) {
            configNode = new Xpp3Dom("configuration");
            container.setConfiguration(configNode);
        }
        updateConfig(configNode);
    }

    private void updateSurefire(MavenProject project) {
        for (Plugin plugin : project.getBuildPlugins()) {
            if (plugin.getGroupId().equals("org.apache.maven.plugins") &&
                    plugin.getArtifactId().equals("maven-surefire-plugin")) {
                System.out.println("JavaMOPExtension: checking surefire version...");
                updateSurefireVersion(plugin);

                if (System.getenv("ADD_AGENT") != null && System.getenv("ADD_AGENT").equals("0"))
                    continue;

                System.out.println("JavaMOPExtension: checking agent...");
                checkAndUpdateConfiguration(plugin);

                for (PluginExecution exe : plugin.getExecutions()) {
                    checkAndUpdateConfiguration(exe);
                }
            }
        }
    }

    @Override
    public void onEvent(Object event) {
        if (event instanceof ExecutionEvent) {
            ExecutionEvent e = (ExecutionEvent) event;
            if (e.getType() == ExecutionEvent.Type.SessionStarted) {
                List<MavenProject> sortedProjects = e.getSession().getProjectDependencyGraph().getSortedProjects();
                for (MavenProject project : sortedProjects) {
                    updateSurefire(project);
                }
            }
        }
    }
}

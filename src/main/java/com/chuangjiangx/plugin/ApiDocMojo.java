package com.chuangjiangx.plugin;

import com.chuangjiangx.doclet.ApiDoclet;
import com.chuangjiangx.generate.MdType;
import com.chuangjiangx.util.ContextUtil;

import com.sun.tools.javadoc.Main;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Goal which touches a timestamp file.process-sources
 *
 * @goal generate
 * @phase package
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("unused")
public class ApiDocMojo extends AbstractMojo {
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> projectRepos;

    /**
     * @component
     */
    private PlexusContainer container;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.eclipse.aether.impl.ArtifactResolver}"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * @parameter expression="${targetClass}"
     * @required
     */
    private String targetClass;
    /**
     * @parameter expression="${output}"
     * @required
     */
    private String output;
    /**
     * 项目编译输出目录
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private String buildOutputDirectory;
    /**
     * @parameter expression="${resource}"
     * @required
     */
    private List<String> resources = new ArrayList<>();
    /**
     * @parameter expression="${method}"
     */
    private List<String> methods = new ArrayList<>();
    /**
     * @parameter expression="${mdType}"
     */
    private String mdType;
    /**
     * @parameter expression="${project.build.sourceEncoding}"
     * @required
     */
    private String encoding = "UTF-8";

    /**
     * 执行入口
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ContextUtil.put(ContextUtil.OUTPUT_KEY, output);
        ContextUtil.put(ContextUtil.FILTER_METHODS_KEY,methods);
        ContextUtil.put(ContextUtil.MDTYPE_KEY, MdType.valueOf(mdType));
        String docletClassName = ApiDoclet.class.getName();

        List<String> commandArgumentList = new ArrayList<>();
        commandArgumentList.addAll(Arrays.asList("-doclet", docletClassName));
        commandArgumentList.addAll(Arrays.asList(
                "-sourcepath", this.resolveMultiModuleSourcePathList()));

        commandArgumentList.addAll(Arrays.asList("-encoding", this.encoding));
        String classpath = File.pathSeparator + this.buildOutputDirectory + File.pathSeparator
                + this.resolveProjectDependenciesPath() + File.pathSeparator
                + this.resolvePluginDependenciesPath();
        commandArgumentList.addAll(Arrays.asList("-classpath", classpath));

        //commandArgumentList.addAll(Arrays.asList("-subpackages", this.basePackage));
        //commandArgumentList.addAll(this.targetClasses);
        commandArgumentList.add(this.targetClass);
        String[] commandArray = commandArgumentList.toArray(new String[commandArgumentList.size()]);
        log.info(Arrays.asList(commandArray).toString());
        int status = Main.execute(commandArray);
        if (status != 0) {
            this.getLog().error("请完善javadoc注释");
        }
    }

    //处理源文件
    private String resolveMultiModuleSourcePathList() {
        StringBuilder sourcePathBuilder = new StringBuilder();
        for (String resource : this.resources) {
            sourcePathBuilder.append(resource).append(File.pathSeparator);
        }
        return sourcePathBuilder.toString();
    }

    //处理项目依赖
    private String resolveProjectDependenciesPath() {
        List<File> dependencyFileList = this.convertProjectDependencyToFile();
        if (dependencyFileList != null && dependencyFileList.size() > 0) {
            StringBuilder classpathBuilder = new StringBuilder();
            for (File file : dependencyFileList) {
                classpathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
            }
            classpathBuilder.deleteCharAt(classpathBuilder.length() - 1);
            return classpathBuilder.toString();
        } else {
            return "";
        }
    }

    //处理插件依赖
    private String resolvePluginDependenciesPath() {
        List<File> dependencyFileList = this.convertPluginDependencyToFile();

        StringBuilder classpathBuilder = new StringBuilder();
        for (File file : dependencyFileList) {
            classpathBuilder.append(file.getAbsolutePath()).append(File.pathSeparator);
        }
        classpathBuilder.deleteCharAt(classpathBuilder.length() - 1);
        return classpathBuilder.toString();
    }

    private List<File> convertPluginDependencyToFile() {
        PluginDescriptor pluginDescriptor = (PluginDescriptor) this.getPluginContext().get("pluginDescriptor");
        Artifact pluginArtifact = pluginDescriptor.getPluginArtifact();
        StringBuilder filePathBuilder = new StringBuilder(pluginArtifact.getFile().toString());

        int endIdx = filePathBuilder.indexOf(pluginArtifact.getGroupId().replace(".", File.separator));
        String mavenLocalRepoPath = filePathBuilder.substring(0, endIdx);

        List<File> dependencyFileList = new ArrayList<>();
        List<Artifact> artifactList = pluginDescriptor.getArtifacts();
        for (Artifact artifact : artifactList) {
            String groupPath = artifact.getGroupId().replace(".", File.separator);
            String jarPath = mavenLocalRepoPath + groupPath + File.separator
                    + artifact.getArtifactId() + File.separator + artifact.getVersion()
                    + File.separator + artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar";
            dependencyFileList.add(new File(jarPath));
        }
        return dependencyFileList;
    }

    private List<File> convertProjectDependencyToFile() {
        PluginDescriptor pluginDescriptor = (PluginDescriptor) this.getPluginContext().get("pluginDescriptor");
        MavenProject mavenProject = (MavenProject) this.getPluginContext().get("project");
        DependencyNode node;
        try {
            DefaultDependencyGraphBuilder dependencyGraphBuilder = new DefaultDependencyGraphBuilder();
            dependencyGraphBuilder.contextualize(this.container.getContext());
            dependencyGraphBuilder.enableLogging(new PlexusLoggerAdapter(this.getLog()));
            ArtifactFilter artifactFilter = new ScopeArtifactFilter("compile");

            Collection<MavenProject> projects = new HashSet<>();
            //获取parent
            MavenProject parent = mavenProject;
            for (parent = parent.getParent(); parent.getParent() != null; parent = parent.getParent()) {
                log.info("");
            }
            projects.add(parent);

            node = dependencyGraphBuilder.buildDependencyGraph(mavenProject, artifactFilter, projects);
            CollectingDependencyNodeVisitor nodeVisitor = new CollectingDependencyNodeVisitor();
            node.accept(nodeVisitor);
            List<Artifact> artifactList = new ArrayList<>();

            for (DependencyNode dependencyNode : nodeVisitor.getNodes()) {
                artifactList.addAll(this.buildAllArtifactList(dependencyNode));
            }

            Artifact pluginArtifact = pluginDescriptor.getPluginArtifact();
            StringBuilder filePathBuilder = new StringBuilder(pluginArtifact.getFile().toString());

            int endIdx = filePathBuilder.indexOf(pluginArtifact.getGroupId().replace(".", File.separator));
            String mavenLocalRepoPath = filePathBuilder.substring(0, endIdx);

            List<File> dependencyFileList = new ArrayList<>();
            for (Artifact artifact : artifactList) {
                String groupPath = artifact.getGroupId().replace(".", File.separator);
                String version = artifact.getVersion();
                if (artifact.isSnapshot()) {
                    version = artifact.getBaseVersion();
                }
                String jarPath = mavenLocalRepoPath + groupPath + File.separator
                        + artifact.getArtifactId() + File.separator + version
                        + File.separator + artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar";
                dependencyFileList.add(new File(jarPath));
            }
            return dependencyFileList;
        } catch (DependencyGraphBuilderException ex) {
            this.getLog().warn("DependencyGraphBuilder could not resolve dependency graph.", ex);
        } catch (Exception ex) {
            this.getLog().warn("Could not collect transient dependencies: " + ex);
        }
        return null;
    }


    private List<Artifact> buildAllArtifactList(DependencyNode dependencyNode) {
        List<Artifact> dependencyNodeList = new ArrayList<>();
        if (dependencyNode != null) {
            for (DependencyNode child : dependencyNode.getChildren()) {
                dependencyNodeList.add(child.getArtifact());
                if (child.getChildren() != null && child.getChildren().size() > 0) {
                    List<Artifact> children = buildAllArtifactList(child);
                    dependencyNodeList.addAll(children);
                }
            }
        }
        return dependencyNodeList;
    }
}

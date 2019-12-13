package com.xj.xrounter.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.plugins.ExtraPropertiesExtension

class XRouterPlugin implements Plugin<Project> {

    static final String APT_OPTION_NAME = "moduleName"
    public static final String XROUTER_API = "router-api"
    public static final String XROUTER_COMPILER = "router-compiler"
    public static final String XROUTER_ANNOTATION = "router-annotation"
    public static final String XROUTER_CONFIG = "xrouter_config"

    private Project project

    String DEFAULT_XROUTER_API_VERSION = "0.0.01"
    String DEFAULT_XROUTER_COMPILER_VERSION = "0.0.01"
    String DEFAULT_XROUTER_ANNOTATION_VERSION = "0.0.01"
    String androidBuildGradleVersion

    @Override
    void apply(Project project) {
        this.project = project
        if (!project.plugins.hasPlugin(AppPlugin)                                // AppPlugin
                && !project.plugins.hasPlugin(LibraryPlugin)                     // LibraryPlugin
                && !project.plugins.hasPlugin(TestPlugin)                        // TestPlugin
                && !project.plugins.hasPlugin("com.android.instantapp")       // InstantAppPlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.feature")          // FeaturePlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.dynamic-feature")) // DynamicFeaturePlugin, added in 3.2
        {
            throw new GradleException("android plugin required.")
        }

        project.rootProject.buildscript.configurations.each {
            if (it.name == ScriptHandler.CLASSPATH_CONFIGURATION) { // classpath
                it.resolvedConfiguration.firstLevelModuleDependencies.each {
                    // println("${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}")
                    if (it.moduleGroup == "com.android.tools.build" && it.moduleName == "gradle") {
                        androidBuildGradleVersion = it.moduleVersion
                    }
                }
            }
        }
        if (!androidBuildGradleVersion) {
            throw new IllegalArgumentException("Unknown android build gradle plugin version.")
        }


        project.extensions.create(XROUTER_CONFIG, XRouterExtension)
        XRouterExtension xrouterConfig = project.extensions[XROUTER_CONFIG]
        log("xrouterConfig=" + xrouterConfig)
        if (xrouterConfig == null) {
            xrouterConfig = new XRouterExtension()
        }

        handle(xrouterConfig, project)


    }

    private void handle(XRouterExtension xrouterConfig, Project project) {
        boolean isDebug = xrouterConfig.isDebug()


        log("apply XRouterPlugin")

        // kotlin project ?
        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        String compileConf = 'implementation'
        if (!is3_xVersion()) {
            compileConf = 'compile'
        }
        String aptConf = 'annotationProcessor'
        if (isKotlinProject) {
            aptConf = 'kapt'
        }

        // Add dependencies
        Project routerProject = project.rootProject.findProject(XROUTER_API)
        Project compilerProject = project.rootProject.findProject(XROUTER_COMPILER)
        Project annotationProject = project.rootProject.findProject(XROUTER_ANNOTATION)
        log("routerProject=" + routerProject + " ;compilerProject=" + compilerProject + " ;annotationProject=" + annotationProject)
        if (routerProject && compilerProject && annotationProject) { // local
            log("use local project")
            project.dependencies.add(compileConf, routerProject)
            project.dependencies.add(compileConf, annotationProject)
            project.dependencies.add(aptConf, compilerProject)
        } else {
            log("use remote jar")
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("apiVersion")) {
                DEFAULT_XROUTER_API_VERSION = ext.get("apiVersion")
            }
            if (ext.has("compilerVersion")) {
                DEFAULT_XROUTER_COMPILER_VERSION = ext.get("compilerVersion")
            }

            if (ext.has("annotationVersion")) {
                DEFAULT_XROUTER_COMPILER_VERSION = ext.get("annotationVersion")
            }
            project.dependencies.add(compileConf,
                    "com.xj.xrouter:router-api:${DEFAULT_XROUTER_API_VERSION}")
            project.dependencies.add(aptConf,
                    "com.xj.xrouter:router-compiler:${DEFAULT_XROUTER_COMPILER_VERSION}")
            project.dependencies.add(compileConf,
                    "com.xj.xrouter:router-annotion:${DEFAULT_XROUTER_ANNOTATION_VERSION}")
        }

        def android = project.extensions.findByName("android")
        if (android) {
            log("find android")
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            android.productFlavors.all {
                log("projectName=" + project.name)
                it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            }
        }

        if (project.plugins.hasPlugin(AppPlugin)) {
            XRouterTransform transform = new XRouterTransform(project, xrouterConfig)
            log("register RouterTransform")
            android.registerTransform(transform)
            project.afterEvaluate {
                log("afterEvaluate xrouterConfig=" + xrouterConfig)
                transform.xRouterExtension = xrouterConfig
            }
        }
    }

    private log(String result) {
        project.logger.lifecycle(result)
    }

    /**
     * Whether the android gradle plugin version is 3.x
     */
    boolean is3_xVersion() {
        return androidBuildGradleVersion.split("\\.")[0].toInteger() >= 3
    }

}
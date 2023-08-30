plugins {
    id("java-gradle-plugin")
}

dependencies {
    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation(project(":kotlin-spring-boot-tools:kotlin-spring-boot-loader-tools"))


    implementation("org.apache.commons:commons-compress:$commonCompressVersion")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

gradlePlugin {
    plugins {
        val kotlinSpringBootPlugin = PluginDeclaration("kotlinSpringBootPlugin")
        kotlinSpringBootPlugin.id = "com.wanna.boot"
        kotlinSpringBootPlugin.description = "Kotlin Spring Boot Gradle Plugin"
        kotlinSpringBootPlugin.displayName = "Kotlin Spring Boot Gradle Plugin"
        kotlinSpringBootPlugin.implementationClass = "com.wanna.boot.gradle.plugin.SpringBootPlugin"

        this.add(kotlinSpringBootPlugin)
    }
}
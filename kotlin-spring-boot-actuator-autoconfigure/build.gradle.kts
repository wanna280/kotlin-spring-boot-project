
dependencies {
    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-beans:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-context:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-jcl:$kotlinSpringFrameworkVersion")

    implementation(project(":kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-boot-actuator"))


    // CompileOnly Optional
    compileOnly("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    compileOnly("io.micrometer:micrometer-core:1.9.5")  // metrics
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")


    testImplementation("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}
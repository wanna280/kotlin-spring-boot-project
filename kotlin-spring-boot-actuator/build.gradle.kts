dependencies {
    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-beans:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-context:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-jcl:$kotlinSpringFrameworkVersion")

    implementation(project(":kotlin-spring-boot"))

    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")


    // CompileOnly Optional
    compileOnly("io.micrometer:micrometer-core:1.9.5")  // metrics
    compileOnly("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    testImplementation("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}
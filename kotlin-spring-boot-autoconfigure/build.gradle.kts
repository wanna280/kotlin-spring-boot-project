dependencies {
    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-beans:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-context:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-aop:$kotlinSpringFrameworkVersion")


    implementation(project(":kotlin-spring-boot"))

    // CompileOnly Optional
    compileOnly("com.wanna.spring:kotlin-spring-jcl:$kotlinSpringFrameworkVersion")
    compileOnly("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")

    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("com.google.code.gson:gson:$gsonVersion")  // gson


    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    testImplementation("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}
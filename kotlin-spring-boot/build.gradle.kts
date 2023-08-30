dependencies {

    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-beans:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-context:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-jcl:$kotlinSpringFrameworkVersion")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")

    // compileOnly
    compileOnly("com.wanna.spring:kotlin-spring-aop:$kotlinSpringFrameworkVersion")
    compileOnly("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")  // apache httpcomponents
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion") // tomcat-core
    compileOnly("ch.qos.logback:logback-classic:$logbackVersion")  // logback


    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
}

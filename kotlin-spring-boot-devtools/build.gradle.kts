dependencies {
    implementation("com.wanna.spring:kotlin-spring-core:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-beans:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-context:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-aop:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-web:$kotlinSpringFrameworkVersion")
    implementation("com.wanna.spring:kotlin-spring-jcl:$kotlinSpringFrameworkVersion")

    implementation(project(":kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-autoconfigure"))
}

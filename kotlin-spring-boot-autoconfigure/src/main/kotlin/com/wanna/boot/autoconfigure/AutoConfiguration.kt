package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 标识这是一个SpringBoot的自动装配的配置类
 */
@Retention(AnnotationRetention.RUNTIME)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore
@AutoConfigureAfter
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
annotation class AutoConfiguration(

    @get:AliasFor(annotation = Configuration::class)
    val value: String = "",

    @get:AliasFor(annotation = AutoConfigureBefore::class, attribute = "value")
    val before: Array<KClass<*>> = [],

    @get:AliasFor(annotation = AutoConfigureBefore::class, attribute = "name")
    val beforeName: Array<String> = [],

    @get:AliasFor(annotation = AutoConfigureAfter::class, attribute = "value")
    val after: Array<KClass<*>> = [],

    @get:AliasFor(annotation = AutoConfigureAfter::class, attribute = "name")
    val afterName: Array<String> = [],
)

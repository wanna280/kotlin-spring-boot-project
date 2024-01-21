package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional

/**
 * WebApplicationType的类型的条件装配注解
 *
 * @see OnWebApplicationCondition
 * @see Type
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnWebApplicationCondition::class)
annotation class ConditionalOnWebApplication(val type: Type = Type.ANY) {
    enum class Type { ANY, MVC, SERVLET }
}
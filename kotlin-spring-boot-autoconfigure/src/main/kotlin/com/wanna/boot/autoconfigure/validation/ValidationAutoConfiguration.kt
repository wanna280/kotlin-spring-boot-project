package com.wanna.boot.autoconfigure.validation

import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnResource
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Lazy
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.validation.beanvalidation.LocalValidatorFactoryBean
import com.wanna.framework.validation.beanvalidation.MethodValidationPostProcessor
import javax.validation.Validation
import javax.validation.Validator


/**
 * Bean Validation的自动配置类, 提供对于JSR303的参数校验的支持
 *
 * @see Validator
 * @see MethodValidationPostProcessor
 * @see LocalValidatorFactoryBean
 */
@Order(Ordered.ORDER_HIGHEST + 20)  // highest order
@ConditionalOnResource(resources = ["classpath:META-INF/services/javax.validation.spi.ValidationProvider"])
@ConditionalOnClass(value = [javax.validation.Validator::class])
@Configuration(proxyBeanMethods = false)
open class ValidationAutoConfiguration {

    /**
     * 注册一个LocalValidatorFactoryBean, 提供Spring的Validator和javax.validation的Validator的Bean的支持
     *
     * @return LocalValidatorFactoryBean
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean([Validator::class])
    open fun defaultValidator(): LocalValidatorFactoryBean {
        val localValidatorFactoryBean = LocalValidatorFactoryBean()
        localValidatorFactoryBean.setTargetValidator(Validation.buildDefaultValidatorFactory().validator)
        return localValidatorFactoryBean
    }

    /**
     * 提供对于`@Validated`注解的参数检验的BeanPostProcessor
     *
     * @param validator [Validator]
     * @return MethodValidationPostProcessor
     */
    @Bean
    @ConditionalOnClass(value = [javax.validation.Validator::class])
    @ConditionalOnMissingBean
    open fun methodValidationPostProcessor(@Lazy validator: Validator): MethodValidationPostProcessor {
        val methodValidationPostProcessor = MethodValidationPostProcessor()
        methodValidationPostProcessor.setValidator(validator)
        return methodValidationPostProcessor
    }
}
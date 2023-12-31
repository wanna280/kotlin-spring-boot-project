package com.wanna.boot.context.config

import com.wanna.boot.context.properties.bind.PlaceholdersResolver
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.PropertyPlaceholderHelper
import com.wanna.framework.util.SystemPropertyUtils

/**
 * 基于[ConfigDataEnvironmentContributor]去实现的[PlaceholdersResolver],
 * 利用[ConfigDataEnvironmentContributor]当中的[PropertySource]去提供占位符的解析
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
class ConfigDataEnvironmentContributorPlaceholdersResolver(
    private val contributors: Iterable<ConfigDataEnvironmentContributor>,
    @Nullable private val activationContext: ConfigDataActivationContext?,
    @Nullable private val activeContributor: ConfigDataEnvironmentContributor?,
    private val failOnResolveFromInactiveContributor: Boolean
) : PlaceholdersResolver {

    /**
     * 提供占位符的解析的解析的Helper工具类
     */
    private val helper = PropertyPlaceholderHelper(
        SystemPropertyUtils.PLACEHOLDER_PREFIX,
        SystemPropertyUtils.PLACEHOLDER_SUFFIX,
        SystemPropertyUtils.VALUE_SEPARATOR
    )

    /**
     * 执行占位符的解析
     *
     * @param value 待解析的占位符(可以为null)
     * @return 解析完成的结果(有可能为null)
     */
    @Nullable
    override fun resolvePlaceholder(@Nullable value: Any?): Any? {
        if (value is String) {
            this.helper.replacePlaceholder(value, this::resolvePlaceholder)
        }
        return value
    }

    /**
     * 利用[ConfigDataEnvironmentContributor]当中的[PropertySource]去提供占位符的解析
     *
     * @param placeholder 待解析的占位符
     * @return 根据[PropertySource]去解析给定的占位符的最终结果
     */
    @Nullable
    private fun resolvePlaceholder(placeholder: String): String? {
        var result: Any? = null

        for (contributor in this.contributors) {
            val propertySource = contributor.getPropertySource()
            val value = propertySource?.getProperty(placeholder)

            result = result ?: value
        }
        return result?.toString()
    }
}
package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnProperty
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.web.mvc.server.NettyWebServerFactory
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Primary
import com.wanna.framework.context.format.support.FormattingConversionService
import com.wanna.framework.core.io.ClassPathResource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.DispatcherHandlerImpl
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration
import com.wanna.framework.web.handler.SimpleUrlHandlerMapping
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import com.wanna.framework.web.resource.ResourceHttpRequestHandler
import com.wanna.boot.web.embedded.netty.NettyServerHandler
import com.wanna.boot.web.embedded.netty.NettyWebServerFactoryImpl

/**
 * 只有在WebMvc下才生效的自动配置类, 给SpringBeanFactory当中导入MVC相关的配置类
 *
 * @see DelegatingWebMvcConfiguration
 * @see DispatcherHandler
 */
@ConditionalOnWebApplication(ConditionalOnWebApplication.Type.MVC)
@EnableConfigurationProperties([NettyWebServerProperties::class])  // 导入WebServerProperties配置文件
@Configuration(proxyBeanMethods = false)
open class NettyMvcAutoConfiguration : ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Bean
    @ConditionalOnMissingBean
    open fun dispatcherHandler(): DispatcherHandler {
        return DispatcherHandlerImpl()
    }

    /**
     * 给SpringBeanFactory当中导入一个NettyWebServerFactory
     * 它的作用是, 为Spring的ApplicationContext去提供WebServer, 
     * 并提供WebServer启动与关闭等相关的操作
     *
     * @param properties WebServer的配置信息
     */
    @Bean
    @ConditionalOnMissingBean
    open fun nettyWebServerFactory(properties: NettyWebServerProperties): NettyWebServerFactory {
        val nettyWebServerFactory = NettyWebServerFactoryImpl()
        nettyWebServerFactory.setHandler(NettyServerHandler(this.applicationContext))
        nettyWebServerFactory.setPort(properties.port)
        nettyWebServerFactory.setBossGroupThreads(properties.bossCount)
        nettyWebServerFactory.setWorkerGroupThreads(properties.workerCount)
        return nettyWebServerFactory
    }

    /**
     * 开启SpringMVC的的配置的类, 导入SpringMVC需要用到的各个相关的组件
     */
    @Configuration(proxyBeanMethods = false)
    open class EnableWebMvcConfiguration : DelegatingWebMvcConfiguration() {

        @Primary  // set to Primary
        @Bean
        @Qualifier("requestMappingHandlerMapping")
        override fun requestMappingHandlerMapping(
            @Qualifier("mvcContentNegotiationManager") contentNegotiationManager: ContentNegotiationManager,
            @Qualifier("mvcConversionService") conversionService: FormattingConversionService
        ): RequestMappingHandlerMapping {
            return super.requestMappingHandlerMapping(contentNegotiationManager, conversionService)
        }
    }

    /**
     * 处理"favicon.ico"请求的配置类, 在SpringBoot2.2.x之前存在, 2.2.x之后取消了(根据默认Logo知道网站开发框架, 泄露隐私)
     */
    @ConditionalOnProperty(value = ["spring.mvc.favicon.enabled"], matchIfMissing = true)
    @Configuration(proxyBeanMethods = false)
    class FaviconConfiguration : ResourceLoaderAware {
        private lateinit var resourceLoader: ResourceLoader

        override fun setResourceLoader(resourceLoader: ResourceLoader) {
            this.resourceLoader = resourceLoader
        }

        @Bean
        @Qualifier("faviconHandlerMapping")
        fun faviconHandlerMapping(@Qualifier("faviconRequestHandler") requestHandler: ResourceHttpRequestHandler): SimpleUrlHandlerMapping {
            val handlerMapping = SimpleUrlHandlerMapping()
            handlerMapping.setUrlMap(mapOf("/favicon.ico" to requestHandler))
            return handlerMapping
        }

        @Bean
        @Qualifier("faviconRequestHandler")
        fun faviconRequestHandler(): ResourceHttpRequestHandler {
            val handler = ResourceHttpRequestHandler()
            handler.setLocations(resolveFaviconLocations())
            return handler
        }

        /**
         * 解析Favicon的位置
         */
        private fun resolveFaviconLocations(): List<Resource> {
            val locations = ArrayList<Resource>()
            locations += ClassPathResource("/")
            return locations;
        }
    }
}
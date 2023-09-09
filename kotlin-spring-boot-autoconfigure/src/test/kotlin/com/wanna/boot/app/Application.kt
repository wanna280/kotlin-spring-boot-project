package com.wanna.boot.app

import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ObjectProvider
import com.wanna.framework.beans.factory.annotation.Qualifier
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.handler.HandlerAdapter
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/9/10
 */
@SpringBootApplication
class Application {

    @Autowired
    private var beanPostProcessors: List<BeanPostProcessor>? = null

    @Autowired
    private var beanPostProcessorMap: Map<String, BeanPostProcessor>? = null

    @Autowired
    private var handlerMappings: List<HandlerMapping>? = null

    @Autowired
    private var handlerAdapters: List<HandlerAdapter>? = null

    @Autowired
    private var applicationContext: ApplicationContext? = null

    @Autowired
    private var beanFactory: BeanFactory? = null

    @Autowired
    private var resourceLoader: ResourceLoader? = null

    @Autowired
    private var conversionService: ConversionService? = null

    @Autowired
    private var dispatcherHandlerOptional: Optional<DispatcherHandler>? = null

    @Autowired
    private var dispatcherHandlerObjectProvider: ObjectProvider<DispatcherHandler>? = null

    @Autowired
    @Qualifier("systemProperties")
    private var properties: Map<Any, Any>? = null

    @Autowired
    private var stringMap: Map<String, String>? = null

    @Bean
    fun stringMap(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        map["key"] = "wanna"
        return map
    }
}


fun main() {
    val applicationContext = runSpringApplication<Application>()
    val application = applicationContext.getBean(Application::class.java)
    println(applicationContext)
}
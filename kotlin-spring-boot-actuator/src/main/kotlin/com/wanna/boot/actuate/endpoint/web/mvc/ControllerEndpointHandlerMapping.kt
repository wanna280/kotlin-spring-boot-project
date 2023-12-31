package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.annotation.ExposableControllerEndpoint
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

/**
 * Controller的Endpoint的HandlerMapping
 */
open class ControllerEndpointHandlerMapping(
    endpoints: Collection<ExposableControllerEndpoint>,
    private val endpointMapping: EndpointMapping,
    private val corsConfig: CorsConfiguration?
) : RequestMappingHandlerMapping() {

    // Handlers, 建立Controller与Endpoint之间的映射关系, 方便通过Controller去获取到ControllerEndpoint
    private val handlers: Map<Any, ExposableControllerEndpoint> = endpoints.associateBy { it.getController() }

    /**
     * 初始化HandlerMethods, 将所有的ControllerEndpoint的bean上的RequestMapping方法去注册为HandlerMethod
     */
    override fun initHandlerMethods() {
        handlers.keys.forEach(this::detectHandlerMethods)
    }

    /**
     * 重写注册HandlerMethod的方式, 我们需要给RequestMapping的路径去拼接上"/actuator"和endpointId的前缀
     *
     * @param handler handler(ControllerEndpointObject)
     * @param mapping RequestMappingInfo
     * @param method 目标HandlerMethod
     */
    override fun registerHandlerMethod(handler: Any, method: Method, mapping: RequestMappingInfo) {
        val controllerEndpoint = this.handlers[handler]!!
        val mappingInfo = getEndpointMappedPattern(controllerEndpoint, mapping)
        super.registerHandlerMethod(handler, method, mappingInfo)
    }

    /**
     * 获取ControllerEndpoint的RequestMappingInfo, 需要将将路径去进行拼接,
     * 路径的格式为"/actuator/{endpointId}/{classMapping}/{methodMapping}"
     *
     * @param endpoint ControllerEndpoint
     * @param mapping RequestMappingInfo
     * @return 重新去进行构建的RequestMappingInfo(重新拼接了路径, 别的没改变)
     */
    private fun getEndpointMappedPattern(
        endpoint: ExposableControllerEndpoint,
        mapping: RequestMappingInfo
    ): RequestMappingInfo {
        // Note: basePath="/actuator", rootPath=endpointId, sub=原来的RequestMapping的路径信息
        val paths = mapping.pathPatternsCondition.paths
            .map { sub -> endpointMapping.createSubPath(endpoint.getRootPath() + sub) }
            .toTypedArray()
        return RequestMappingInfo.Builder()
            .paths(*paths)
            .methods(mapping.methodsCondition)
            .headers(mapping.headersCondition)
            .params(mapping.paramsCondition)
            .produces(mapping.producesCondition)
            .build()
    }

    /**
     * 判断是否有CorsConfigurationSource? (似乎也不必重写, 因为父类会根据MappingRegistry去进行匹配)
     * 因为这个HandlerMapping不会被配置全局的CorsConfigurationSource,
     * 因此我们不必使用父类的判断, 我们直接根据this.corsConfig去进行判断
     *
     * @param handler handler
     */
    override fun hasCorsConfigurationSource(handler: Any) = this.corsConfig != null

    /**
     * 初始化ControllerEndpoint的CorsConfiguration
     *
     * @param handler handler
     * @param mapping mapping
     * @param method method
     */
    override fun initCorsConfiguration(handler: Any, method: Method, mapping: RequestMappingInfo) = this.corsConfig
}
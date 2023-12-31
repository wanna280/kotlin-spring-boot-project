package com.wanna.boot.devtools.restart

import com.wanna.boot.context.event.ApplicationFailedEvent
import com.wanna.boot.context.event.ApplicationPreparedEvent
import com.wanna.boot.context.event.ApplicationStartingEvent
import com.wanna.framework.context.event.ApplicationEvent
import com.wanna.framework.context.event.ApplicationListener

/**
 * Restart的ApplicationListener, 负责监听SpringApplication的相关事件, 并去去初始化单例的Restarter
 *
 * @see ApplicationStartingEvent
 * @see ApplicationPreparedEvent
 * @see ApplicationFailedEvent
 */
class RestartApplicationListener : ApplicationListener<ApplicationEvent> {

    /**
     * 处理ApplicationContext发布的[ApplicationEvent]事件
     *
     * @see ApplicationStartingEvent
     * @see ApplicationPreparedEvent
     * @see ApplicationFailedEvent
     *
     * @param event event
     */
    override fun onApplicationEvent(event: ApplicationEvent) {
        // 在SpringApplication开始启动时, 需要去初始化Restarter
        // 第一次去执行的ApplicationContext的启动时, 才会负责去创建Restarter
        if (event is ApplicationStartingEvent) {
            Restarter.initialize(event.args, DefaultRestartInitializer())
        }

        val restarter = Restarter.getInstance()!!
        // 如果Application已经启动好了, 那么需要将ApplicationContext设置到Restarter当中
        if (event is ApplicationPreparedEvent) {
            restarter.prepare(event.context)
        }

        // 如果Application启动失败, 那么需要remove ApplicationContext
        if (event is ApplicationFailedEvent && event.context != null) {
            restarter.remove(event.context!!)
        }
    }
}
package com.wanna.boot

import com.wanna.framework.core.environment.Environment
import java.io.PrintStream

/**
 * 这是一个SpringBoot的Banner, 负责打印SpringBoot的Logo的Banner
 *
 * @see Banner
 */
open class SpringBootBanner : Banner {
    companion object {
        /**
         * Banner的字符串
         */
        @JvmStatic
        private val BANNER: Array<String> = arrayOf(
            "", "  .   ____          _            __ _ _",
            " /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\", "( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\",
            " \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )", "  '  |____| .__|_| |_|_| |_\\__, | / / / /",
            " =========|_|==============|___/=/_/_/_/"
        )
    }

    /**
     * 打印Banner
     *
     * @param environment Environment
     * @param sourceClass 源类
     * @param printStream 输出流
     */
    override fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream) {
        BANNER.forEach { printStream.println(it) }
    }
}
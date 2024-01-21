package com.wanna.boot.autoconfigureprocessor

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.TreeMap
import javax.annotation.Nullable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.StandardLocation

/**
 * SpringBoot的自动装配的元信息的收集的注解处理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2024/1/21
 */
@SupportedAnnotationTypes(
    value = [
        "com.wanna.boot.autoconfigure.condition.ConditionalOnClass",
        "com.wanna.boot.autoconfigure.condition.ConditionalOnBean",
        "com.wanna.boot.autoconfigure.condition.ConditionalOnSingleCandidate",
        "com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication",
        "com.wanna.boot.autoconfigure.AutoConfigureBefore",
        "com.wanna.boot.autoconfigure.AutoConfigureAfter",
        "com.wanna.boot.autoconfigure.AutoConfigureOrder",
        "com.wanna.boot.autoconfigure.AutoConfiguration"
    ]
)
class AutoConfigureAnnotationProcessor : AbstractProcessor() {

    companion object {
        /**
         * 存放自动装配的元数据的位置
         */
        const val PROPERTIES_PATH = "META-INF/spring-autoconfigure-metadata.properties"
    }

    /**
     * 最终需要去进行收集的Metadata数据, 格式为"{className}.{propertyName}=xxx,yyy",
     * 表达的含义是className对应的类上标注了propertyName这个注解, 并且配置了xxx和yyy的配置
     */
    private val properties = TreeMap<String, String>()

    /**
     * 为properties提供属性的生成的PropertyGenerator
     */
    private val propertyGenerators: MutableList<PropertyGenerator> = ArrayList()

    init {
        // 添加为Conditional相关的注解提供属性的生成的PropertyGenerator
        addConditionPropertyGenerators()

        // 添加为AutoConfiguration相关的注解提供属性的生成的PropertyGenerator
        addAutoConfigurePropertyGenerators()
    }

    /**
     * 添加为Conditional相关的注解提供属性的生成的PropertyGenerator
     */
    private fun addConditionPropertyGenerators() {
        val annotationPackage = "com.wanna.boot.autoconfigure.condition"

        propertyGenerators += PropertyGenerator.of(annotationPackage, "ConditionalOnClass")
            .withAnnotation(OnClassConditionValueExtractor())

        propertyGenerators += PropertyGenerator.of(annotationPackage, "ConditionalOnBean")
            .withAnnotation(OnBeanConditionValueExtractor())

        propertyGenerators += PropertyGenerator.of(annotationPackage, "ConditionalOnSingleCandidate")
            .withAnnotation(OnBeanConditionValueExtractor())

        propertyGenerators += PropertyGenerator.of(annotationPackage, "ConditionalOnWebApplication")
            .withAnnotation(ValueExtractor.allFrom("type"))
    }

    /**
     * 添加为AutoConfiguration相关的注解提供属性的生成的PropertyGenerator
     */
    private fun addAutoConfigurePropertyGenerators() {
        val annotationPackage = "com.wanna.boot.autoconfigure"

        propertyGenerators += PropertyGenerator.of(annotationPackage, "AutoConfigureBefore", true)
            .withAnnotation(ValueExtractor.allFrom("value", "name"))
            .withAnnotation("AutoConfiguration", ValueExtractor.allFrom("before", "beforeName"))

        propertyGenerators += PropertyGenerator.of(annotationPackage, "AutoConfigureAfter", true)
            .withAnnotation(ValueExtractor.allFrom("value", "name"))
            .withAnnotation("AutoConfiguration", ValueExtractor.allFrom("after", "afterName"))

        propertyGenerators += PropertyGenerator.of(annotationPackage, "AutoConfigureOrder")
            .withAnnotation(ValueExtractor.allFrom("value"))
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        // 遍历所有的propertyGenerator, 依次尝试去进行处理
        for (propertyGenerator in propertyGenerators) {
            process(roundEnv, propertyGenerator)
        }

        // 在处理完成之后, 将元数据写入到"META-INF/spring-autoconfigure-metadata.properties"当中
        if (roundEnv.processingOver()) {
            try {
                writeProperties()
            } catch (ex: Exception) {
                throw IllegalStateException("Failed to write metadata", ex)
            }
        }
        return false
    }

    /**
     * 单个[PropertyGenerator]尝试去它支持去进行处理的注解进行处理
     *
     * @param roundEnv 维护正在编译的类的信息
     * @param generator PropertyGenerator
     */
    private fun process(roundEnv: RoundEnvironment, generator: PropertyGenerator) {
        // 获取当前的Generator支持去处理的注解列表
        for (annotationName in generator.getSupportAnnotations()) {

            // 根据注解类名, 去获取到注解类型TypeElement
            val annotationType = this.processingEnv.elementUtils.getTypeElement(annotationName) ?: continue

            // 获取到所有的标注了当前注解的类的列表, 一个element代表了一个标注了该注解的类
            for (element in roundEnv.getElementsAnnotatedWith(annotationType)) {
                processElement(element, generator, annotationName)
            }
        }
    }

    /**
     * 处理单个类上的单个标注的注解
     *
     * @param element 当前正在处理的类
     * @param generator generator
     * @param annotationName 当前类上需要去进行check的注解类名
     */
    private fun processElement(element: Element, generator: PropertyGenerator, annotationName: String) {
        try {
            // 获取到类名
            val qualifiedClassName = Elements.getQualifiedName(element) ?: return
            // 获取到类上标注的目标注解对象AnnotationMirror
            val annotationMirror = getAnnotation(element, annotationName) ?: return

            // 使用ValueExtractor, 从该注解对象当中提取到对应的属性值列表
            val annotationValues = getValues(generator, annotationName, annotationMirror)

            // 将提取出来的属性值列表信息, 去应用到properties当中
            generator.applyToProperties(properties, qualifiedClassName, annotationValues)
            this.properties[qualifiedClassName] = ""
        } catch (ex: Throwable) {
            throw IllegalStateException("Error processing configuration meta-data on $element", ex)
        }
    }

    /**
     * 从给定的Element上去寻找到目标注解
     *
     * @param element 要去进行寻找注解的Element
     * @param annotationType 寻找的注解类型
     * @return 从该元素上找到的目标注解, 如果没有找到return null
     */
    @Nullable
    private fun getAnnotation(element: Element?, annotationType: String): AnnotationMirror? {
        element ?: return null
        for (annotationMirror in element.annotationMirrors) {
            if (annotationMirror.annotationType.toString() == annotationType) {
                return annotationMirror
            }
        }
        return null
    }

    /**
     * 提取给定的注解对应的属性值
     *
     * @param generator Generator
     * @param annotationName 注解名称
     * @param annotationMirror 注解Mirror对象
     */
    private fun getValues(
        generator: PropertyGenerator,
        annotationName: String,
        annotationMirror: AnnotationMirror
    ): List<Any> {
        return generator.getValueExtractor(annotationName)?.getValues(annotationMirror) ?: emptyList()
    }

    /**
     * 将properties写出到本地的"META-INF/spring-autoconfigure-metadata.properties"文件
     *
     * @see properties
     */
    private fun writeProperties() {
        if (this.properties.isEmpty()) {
            return
        }
        val fileObject = this.processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", PROPERTIES_PATH)
        OutputStreamWriter(fileObject.openOutputStream(), StandardCharsets.UTF_8)
            .use {
                properties.forEach { (k, v) ->
                    it.write(k)
                    it.write("=")
                    it.write(v)
                    it.write(System.lineSeparator())
                }
            }
    }

    /**
     * 输出的Metadata数据的属性的生成器, 最终生成的结构是"{className}.{propertyName}",
     * 一个属性值, 可能来自于多个注解, 因此valueExtractors需要设计成为一个列表
     *
     * 例如"com.wanna.App.AutoConfigureAfter=xxx,yyy"这样的属性值来说
     * className="com.wanna.App", propertyName="AutoConfigureAfter", value="xxx,yyy",
     *
     * 这里的propertyName是固定的, value则可能需要从多个注解当中提取出来:
     * * 1.需要从AutoConfigureAfter注解的value/name属性当中提取
     * * 2.需要从AutoConfiguration注解的after/afterName属性当中提取
     *
     * @param annotationPackage 注解所在的包名
     * @param propertyName 生成的最终properties时的属性名
     * @param omitEmptyValues 是否忽略空的属性值(如果为true的话, 那么如果没有提取到属性值, 就不merge到properties当中)
     * @param valueExtractors 注解的属性值的提取器ValueExtractor的Map, Key是注解的全类名, Value是该注解类型对应的ValueExtractor
     */
    private class PropertyGenerator private constructor(
        private val annotationPackage: String,
        private val propertyName: String,
        private val omitEmptyValues: Boolean,
        private val valueExtractors: Map<String, ValueExtractor>
    ) {
        /**
         * 获取所有支持去进行处理的注解列表(本质上就是当前PropertyGenerator, 存储了那些注解的ValueExtractor)
         *
         * @return 所有的支持去进行处理的注解列表
         */
        fun getSupportAnnotations(): Set<String> {
            return valueExtractors.keys
        }

        /**
         * 为当前[PropertyGenerator]对应的propertyName对应的注解去提供对应的[ValueExtractor]
         *
         * @param valueExtractor propertyName对应的注解的属性值的提取器ValueExtractor
         * @return 支持去为propertyName对应的注解去进行属性值的提取的新的PropertyGenerator
         */
        fun withAnnotation(valueExtractor: ValueExtractor): PropertyGenerator {
            return withAnnotation(propertyName, valueExtractor)
        }

        /**
         * 提供对于另外一个注解的解析的[ValueExtractor]
         *
         * @param name 注解的简单类名
         * @param valueExtractor 该注解对应的属性值提取器ValueExtractor
         * @return 支持该注解的属性值的提取的新的PropertyGenerator对象
         */
        fun withAnnotation(name: String, valueExtractor: ValueExtractor): PropertyGenerator {
            val valueExtractors = LinkedHashMap(valueExtractors)

            // 拼接出来注解的类名, 格式为"{annotationPackage}.{name}"
            valueExtractors[this.annotationPackage + "." + name] = valueExtractor

            // 构建新的PropertyGenerator
            return PropertyGenerator(this.annotationPackage, propertyName, omitEmptyValues, valueExtractors)
        }

        /**
         * 将收集到的注解的属性信息, 去应用到properties当中
         *
         * @param properties 最终输出结果的properties
         * @param className 当前正在去进行处理的类
         * @param annotationValues 针对该类为当前PropertyName去收集到的注解属性值列表
         */
        fun applyToProperties(properties: MutableMap<String, String>, className: String, annotationValues: List<Any>) {
            if (omitEmptyValues && annotationValues.isEmpty()) {
                return
            }
            mergeProperties(properties, "$className.$propertyName", toCommaDelimitedString(annotationValues))
        }

        /**
         * 将properties当中key对应的元素去进行值的merge, 如果存在有旧值和新值, 需要使用","拼起来
         *
         * @param properties properties
         * @param key 待进行merge的元素Key
         * @param value key对应的新的value
         */
        private fun mergeProperties(properties: MutableMap<String, String>, key: String, value: String) {
            val existingValue = properties[key]
            if (existingValue.isNullOrBlank()) {
                properties[key] = value
            } else {
                properties[key] = "$existingValue,$value"
            }
        }

        /**
         * 将给定的列表去转换成为使用","分割的字符串
         *
         * @param list 待拼接的对象列表
         * @return 使用","去进行拼接得到的最终字符串
         */
        private fun toCommaDelimitedString(list: List<Any>): String {
            return list.joinToString(",") { it.toString() }
        }

        /**
         * 为给定的注解类型, 去获取到对应的[ValueExtractor]
         *
         * @param annotationName 需要获取ValueExtractor的注解类型
         * @return 处理该注解的属性提取的ValueExtractor(获取不到return null)
         */
        fun getValueExtractor(annotationName: String): ValueExtractor? = valueExtractors[annotationName]

        companion object {

            /**
             * 根据注解的包名和注解的简单类名, 去构建[PropertyGenerator]的工厂方法(omitEmptyValues=false, 就算没有属性值, 那么也需要去写入到properties当中)
             *
             * @param annotationPackage 注解的包名
             * @param propertyName 注解简单类名
             * @return PropertyGenerator
             */
            @JvmStatic
            fun of(annotationPackage: String, propertyName: String): PropertyGenerator {
                return of(annotationPackage, propertyName, false)
            }

            /**
             * 根据注解的包名和注解的简单类名, 去构建[PropertyGenerator]的工厂方法
             *
             * @param annotationPackage 注解的包名
             * @param propertyName 注解简单类名
             * @param omitEmptyValues 如果根据PropertyGenerator, 没有找到对应的注解属性值, 那么是否忽略掉, 不进行properties的写入
             * @return PropertyGenerator
             */
            @JvmStatic
            fun of(annotationPackage: String, propertyName: String, omitEmptyValues: Boolean): PropertyGenerator {
                return PropertyGenerator(annotationPackage, propertyName, omitEmptyValues, emptyMap())
            }
        }

    }

    /**
     * 注解的属性值的提取器Extractor
     */
    fun interface ValueExtractor {

        /**
         * 执行对于给定的注解的属性值的提取
         *
         * @param annotationMirror 注解Mirror对象
         * @return 从给定的注解当中提取到的属性值列表
         */
        fun getValues(annotationMirror: AnnotationMirror): List<Any>

        companion object {
            /**
             * 快速构建: 基于给定属性名列表去进行注解的属性值的获取的[ValueExtractor]
             *
             * @param names 属性名列表
             * @return 以给定的属性名列表去进行属性值的提取的ValueExtractor
             */
            @JvmStatic
            fun allFrom(vararg names: String): ValueExtractor {
                return NamedValuesExtractor(*names)
            }
        }
    }

    /**
     * 抽象的[ValueExtractor]实现, 提供一些工具方法给子类使用
     *
     * @see extractValues
     */
    private abstract class AbstractValueExtractor : ValueExtractor {

        /**
         * 提取给定的[AnnotationValue]当中的属性值
         *
         * @param annotationValue AnnotationValue
         * @return 提取到的属性值列表
         */
        @Suppress("UNCHECKED_CAST")
        protected open fun extractValues(annotationValue: AnnotationValue?): List<Any> {
            annotationValue ?: return emptyList()
            val value = annotationValue.value
            if (value is List<*>) {
                return (value as List<AnnotationValue>).map { extractValue(it.value) }
            }
            return listOf(extractValue(value!!))
        }

        /**
         * 执行对于属性值的提取(如果给定的是一个类的DeclaredType, 也就是注解的属性值当中配置了Class的情况, 那么返回全限定名; 否则返回value即可)
         *
         * @param value value
         * @return 新的value
         */
        private fun extractValue(value: Any): Any {
            if (value is DeclaredType) {
                return Elements.getQualifiedName(value.asElement()!!)!!
            }
            return value
        }
    }

    /**
     * 从给定的注解当中, 根据属性名去进行属性值的提取的[ValueExtractor]实现
     *
     * @param names 需要提前属性值的属性名列表
     */
    private open class NamedValuesExtractor(private val names: Set<String>) : AbstractValueExtractor() {
        constructor(vararg names: String) : this(setOf(*names))

        override fun getValues(annotationMirror: AnnotationMirror): List<Any> {
            return annotationMirror.elementValues
                // 过滤出来指定的属性名的AnnotationValue
                .filter { names.contains(it.key.simpleName.toString()) }
                // 将每个属性名对应的AnnotationValue去转换成为List<Object>
                .map { extractValues(it.value) }
                // 所有的属性名的提取属性值的结果摊开成为一个大的List
                .flatten()
        }
    }

    /**
     * 对于OnBeanCondition属性值的提取, 我们直接提取它的"value"和"type"属性的配置即可
     */
    private class OnBeanConditionValueExtractor : AbstractValueExtractor() {

        private val names = setOf("value", "type")
        override fun getValues(annotationMirror: AnnotationMirror): List<Any> {
            val attributes = annotationMirror.elementValues.map { it.key.simpleName.toString() to it.value }.toMap()
            if (attributes.containsKey("name")) {
                return emptyList()
            }
            return attributes.filter { names.contains(it.key) }.map { extractValues(it.value) }
        }
    }

    /**
     * 对于OnClassCondition属性值的提取, 我们直接提取它的"name"和"value"属性的配置即可
     */
    private class OnClassConditionValueExtractor : NamedValuesExtractor("name", "value") {
        override fun getValues(annotationMirror: AnnotationMirror): List<Any> {
            val values = super.getValues(annotationMirror)
            return values.sortedWith { o1, o2 ->
                String.CASE_INSENSITIVE_ORDER.compare(
                    o1.toString(),
                    o2.toString()
                )
            }
        }
    }
}
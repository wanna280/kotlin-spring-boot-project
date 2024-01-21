package com.wanna.boot.autoconfigureprocessor

import javax.annotation.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * [Element]的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2024/1/21
 */
internal object Elements {

    /**
     * 返回给定的类的全限定名
     *
     * @param element Element
     * @return 如果给定的Element是一个类, 那么返回给定的类的全限定名; 如果给定的Element不是一个类, 那么return null
     */
    @JvmStatic
    fun getQualifiedName(element: Element?): String? {
        element ?: return null
        val enclosingClass = getEnclosingTypeElement(element.asType())
        if (enclosingClass != null) {
            return getQualifiedName(enclosingClass) + "$" + element.simpleName.toString()
        }
        if (element is TypeElement) {
            return element.qualifiedName.toString()
        }
        return null
    }

    /**
     * 获取给定的类对应的外部类
     *
     * @param type type
     * @return 如果给定的type是一个类(或者接口), 并且存在有外部类, 那么return外部类; 否则return null
     */
    @JvmStatic
    @Nullable
    private fun getEnclosingTypeElement(type: TypeMirror?): TypeElement? {
        if (type is DeclaredType) {
            val enclosingElement = type.asElement().enclosingElement

            // 只有在外部元素是TypeElement, 也就是一个类的情况下, 才返回...
            if (enclosingElement is TypeElement) {
                return enclosingElement
            }
        }
        return null
    }
}
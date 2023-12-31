package com.wanna.boot.loader.jar

import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.security.CodeSigner
import java.security.cert.Certificate
import java.util.jar.Attributes
import java.util.jar.JarEntry
import javax.annotation.Nullable

/**
 * [JarEntry]的变体, 供SpringBootLoader内部的[JarFile]去进行使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class JarEntry(jarFile: JarFile, val index: Int, header: CentralDirectoryFileHeader, @Nullable nameAlias: AsciiBytes?) :
    JarEntry(nameAlias?.toString() ?: header.getName().toString()), FileHeader {
    val asciiBytesName: AsciiBytes
    private val headerName: AsciiBytes?
    private val jarFile: JarFile
    private val localHeaderOffset: Long

    init {
        asciiBytesName = nameAlias ?: header.getName()!!
        headerName = header.getName()
        this.jarFile = jarFile
        localHeaderOffset = header.getLocalHeaderOffset()
        compressedSize = header.getCompressedSize()
        method = header.getMethod()
        crc = header.getCrc()
        comment = header.getComment().toString()
        size = header.getSize()
        //		setTime(header.getTime());
        if (header.hasExtra()) {
            extra = header.getExtra()
        }
    }

    @get:Throws(MalformedURLException::class)
    val url: URL
        get() = URL(jarFile.getUrl(), name)

    @Volatile
    private var certification: JarEntryCertification? = null
        get() {
            if (!jarFile.isSigned) {
                return JarEntryCertification.NONE
            }
            var certification = field
            if (certification == null) {
                certification = jarFile.getCertification(this)
                field = certification
            }
            return certification
        }

    override fun hasName(name: CharSequence, suffix: Char) = headerName!!.matches(name, suffix)

    @Nullable
    @Throws(IOException::class)
    override fun getAttributes(): Attributes? = jarFile.manifest?.getAttributes(name)

    override fun getCertificates(): Array<Certificate> = certification!!.getCertificates()!!

    override fun getCodeSigners(): Array<CodeSigner> = certification!!.getCodeSigners()!!

    override fun getLocalHeaderOffset() = localHeaderOffset
}
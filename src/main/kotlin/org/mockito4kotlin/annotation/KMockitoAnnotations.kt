/*
 * The MIT License
 *
 *   Copyright (c) 2017-2018 Wilhelm Schulenburg
 *   Copyright (c) 2007 Mockito contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.mockito4kotlin.annotation

import org.mockito.*
import org.mockito4kotlin.annotation.engine.AnnotationEngineFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Mark a field, property or property's backing field as a mock.
 *
 * * Makes code easier to read.
 * * Separate mock initialization from test code.
 * * Works like @[org.mockito.Mock] annotation of Mockito.
 * * With [KMock] you are able to use Mockitos Annotation by data classes and classes with delegates.
 *
 * See examples in [org.mockito.Mock]
 *
 * @see org.mockito.Mock
 * @see org.mockito.Mockito.mock
 * @see org.mockito.MockSettings
 * @see org.mockito.Spy
 * @see org.mockito.InjectMocks
 * @see org.mockito.MockitoAnnotations.initMocks
 *
 * @property extraInterfaces Extra interfaces the mock should implement.
 * @property stubOnly Does not record method invocation -> disallowing verification of invocations
 * @property name Name of this mock.
 * @property answer Default answers to interactions.
 * @property serializable Mock is serializable.
 *
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@MustBeDocumented
annotation class KMock(val extraInterfaces: Array<KClass<out Any>> = [],
                       val stubOnly: Boolean = false,
                       val name: String = "",
                       val answer: Answers = Answers.RETURNS_DEFAULTS,
                       val serializable: Boolean = false)

/**
 * Allows shorthand [com.nhaarman.mockito_kotlin.KArgumentCaptor] creation on fields,
 * properties or property's backing fields.
 *
 * * Makes code easier to read.
 * * Separate initialization of KArgumentCaptor from test code.
 * * Works like @[org.mockito.Captor] annotation of Mockito.
 *
 * See examples in [org.mockito.Captor]
 *
 * @see org.mockito.Captor
 * @see org.mockito.ArgumentCaptor
 * @see com.nhaarman.mockitokotlin2.KArgumentCaptor
 * @see org.mockito.Mock
 * @see org.mockito.Mockito.spy
 * @see org.mockito.InjectMocks
 * @see org.mockito.MockitoAnnotations.initMocks
 *
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@MustBeDocumented
annotation class KCaptor


/**
 * KMockitoAnnotations.initMocks(this) initializes fields, property or property's backing field
 * of the given instance annotated with mock annotations like [Mock], [KMock], [Spy], [Captor], [KCaptor] and [InjectMocks].
 *
 * * Makes code easier to read.
 * * Separate initialization of ArgumentCaptor from test code.
 * * Works like [org.mockito.MockitoAnnotations] of Mockito.
 *
 * See examples in [org.mockito.MockitoAnnotations]
 *
 * @see org.mockito.MockitoAnnotations.initMocks
 * @see org.mockito.Mockito.mock
 * @see org.mockito.Mock
 * @see org.mockito.Spy
 * @see org.mockito.Captor
 * @see org.mockito.InjectMocks
 */
class KMockitoAnnotations {
    companion object {
        private val mockedPropertiesAssigned = MockPropertyCollector()

        /**
         * Initializes objects annotated with [Mock], [KMock], [Spy], [Captor], [KCaptor] and [InjectMocks].
         *
         * @param anyWithMocks instance with Mockannotations to be initialized.
         *
         */
        fun initMocks(anyWithMocks: Any) {
            mockedPropertiesAssigned.reset()
            val properties = anyWithMocks::class.memberProperties

            processAnnotations(anyWithMocks, properties, ::isMockito4KotlinAnnotation)
            registerInlinedMockDeclaration(anyWithMocks, properties)
            processAnnotations(anyWithMocks, properties) { it is InjectMocks }
        }

        private fun processAnnotations(anyWithMocks: Any, properties: Collection<KProperty<*>>, predicate: Predicate<Annotation>) {
            properties.filter { it.allAnnotations().any(predicate) }
                .forEach { property ->
                    AnnotationEngineFactory.create(asAnnotation(property, predicate)).apply {
                        inject(this@Companion.mockedPropertiesAssigned)
                        process(anyWithMocks, property)
                    }
                }
        }

        private fun registerInlinedMockDeclaration(anyWithMocks: Any, properties: Collection<KProperty<*>>) {

            fun isValueMocked(value: Any): Boolean {
                val mockingDetails = Mockito.mockingDetails(value)
                return mockingDetails.isMock || mockingDetails.isSpy
            }

            properties.forEach { property ->
                property.isAccessible = true
                property.getter.call(anyWithMocks)?.let { value ->
                    if (isValueMocked(value)) {
                        mockedPropertiesAssigned.register(property, value)
                    }
                }
            }
        }

        private fun asAnnotation(property: KProperty<*>, predicate: Predicate<Annotation>) =
            property.allAnnotations().find(predicate)

    }
}

@Deprecated("This class is deprecated. Use 'KMockitoAnnotations' instead.",
    replaceWith = ReplaceWith("KMockitoAnnotations")
)
class MockAnnotations {
    companion object {
        @Deprecated("This function is deprecated. Use 'KMockitoAnnotations.initMocks(Any)' instead.",
            replaceWith = ReplaceWith("KMockitoAnnotations.initMocks(anyWithMocks)"),
            level = DeprecationLevel.ERROR
        )
        fun initMocks(anyWithMocks: Any) {
            KMockitoAnnotations.initMocks(anyWithMocks)
        }
    }
}

/**
 * Returns true if the given annotation is a Mockito4Kotlin annotation like:
 * [Mock], [KMock], [Spy], [Captor] and [KCaptor].
 *
 * @param it given annotation
 */
internal fun isMockito4KotlinAnnotation(it: Annotation) = it is Mock || it is Spy || it is Captor || it is KCaptor || it is KMock

internal typealias Predicate<T> = (T) -> Boolean

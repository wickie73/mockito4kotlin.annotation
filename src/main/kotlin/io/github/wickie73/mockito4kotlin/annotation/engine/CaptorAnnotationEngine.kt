/*
 *
 * The MIT License
 *
 *   Copyright (c) 2017-2021 Wilhelm Schulenburg
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
 *
 */

package io.github.wickie73.mockito4kotlin.annotation.engine

import org.mockito.ArgumentCaptor
import io.github.wickie73.mockito4kotlin.annotation.engine.MockAnnotationsChecker.checkImmutableProperties
import io.github.wickie73.mockito4kotlin.annotation.engine.MockAnnotationsChecker.checkIsArgumentCaptor
import io.github.wickie73.mockito4kotlin.annotation.engine.MockAnnotationsChecker.checkNumberOfMockAnnotations
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.isAccessible

internal class CaptorAnnotationEngine : AbstractAnnotationEngine() {

    override fun process(anyInstanceWithMocks: Any, property: KProperty<*>) {
        property.isAccessible = true
        checkImmutableProperties(property)
        checkNumberOfMockAnnotations(property)
        checkIsArgumentCaptor(property)

        (property as KMutableProperty<*>).setter.call(anyInstanceWithMocks, createArgumentCaptor(property))
    }

    private fun createArgumentCaptor(property: KProperty<*>): Any =
        ArgumentCaptor.forClass(genericClassOf(property.returnType).java)

    private fun genericClassOf(propertyReturnType: KType): KClass<out Any> =
        propertyReturnType.arguments.firstOrNull()?.type?.classifier as? KClass<out Any> ?: Any::class
}
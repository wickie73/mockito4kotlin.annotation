/*
 *
 * The MIT License
 *
 *   Copyright (c) 2017 Wilhelm Schulenburg
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

package io.github.wickie73.mockito4kotlin.annotation.mockito

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Spy
import org.mockito.internal.util.MockUtil.isSpy
import io.github.wickie73.mockito4kotlin.annotation.KMockitoAnnotations
import org.junit.jupiter.api.AfterEach

/**
 * This test class is originated from Mockito's [org.mockitousage.annotation.SpyAnnotationInitializedInBaseClassTest] and
 * ensures that [KMockitoAnnotations] is compatible with Mockito Annotations like
 * * @[org.mockito.Mock]
 * * @[org.mockito.Spy]
 * * @[org.mockito.Captor]
 * * @[org.mockito.InjectMocks]
 */
open class SpyAnnotationInitializedInBaseClassTest {

    internal open inner class BaseClass {

        @Spy
        var list = mutableListOf<Any>()
    }

    internal inner class SubClass : BaseClass()

    private lateinit var testCloseable: AutoCloseable

    @AfterEach
    fun releaseMocks() {
        if (this::testCloseable.isInitialized) {
            testCloseable.close()
        }
    }

    @Test
    @DisplayName("Spy in BaseClass should be initialized")
    fun shouldInitSpiesInBaseClass() {
        //given
        val subClass = SubClass()
        //when
        testCloseable = KMockitoAnnotations.openMocks(subClass)
        //then
        assertTrue(isSpy(subClass.list))
    }

    @Test
    @DisplayName("Spies in nested class should be initialized")
    fun shouldInitSpiesInHierarchyInNestedClass() {
        val subTest = SubTest()

        testCloseable = KMockitoAnnotations.openMocks(subTest)

        subTest.shouldInitSpiesInHierarchy()
    }

    @BeforeEach
    internal fun setUp() {
        testCloseable = KMockitoAnnotations.openMocks(this)
    }

    @Spy
    internal var spyInBaseclass = mutableListOf<Any>()

    class SubTest : SpyAnnotationInitializedInBaseClassTest() {

        @Spy
        private var spyInSubclass = mutableListOf<Any>()

        @Test
        fun shouldInitSpiesInHierarchy() {
            assertTrue(isSpy(spyInSubclass))
            assertTrue(isSpy(spyInBaseclass))
        }
    }

}

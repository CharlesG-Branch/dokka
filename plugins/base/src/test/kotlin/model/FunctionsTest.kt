package model

import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.Function
import org.junit.Test
import utils.*

class FunctionTest : AbstractModelTest("/src/main/kotlin/function/Test.kt", "function") {

    @Test
    fun function() {
        inlineModelTest(
            """
            |/**
            | * Function fn
            | */
            |fun fn() {}
        """
        ) {
            with((this / "function" / "fn").cast<Function>()) {
                name equals "fn"
                type.constructorFqName equals "kotlin.Unit"
                this.children.assertCount(0, "Function children: ")
            }
        }
    }

    @Test
    fun overloads() {
        inlineModelTest(
            """
            |/**
            | * Function fn
            | */
            |fun fn() {}
            | /**
            | * Function fn(Int)
            | */
            |fun fn(i: Int) {}
        """
        ) {
            with((this / "function").cast<Package>()) {
                val fn1 = functions.find {
                    it.name == "fn" && it.parameters.isNullOrEmpty()
                }.assertNotNull("fn()")
                val fn2 = functions.find {
                    it.name == "fn" && it.parameters.isNotEmpty()
                }.assertNotNull("fn(Int)")

                with(fn1) {
                    name equals "fn"
                    parameters.assertCount(0)
                }

                with(fn2) {
                    name equals "fn"
                    parameters.assertCount(1)
                    parameters.first().type.constructorFqName equals "kotlin.Int"
                }
            }
        }
    }

    @Test
    fun functionWithReceiver() {
        inlineModelTest(
            """
            |/**
            | * Function with receiver
            | */
            |fun String.fn() {}
            |
            |/**
            | * Function with receiver
            | */
            |fun String.fn(x: Int) {}
        """
        ) {
            with((this / "function").cast<Package>()) {
                val fn1 = functions.find {
                    it.name == "fn" && it.parameters.isNullOrEmpty()
                }.assertNotNull("fn()")
                val fn2 = functions.find {
                    it.name == "fn" && it.parameters.count() == 1
                }.assertNotNull("fn(Int)")

                with(fn1) {
                    name equals "fn"
                    parameters counts 0
                    receiver.assertNotNull("fn() receiver")
                }

                with(fn2) {
                    name equals "fn"
                    parameters counts 1
                    receiver.assertNotNull("fn(Int) receiver")
                    parameters.first().type.constructorFqName equals "kotlin.Int"
                }
            }
        }
    }

    @Test
    fun functionWithParams() {
        inlineModelTest(
            """
                |/**
                | * Multiline
                | *
                | * Function
                | * Documentation
                | */
                |fun function(/** parameter */ x: Int) {
                |}
        """
        ) {
            with((this / "function" / "function").cast<Function>()) {
                comments() equals "Multiline\nFunction Documentation"

                name equals "function"
                parameters counts 1
                parameters.firstOrNull().assertNotNull("Parameter: ").also {
                    it.name equals "x"
                    it.type.constructorFqName equals "kotlin.Int"
                    it.comments() equals "parameter"
                }

                type.assertNotNull("Return type: ").constructorFqName equals "kotlin.Unit"
            }
        }
    }

// TODO add modifiers - start

    @Test
    fun functionWithNotDocumentedAnnotation() {
        inlineModelTest(
            """
                |@Suppress("FOO") fun f() {}
        """
        ) {
            // TODO add annotations

            with((this / "function" / "f").cast<Function>()) {
                assert(false) { "No annotation data" }
            }
        }
    }

//    @Test fun functionWithNotDocumentedAnnotation() {
//        verifyPackageMember("testdata/functions/functionWithNotDocumentedAnnotation.kt", defaultModelConfig) { func ->
//            assertEquals(0, func.annotations.count())
//        }
//    }

    @Test
    fun inlineFunction() {
        inlineModelTest(
            """
                |inline fun f(a: () -> String) {}
        """
        ) {
            // TODO add data about inline

            with((this / "function" / "f").cast<Function>()) {
                with(extra[AdditionalModifiers.AdditionalKey].assertNotNull("AdditionalModifiers")) {
                    content.find { it == ExtraModifiers.INLINE }.assertNotNull("inline modifier")
                }
            }
        }
    }

    @Test
    fun suspendFunction() {
        inlineModelTest(
            """
                |suspend fun f() {}
        """
        ) {
            with((this / "function" / "f").cast<Function>()) {
                with(extra[AdditionalModifiers.AdditionalKey].assertNotNull("AdditionalModifiers")) {
                    content.find { it == ExtraModifiers.SUSPEND }.assertNotNull("suspend modifier")
                }
            }
        }
    }

    @Test
    fun suspendInlineFunctionOrder() {
        inlineModelTest(
            """
                |suspend inline fun f(a: () -> String) {}
        """
        ) {
            with((this / "function" / "f").cast<Function>()) {
                with(extra[AdditionalModifiers.AdditionalKey].assertNotNull("AdditionalModifiers")) {
                    content.find { it == ExtraModifiers.SUSPEND }.assertNotNull("suspend modifier")
                    content.find { it == ExtraModifiers.INLINE }.assertNotNull("inline modifier")
                }
            }
        }
    }
//
//    @Test fun functionWithAnnotatedParam() {
//        checkSourceExistsAndVerifyModel("testdata/functions/functionWithAnnotatedParam.kt", defaultModelConfig) { model ->
//            with(model.members.single().members.single { it.name == "function" }) {
//                with(details(NodeKind.Parameter).first()) {
//                    assertEquals(1, annotations.count())
//                    with(annotations[0]) {
//                        assertEquals("Fancy", name)
//                        assertEquals(Content.Empty, content)
//                        assertEquals(NodeKind.Annotation, kind)
//                    }
//                }
//            }
//        }
//    }

    @Test
    fun functionWithNoinlineParam() {
        inlineModelTest(
            """
                |fun f(noinline notInlined: () -> Unit) {}
        """
        ) {
            with((this / "function" / "f" / "notInlined").cast<Parameter>()) {
                with(extra[AdditionalModifiers.AdditionalKey].assertNotNull("AdditionalModifiers")) {
                    content.find { it == ExtraModifiers.NOINLINE }.assertNotNull("noinline modifier")
                }
            }
        }
    }
//
//    @Test fun annotatedFunctionWithAnnotationParameters() {
//        checkSourceExistsAndVerifyModel(
//            "testdata/functions/annotatedFunctionWithAnnotationParameters.kt",
//            defaultModelConfig
//        ) { model ->
//            with(model.members.single().members.single { it.name == "f" }) {
//                assertEquals(1, annotations.count())
//                with(annotations[0]) {
//                    assertEquals("Fancy", name)
//                    assertEquals(Content.Empty, content)
//                    assertEquals(NodeKind.Annotation, kind)
//                    assertEquals(1, details.count())
//                    with(details[0]) {
//                        assertEquals(NodeKind.Parameter, kind)
//                        assertEquals(1, details.count())
//                        with(details[0]) {
//                            assertEquals(NodeKind.Value, kind)
//                            assertEquals("1", name)
//                        }
//                    }
//                }
//            }
//        }
//    }

// TODO add modifiers - end

    @Test
    fun functionWithDefaultParameter() {
        inlineModelTest(
            """
                |/src/main/kotlin/function/Test.kt
                |package function
                |fun f(x: String = "") {}
        """
        ) {
            // TODO add default value data

            with((this / "function" / "f").cast<Function>()) {
                parameters.forEach { p ->
                    p.name equals "x"
                    p.type.constructorFqName.assertNotNull("Parameter type: ") equals "kotlin.String"
                    assert(false) { "Add default value data" }
                }
            }
        }
    }

//    @Test fun functionWithDefaultParameter() {
//        checkSourceExistsAndVerifyModel("testdata/functions/functionWithDefaultParameter.kt", defaultModelConfig) { model ->
//            with(model.members.single().members.single()) {
//                with(details.elementAt(3)) {
//                    val value = details(NodeKind.Value)
//                    assertEquals(1, value.count())
//                    with(value[0]) {
//                        assertEquals("\"\"", name)
//                    }
//                }
//            }
//        }
//    }
//
//    @Test fun sinceKotlin() {
//        checkSourceExistsAndVerifyModel("testdata/functions/sinceKotlin.kt", defaultModelConfig) { model ->
//            with(model.members.single().members.single()) {
//                assertEquals("1.1", sinceKotlin)
//            }
//        }
//    }
//}

}
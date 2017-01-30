package us.shiroyama.android.shoebill.processor;

import android.annotation.TargetApi;
import android.support.annotation.VisibleForTesting;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import us.shiroyama.android.shoebill.annotations.WrapStatic;

/**
 * Annotation Processor
 *
 * @author Fumihiko Shiroyama
 */

@AutoService(Processor.class)
public class ShoebillProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(WrapStatic.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @TargetApi(24)
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            return true;
        }

        TypeElement originalClass = roundEnvironment.getElementsAnnotatedWith(WrapStatic.class).stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .map(element -> (TypeElement) element)
                .findFirst()
                .get();

        List<ExecutableElement> staticMethods = originalClass.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(element -> (ExecutableElement) element)
                .filter(executableElement -> executableElement.getModifiers().stream().anyMatch(modifier -> modifier == Modifier.STATIC))
                .filter(executableElement -> executableElement.getModifiers().stream().noneMatch(modifier -> modifier == Modifier.PRIVATE))
                .collect(Collectors.toList());

        List<MethodSpec> proxyMethods = staticMethods.stream()
                .map((method) -> getProxyMethod(originalClass, method))
                .collect(Collectors.toList());

        String targetPackage = elementUtils.getPackageOf(originalClass).getQualifiedName().toString();
        String targetClassName = getTargetClassName(originalClass);
        ClassName className = ClassName.bestGuess(String.format("%s.%s", targetPackage, targetClassName));
        FieldSpec singletonField = getSingletonField(className);

        TypeSpec targetTypeSpec = TypeSpec
                .classBuilder(targetClassName)
                .addModifiers(getTargetClassModifiers(originalClass))
                .addField(singletonField)
                .addMethod(getSingletonGetterMethod(className, singletonField))
                .addMethod(getSingletonSetterMethod(className, singletonField))
                .addMethods(proxyMethods)
                .build();

        JavaFile javaFile = JavaFile
                .builder(targetPackage, targetTypeSpec)
                .addFileComment("This is auto-generated code. Do not modify this directly.")
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return true;
    }

    private String getTargetClassName(TypeElement originalClass) {
        return originalClass.getSimpleName() + "Wrapper";
    }

    @TargetApi(24)
    private Modifier[] getTargetClassModifiers(TypeElement originalClass) {
        List<Modifier> modifiers = originalClass.getModifiers()
                .stream()
                .filter(modifier -> modifier != Modifier.PRIVATE)
                .filter(modifier -> modifier != Modifier.STATIC)
                .collect(Collectors.toList());
        modifiers.addAll(Collections.singletonList(Modifier.FINAL));

        return modifiers.toArray(new Modifier[modifiers.size()]);
    }

    private FieldSpec getSingletonField(ClassName className) {
        return FieldSpec
                .builder(className, "singleton")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer("new $L()", className.simpleName())
                .build();
    }

    private MethodSpec getSingletonGetterMethod(ClassName className, FieldSpec singletonField) {
        return MethodSpec
                .methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(className)
                .addStatement("return $N", singletonField)
                .build();
    }

    private MethodSpec getSingletonSetterMethod(ClassName className, FieldSpec singletonField) {
        ParameterSpec parameter = ParameterSpec
                .builder(className, "wrapper")
                .build();

        AnnotationSpec visibleForTesting = AnnotationSpec
                .builder(VisibleForTesting.class)
                .addMember("otherwise", "VisibleForTesting.NONE")
                .build();

        return MethodSpec
                .methodBuilder("setInstance")
                .addAnnotation(visibleForTesting)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(parameter)
                .addStatement("$N = $N", singletonField, parameter)
                .build();
    }

    @TargetApi(24)
    private MethodSpec getProxyMethod(TypeElement originalClass, ExecutableElement method) {
        List<Modifier> modifiers = method.getModifiers()
                .stream()
                .filter(modifier -> modifier != Modifier.PRIVATE)
                .filter(modifier -> modifier != Modifier.STATIC)
                .collect(Collectors.toList());
        modifiers.addAll(Collections.singletonList(Modifier.FINAL));

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder(method.getSimpleName().toString())
                .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]))
                .returns(TypeName.get(method.getReturnType()));

        List<ParameterSpec> parameterSpecs = method.getParameters()
                .stream()
                .map(ParameterSpec::get)
                .collect(Collectors.toList());

        if (!parameterSpecs.isEmpty()) {
            builder.addParameters(parameterSpecs);
        }

        StringBuilder statementBuilder = new StringBuilder();

        if (method.getReturnType().getKind() != TypeKind.VOID) {
            statementBuilder.append("return ");
        }

        statementBuilder.append("$L.$L($L)");
        String args = String.join(",", parameterSpecs.stream().map(parameterSpec -> parameterSpec.name).collect(Collectors.toList()));
        builder.addStatement(statementBuilder.toString(), originalClass.getSimpleName().toString(), method.getSimpleName().toString(), args);

        return builder.build();
    }

}

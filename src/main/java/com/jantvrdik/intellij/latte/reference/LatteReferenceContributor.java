package com.jantvrdik.intellij.latte.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.reference.references.*;
import org.jetbrains.annotations.NotNull;

public class LatteReferenceContributor extends PsiReferenceContributor {
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_VARIABLE),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpVariable)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpVariable) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{
                                    new LattePhpVariableReference((LattePhpVariable) element, new TextRange(0, value.getTextLength()))
                            };
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_METHOD),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpMethod)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpMethod) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{
                                    new LattePhpMethodReference((LattePhpMethod) element, new TextRange(0, value.getTextLength()))
                            };
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_PROPERTY),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpProperty)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpProperty) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{
                                    new LattePhpPropertyReference((LattePhpProperty) element, new TextRange(0, value.getTextLength()))
                            };
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_CONSTANT),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpConstant)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpConstant) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{
                                    new LattePhpConstantReference((LattePhpConstant) element, new TextRange(0, value.getTextLength()))
                            };
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_STATIC_VARIABLE),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpStaticVariable)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpStaticVariable) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{
                                    new LattePhpStaticVariableReference((LattePhpStaticVariable) element, new TextRange(0, value.getTextLength()))
                            };
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(LatteTypes.PHP_CLASS),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LattePhpClass)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiElement value = ((LattePhpClass) element).getTextElement();
                        if (value != null && value.getTextLength() > 0) {
                            return new PsiReference[]{new LattePhpClassReference((LattePhpClass) element, new TextRange(0, value.getTextLength()))};
                        }

                        return PsiReference.EMPTY_ARRAY;
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.or(
                        PlatformPatterns.psiElement(LatteTypes.MACRO_OPEN_TAG),
                        PlatformPatterns.psiElement(LatteTypes.MACRO_CLOSE_TAG)
                ),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LatteMacroTag)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        int valueLength = ((LatteMacroTag) element).getMacroNameLength();
                        if (valueLength == 0) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        int length = element instanceof LatteMacroCloseTag ? 2 : 1;
                        return new PsiReference[]{new LatteMacroTagReference((LatteMacroTag) element, new TextRange(1, valueLength + length))};
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.or(
                        PlatformPatterns.psiElement(LatteTypes.MACRO_MODIFIER)
                ),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        if (!(element instanceof LatteMacroModifier)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        LatteMacroModifier constantElement = (LatteMacroModifier) element;
                        PsiElement textElement = ((LatteMacroModifier) element).getTextElement();
                        if (textElement != null && textElement.getTextLength() > 0) {
                            return new PsiReference[]{new LatteMacroModifierReference(constantElement, new TextRange(0, textElement.getTextLength()))};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }
}

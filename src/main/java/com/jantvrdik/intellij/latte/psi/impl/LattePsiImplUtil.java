package com.jantvrdik.intellij.latte.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jantvrdik.intellij.latte.config.LatteConfiguration;
import com.jantvrdik.intellij.latte.settings.LatteCustomFunctionSettings;
import com.jantvrdik.intellij.latte.settings.LatteVariableSettings;
import com.jantvrdik.intellij.latte.psi.*;
import com.jantvrdik.intellij.latte.utils.LattePhpType;
import com.jantvrdik.intellij.latte.utils.LattePhpUtil;
import com.jantvrdik.intellij.latte.utils.LatteUtil;
import com.jantvrdik.intellij.latte.utils.PsiPositionedElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

public class LattePsiImplUtil {
	@NotNull
	public static String getMacroName(LatteMacroTag element) {
		ASTNode nameNode = getMacroNameNode(element);
		if (nameNode != null) {
			return nameNode.getText();
		}
		return createMacroName(element);
	}

	public static boolean matchMacroName(LatteMacroTag element, @NotNull String name) {
		ASTNode nameNode = getMacroNameNode(element);
		if (nameNode == null) {
			return createMacroName(element).equals(name);
		}
		return matchPsiElement(nameNode, name);
	}

	public static int getMacroNameLength(LatteMacroTag element) {
		ASTNode nameNode = getMacroNameNode(element);
		if (nameNode != null) {
			return nameNode.getTextLength();
		}
		return createMacroName(element).length();
	}

	private static boolean matchPsiElement(ASTNode element, @NotNull String text) {
		return element.getTextLength() == text.length() && element.getText().equals(text);
	}

	@Nullable
	private static ASTNode getMacroNameNode(LatteMacroTag element) {
		ASTNode elementNode = element.getNode();
		ASTNode nameNode = elementNode.findChildByType(T_MACRO_NAME);
		if (nameNode != null) {
			return nameNode;
		}
		return elementNode.findChildByType(T_MACRO_SHORTNAME);
	}

	@NotNull
	private static String createMacroName(LatteMacroTag element) {
		LatteMacroContent content = element.getMacroContent();
		if (content == null || element instanceof LatteMacroCloseTag) {
			return "";
		}
		return "=";
	}

	@Nullable
	public static LattePhpContent getFirstPhpContent(@NotNull LatteMacroContent macroContent) {
		List<LattePhpContent> phpContents = macroContent.getPhpContentList();
		return phpContents.stream().findFirst().isPresent() ? phpContents.stream().findFirst().get() : null;
	}

	public static String getVariableName(@NotNull LattePhpVariable element) {
		PsiElement found = getTextElement(element);
		return found != null ? LattePhpUtil.normalizePhpVariable(found.getText()) : null;
	}

	public static String getVariableName(@NotNull LattePhpStaticVariable element) {
		PsiElement found = getTextElement(element);
		return found != null ? LattePhpUtil.normalizePhpVariable(found.getText()) : null;
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull LattePhpStaticVariable element) {
		return findFirstChildWithType(element, T_MACRO_ARGS_VAR);
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull LattePhpVariable element) {
		return findFirstChildWithType(element, T_MACRO_ARGS_VAR);
	}

	public static String getConstantName(@NotNull LattePhpConstant element) {
		PsiElement found = getTextElement(element);
		return found != null ? found.getText() : null;
	}

	public static String getMethodName(@NotNull LattePhpMethod element) {
		PsiElement found = findFirstChildWithType(element, T_PHP_METHOD);
		return found != null ? found.getText() : null;
	}

	public static String getPropertyName(@NotNull LattePhpProperty element) {
		PsiElement found = getTextElement(element);
		return found != null ? found.getText() : null;
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull LattePhpMethod element) {
		return findFirstChildWithType(element, T_PHP_METHOD);
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull LattePhpClass element) {
		return findFirstChildWithType(element, T_PHP_CLASS_NAME);
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull LatteMacroModifier element) {
		return findFirstChildWithType(element, T_MACRO_FILTERS);
	}

	@Nullable
	public static PsiElement getTextElement(@NotNull PsiElement element) {
		return findFirstChildWithType(element, T_PHP_IDENTIFIER);
	}

	public static String getClassName(@NotNull LattePhpClass element) {
		PsiElement found = getTextElement(element);
		return found != null ? LattePhpUtil.normalizeClassName(found.getText()) : null;
	}

	public static String getModifierName(@NotNull LatteMacroModifier element) {
		PsiElement found = getTextElement(element);
		return found != null ? LatteUtil.normalizeMacroModifier(found.getText()) : null;
	}

	public static boolean isVariableModifier(@NotNull LatteMacroModifier element) {
		LattePhpInBrackets variableModifier = PsiTreeUtil.getParentOfType(element, LattePhpInBrackets.class);
		return variableModifier != null;
	}

	@Nullable
	public static LattePhpType detectVariableTypeFromTemplateType(@NotNull PsiElement element, @NotNull String variableName)
	{
		if (!(element.getContainingFile() instanceof LatteFile)) {
			return null;
		}

		LattePhpType templateType = LatteUtil.findFirstLatteTemplateType((LatteFile) element.getContainingFile());
		if (templateType == null) {
			return null;
		}

		Collection<PhpClass> classes = templateType.getPhpClasses(element.getProject());
		if (classes == null) {
			return null;
		}
		for (PhpClass phpClass : classes) {
			for (Field field : phpClass.getFields()) {
				if (!field.isConstant() && field.getModifier().isPublic() && variableName.equals(field.getName())) {
					return new LattePhpType(field.getName(), field.getType().toString(), LattePhpUtil.isNullable(field.getType()));
				}
			}
		}
		return null;
	}

	private static LattePhpType detectVariableType(@NotNull PsiElement element, @NotNull String variableName)
	{
		List<PsiPositionedElement> all = LatteUtil.findVariablesInFileBeforeElement(element, element.getContainingFile().getOriginalFile().getVirtualFile(), variableName);
		List<PsiPositionedElement> definitions = all.stream().filter(
				psiPositionedElement -> psiPositionedElement.getElement() instanceof LattePhpVariable
						&& ((LattePhpVariable) psiPositionedElement.getElement()).isDefinition()
		).collect(Collectors.toList());

		for (PsiPositionedElement positionedElement : definitions) {
			if (!(positionedElement.getElement() instanceof LattePhpVariable)) {
				continue;
			}

			PsiElement current = positionedElement.getElement();
			if (isVarTypeDefinition((LattePhpVariable) current) || isVarDefinition((LattePhpVariable) current)) {
				String prevPhpType = findPrevPhpType(positionedElement.getElement());
				return new LattePhpType(prevPhpType.length() == 0 ? "mixed" : prevPhpType);
			}
		}

		LattePhpType templateType = detectVariableTypeFromTemplateType(element, variableName);
		if (templateType != null) {
			return templateType;
		}

		LatteVariableSettings defaultVariable = LatteConfiguration.INSTANCE.getVariable(element.getProject(), variableName);
		if (defaultVariable != null) {
			return defaultVariable.toPhpType();
		}

		return new LattePhpType("mixed", false);
	}

	private static String findPrevPhpType(PsiElement element)
	{
		return findPrevPhpType(element, "");
	}

	private static String findPrevPhpType(PsiElement element, String phpType)
	{
		PsiElement prevElement = PsiTreeUtil.prevLeaf(element, true);
		if (prevElement == null || prevElement.getNode().getElementType() == T_MACRO_NAME) {
			return phpType;
		}

		String text = prevElement.getText();
		if (text.trim().length() == 0) {
			return findPrevPhpType(prevElement, phpType);
		}

		return findPrevPhpType(prevElement, text + phpType);
	}

	public static @NotNull LattePhpType getPhpType(@NotNull PsiElement element) {
		PsiElement prev = PsiTreeUtil.skipWhitespacesBackward(element);
		if (prev == null || (prev.getNode().getElementType() != T_PHP_DOUBLE_COLON && prev.getNode().getElementType() != T_PHP_OBJECT_OPERATOR)) {
			if (element instanceof LattePhpVariable) {
				return detectVariableType(element, ((LattePhpVariable) element).getVariableName());
			}
			return new LattePhpType("mixed", false);
		}

		PsiElement prevElement;
		if (prev.getParent().getNode().getElementType() == PHP_FOREACH) {
			prevElement = PsiTreeUtil.skipWhitespacesBackward(prev.getParent());
		} else {
			prevElement = PsiTreeUtil.skipWhitespacesBackward(prev);
		}

		if (prevElement != null && prevElement.getText().equals(")")) {
			PsiElement beforeBraces = PsiTreeUtil.skipWhitespacesBackward(prevElement);
			if (beforeBraces instanceof LattePhpMethodArgs) {
				beforeBraces = PsiTreeUtil.skipWhitespacesBackward(beforeBraces);
			}

			if (beforeBraces != null && beforeBraces.getText().equals("(")) {
				prevElement = PsiTreeUtil.skipWhitespacesBackward(beforeBraces);
			}
		}

		LattePhpType type = null;
		if (prevElement instanceof LattePhpStaticVariable) {
			type = ((LattePhpStaticVariable) prevElement).getPropertyType();
		} else if (prevElement instanceof LattePhpClass) {
			type = ((LattePhpClass) prevElement).getPhpType();
		} else if (prevElement instanceof LattePhpMethod) {
			type = ((LattePhpMethod) prevElement).getReturnType();
		} else if (prevElement instanceof LattePhpProperty) {
			type = ((LattePhpProperty) prevElement).getPropertyType();
		} else if (prevElement instanceof LattePhpConstant) {
			type = ((LattePhpConstant) prevElement).getConstantType();
		} else if (prevElement instanceof LattePhpVariable) {
			type = ((LattePhpVariable) prevElement).getPhpType();
		}
		return type != null ? type : new LattePhpType("mixed", false);
	}

	public static boolean isStatic(@NotNull PsiElement element) {
		PsiElement prev = PsiTreeUtil.skipWhitespacesBackward(element);
		return prev != null && prev.getNode().getElementType() == T_PHP_DOUBLE_COLON;
	}

	public static boolean isFunction(@NotNull PsiElement element) {
		PsiElement prev = PsiTreeUtil.skipWhitespacesBackward(element);
		return prev == null || (prev.getNode().getElementType() != T_PHP_DOUBLE_COLON && prev.getNode().getElementType() != T_PHP_OBJECT_OPERATOR);
	}

	public static LattePhpType getReturnType(@NotNull LattePhpMethod element) {
		LattePhpType type = element.getPhpType();
		Collection<PhpClass> phpClasses = type.getPhpClasses(element.getProject());
		String name = element.getMethodName();
		if (phpClasses.size() == 0) {
			LatteCustomFunctionSettings customFunction = LatteConfiguration.INSTANCE.getFunction(element.getProject(), name);
			return customFunction == null ? null : new LattePhpType(customFunction.getFunctionReturnType());
		}

		for (PhpClass phpClass : phpClasses) {
			for (Method phpMethod : phpClass.getMethods()) {
				if (phpMethod.getName().equals(name)) {
					return new LattePhpType(phpMethod.getType().toString(), LattePhpUtil.isNullable(phpMethod.getType()));
				}
			}
		}
		return null;
	}

	public static LattePhpType getPropertyType(@NotNull LattePhpStaticVariable element) {
		return getPropertyType(element.getProject(), element.getPhpType(), element.getVariableName());
	}

	public static LattePhpType getConstantType(@NotNull LattePhpConstant element) {
		return getPropertyType(element.getProject(), element.getPhpType(), element.getConstantName());
	}

	public static LattePhpType getPropertyType(@NotNull LattePhpProperty element) {
		return getPropertyType(element.getProject(), element.getPhpType(), element.getPropertyName());
	}

	private static LattePhpType getPropertyType(@NotNull Project project, @NotNull LattePhpType type, @NotNull String elementName) {
		Collection<PhpClass> phpClasses = type.getPhpClasses(project);
		if (phpClasses.size() == 0) {
			return null;
		}

		for (PhpClass phpClass : phpClasses) {
			for (Field field : phpClass.getFields()) {
				if (field.getName().equals(LattePhpUtil.normalizePhpVariable(elementName))) {
					return new LattePhpType(field.getType().toString(), LattePhpUtil.isNullable(field.getType()));
				}
			}
		}
		return null;
	}

	public static LattePhpType getPhpType(@NotNull LattePhpClass element) {
		return new LattePhpType(element.getClassName(), false);
	}

	public static boolean isTemplateType(@NotNull LattePhpClass element) {
		return LatteUtil.matchParentMacroName(element, "templateType");
	}

	public static boolean isVarTypeDefinition(@NotNull LattePhpVariable element) {
		return LatteUtil.matchParentMacroName(element, "varType");
	}

	public static boolean isVarDefinition(@NotNull LattePhpVariable element) {
		return LatteUtil.matchParentMacroName(element, "var") || LatteUtil.matchParentMacroName(element, "default");
	}

	public static boolean isDefinitionInForeach(@NotNull PsiElement element) {
		PsiElement parent = element.getParent();
		if (parent != null && parent.getNode().getElementType() == PHP_FOREACH) {
			PsiElement prevElement = PsiTreeUtil.skipWhitespacesBackward(element);
			IElementType type = prevElement != null ? prevElement.getNode().getElementType() : null;
			return type == T_PHP_AS || type == T_PHP_DOUBLE_ARROW;

		} else if (parent != null && parent.getNode().getElementType() == PHP_ARRAY_OF_VARIABLES) {
			return isDefinitionInForeach(parent);
		}
		return false;
	}

	public static boolean isDefinitionInFor(@NotNull LattePhpVariable element) {
		LatteNetteAttrValue parentAttr = PsiTreeUtil.getParentOfType(element, LatteNetteAttrValue.class);
		if (parentAttr != null) {
			PsiElement nextElement = PsiTreeUtil.skipWhitespacesForward(element);
			if (nextElement == null || nextElement.getNode().getElementType() != LatteTypes.T_PHP_DEFINITION_OPERATOR) {
				return false;
			}
			PsiElement prevElement = PsiTreeUtil.skipWhitespacesBackward(parentAttr);
			if (prevElement == null || prevElement.getNode().getElementType() != LatteTypes.T_PHP_DEFINITION_OPERATOR) {
				return false;
			}

			prevElement = PsiTreeUtil.skipWhitespacesBackward(prevElement);
			return prevElement != null && prevElement.getText().equals("n:for");
		}
		return LatteUtil.matchParentMacroName(element, "for") && isNextDefinitionOperator(element);
	}

	private static boolean isNextDefinitionOperator(@NotNull PsiElement element) {
		PsiElement nextElement = PsiTreeUtil.skipWhitespacesForward(element);
		return nextElement != null && nextElement.getNode().getElementType() == LatteTypes.T_PHP_DEFINITION_OPERATOR;
	}

	public static boolean isDefinition(@NotNull LattePhpVariable element) {
		if (isVarTypeDefinition(element) || LatteUtil.matchParentMacroName(element, "capture")) {
			return true;
		}

		if (isVarDefinition(element)) {
			if (isNextDefinitionOperator(element)) {
				return true;
			}
		}

		PsiElement parent = element.getParent();
		if (parent == null) {
			return false;
		}

		if (parent.getNode().getElementType() == PHP_ARRAY_OF_VARIABLES) {
			if (isNextDefinitionOperator(parent)) {
				return true;
			}
		}

		if (isDefinitionInForeach(element)) {
			return true;
		}

		return isDefinitionInFor(element);
	}

	public static String getName(LattePhpVariable element) {
		return element.getVariableName();
	}

	public static PsiElement setName(LattePhpMethod element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		PsiElement method = LatteElementFactory.createMethod(element.getProject(), newName);
		if (method == null) {
			return element;
		}
		return replaceChildNode(element, method, keyNode);
	}

	public static PsiElement setName(LattePhpProperty element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		PsiElement property = LatteElementFactory.createProperty(element.getProject(), newName);
		if (property == null) {
			return element;
		}
		return replaceChildNode(element, property, keyNode);
	}

	public static PsiElement setName(LattePhpConstant element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		PsiElement property = LatteElementFactory.createConstant(element.getProject(), newName);
		if (property == null) {
			return element;
		}
		return replaceChildNode(element, property, keyNode);
	}

	public static PsiElement setName(LattePhpStaticVariable element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		PsiElement property = LatteElementFactory.createStaticVariable(element.getProject(), newName);
		if (property == null) {
			return element;
		}
		return replaceChildNode(element, property, keyNode);
	}

	public static PsiElement setName(LattePhpVariable element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		PsiElement variable = LatteElementFactory.createVariable(element.getProject(), newName);
		if (variable == null) {
			return element;
		}
		return replaceChildNode(element, variable, keyNode);
	}

	public static PsiElement setName(LattePhpClass element, String newName) {
		ASTNode keyNode = element.getFirstChild().getNode();
		String namespace = element.getText().substring(0, element.getText().lastIndexOf("\\"));
		PsiElement classType = LatteElementFactory.createClassType(element.getProject(), namespace + "\\" + newName);
		if (classType == null) {
			return element;
		}
		return replaceChildNode(element, classType, keyNode);
	}

	@NotNull
	private static PsiElement replaceChildNode(@NotNull PsiElement psiElement, @NotNull PsiElement newElement, @Nullable ASTNode keyNode) {
		ASTNode newKeyNode = newElement.getFirstChild().getNode();
		if (newKeyNode == null) {
			return psiElement;
		}

		if (keyNode == null) {
			psiElement.getNode().addChild(newKeyNode);

		} else {
			psiElement.getNode().replaceChild(keyNode, newKeyNode);
		}
		return psiElement;
	}

	public static PsiElement getNameIdentifier(LatteMacroModifier element) {
		return findFirstChildWithType(element, T_MACRO_FILTERS);
	}

	public static PsiElement getNameIdentifier(LattePhpVariable element) {
		return findFirstChildWithType(element, T_MACRO_ARGS_VAR);
	}

	public static String getName(LattePhpStaticVariable element) {
		PsiElement found = findFirstChildWithType(element, T_MACRO_ARGS_VAR);
		return found != null ? LattePhpUtil.normalizePhpVariable(found.getText()) : null;
	}

	public static PsiElement getNameIdentifier(LattePhpStaticVariable element) {
		return findFirstChildWithType(element, T_MACRO_ARGS_VAR);
	}

	public static PsiElement getNameIdentifier(PsiElement element) {
		return findFirstChildWithType(element, T_PHP_METHOD);
	}

	public static PsiElement getNameIdentifier(LattePhpConstant element) {
		return findFirstChildWithType(element, T_PHP_IDENTIFIER);
	}

	public static PsiElement getNameIdentifier(LattePhpProperty element) {
		return findFirstChildWithType(element, T_PHP_IDENTIFIER);
	}

	public static PsiElement getNameIdentifier(LatteMacroTag element) {
		return findFirstChildWithType(element, T_MACRO_NAME);
	}

	public static String getName(LatteMacroTag element) {
		return element.getMacroName();
	}

	public static PsiElement getNameIdentifier(LattePhpClass element) {
		return findFirstChildWithType(element, T_PHP_CLASS_NAME);
	}

	public static String getName(PsiElement element) {
		PsiElement found = findFirstChildWithType(element, T_PHP_METHOD);
		return found != null ? found.getText() : null;
	}

	public static String getName(LatteMacroModifier element) {
		return element.getModifierName();
	}

	public static String getName(LattePhpConstant element) {
		PsiElement found = findFirstChildWithType(element, T_PHP_IDENTIFIER);
		return found != null ? found.getText() : null;
	}

	public static String getName(LattePhpClass element) {
		PsiElement found = findFirstChildWithType(element, T_PHP_METHOD);
		return found != null ? LattePhpUtil.normalizeClassName(found.getText()) : null;
	}

	public static String getName(LattePhpProperty element) {
		PsiElement found = findFirstChildWithType(element, T_PHP_IDENTIFIER);
		return found != null ? found.getText() : null;
	}

	public static PsiElement setName(PsiElement element, String newName) {
		return element;
	}

	private static PsiElement findFirstChildWithType(PsiElement element, @NotNull IElementType type) {
		ASTNode keyNode = element.getNode().findChildByType(type);
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	@Nullable
	private static ASTNode findFirstChildNodeWithType(PsiElement element, @NotNull IElementType type) {
		return element.getNode().findChildByType(type);
	}
}

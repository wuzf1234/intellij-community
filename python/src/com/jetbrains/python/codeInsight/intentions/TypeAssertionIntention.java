package com.jetbrains.python.codeInsight.intentions;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyReturnTypeReference;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: ktisha
 *
 * Helps to specify type by assertion
 */
public class TypeAssertionIntention implements IntentionAction {

  public TypeAssertionIntention() {
  }

  @NotNull
  public String getText() {
    return PyBundle.message("INTN.insert.assertion");
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INTN.insert.assertion");
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    PyExpression problemElement =
      PsiTreeUtil.getTopmostParentOfType(file.findElementAt(editor.getCaretModel().getOffset()-1), PyQualifiedExpression.class);
    if (problemElement == null) return false;
    if (problemElement instanceof PyQualifiedExpression && ((PyQualifiedExpression)problemElement).getReferencedName() == null) {
      final PyExpression qualifier = ((PyQualifiedExpression)problemElement).getQualifier();
      if (qualifier != null && !qualifier.getText().equals(PyNames.CANONICAL_SELF)) {
        problemElement = qualifier;
      }
    }
    final PyType type = problemElement.getType(TypeEvalContext.fast());
    return (type == null || type instanceof PyReturnTypeReference);
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PyExpression problemElement = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()-1),
                                                                              PyExpression.class);
    if (problemElement != null) {
      PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);

      String name = problemElement.getText();
      if (problemElement instanceof PyQualifiedExpression) {
        final PyExpression qualifier = ((PyQualifiedExpression)problemElement).getQualifier();
        if (qualifier != null && !qualifier.getText().equals(PyNames.CANONICAL_SELF)) {
          final String referencedName = ((PyQualifiedExpression)problemElement).getReferencedName();
          if (referencedName == null || PyNames.GETITEM.equals(referencedName))
            name = qualifier.getText();
        }
      }

      final String text = "assert isinstance(" + name + ", )";
      PyAssertStatement assertStatement = elementGenerator.createFromText(LanguageLevel.forElement(problemElement),
                                                                          PyAssertStatement.class, text);

      final PsiElement parentStatement = PsiTreeUtil.getParentOfType(problemElement, PyStatement.class);
      if (parentStatement == null) return;
      final PsiElement parent = parentStatement.getParent();
      PsiElement element;
      if (parentStatement instanceof PyAssignmentStatement &&
          ((PyAssignmentStatement)parentStatement).getTargets()[0] == problemElement) {
        element = parent.addAfter(assertStatement, parentStatement);
      }
      else {
        element = parent.addBefore(assertStatement, parentStatement);
      }

      int textOffSet = element.getTextOffset();
      editor.getCaretModel().moveToOffset(textOffSet);

      element = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(element);
      final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(element);
      builder.replaceRange(TextRange.create(text.length()-1, text.length()-1), PyNames.OBJECT);
      Template template = ((TemplateBuilderImpl)builder).buildInlineTemplate();
      TemplateManager.getInstance(project).startTemplate(editor, template);
    }
  }

  public boolean startInWriteAction() {
    return true;
  }
}
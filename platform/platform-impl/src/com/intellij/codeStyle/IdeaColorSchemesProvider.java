/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeStyle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.impl.BundledColorSchemesProvider;
import com.intellij.util.PlatformUtils;

/**
 * @author Konstantin Bulenkov
 */
public class IdeaColorSchemesProvider implements BundledColorSchemesProvider {
  public static final String[] PATHS = {
    "/colorSchemes/Darcula"
  };

  @Override
  public String[] getBundledSchemesRelativePaths() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return null;
    if (PlatformUtils.isCommunity() || PlatformUtils.isIdea()) return PATHS;

    return null;
  }

  @Override
  public String getDefaultSchemaExtensionPath() {
    return null;
  }
}

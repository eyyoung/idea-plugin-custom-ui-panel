package com.nd.sdp.common.codeCompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.nd.sdp.common.model.Config;
import com.nd.sdp.common.model.Widget;
import com.nd.sdp.common.task.CheckGradleTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Provider
 * Created by Young on 2017/11/29.
 */
public class WidgetProvider extends CompletionProvider<CompletionParameters> {

    private final Collection<LookupElement> myLookupElements;
    private final Config mConfig;

    public WidgetProvider(Config config, List<Widget> widgetList) {
        this.mConfig = config;
        widgetList.removeIf(widget -> widget.getXml() == null || widget.getXml().isEmpty());
        myLookupElements = ContainerUtil.map2List(widgetList, this::createLookupElementBuilder);
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    private LookupElementBuilder createLookupElementBuilder(Widget item) {
        //noinspection ConstantConditions
        String substring = item.getXml().substring(0, item.getXml().length() - 1);
        return LookupElementBuilder.create(substring).withCaseSensitivity(true)
                .withLookupString(item.getName())
                .withTypeText(item.getName())
                .withPresentableText(item.getName())
                .withInsertHandler(new InsertHandler<LookupElement>() {
                    @Override
                    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
                        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                            @Override
                            public void run() {
                                Project project = insertionContext.getProject();
                                WriteCommandAction.runWriteCommandAction(project, () -> {
                                    if (item.getDependencies() == null || item.getDependencies().getDependency() == null || item.getDependencies().getDependency().isEmpty()) {
                                        if (item.getDependency() == null ||
                                                item.getDependency().getGroupId() == null ||
                                                item.getDependency().getGroupId().isEmpty() ||
                                                item.getDependency().getArtifactId() == null ||
                                                item.getDependency().getArtifactId().isEmpty() ||
                                                item.getDependency().getVersion() == null ||
                                                item.getDependency().getVersion().isEmpty()) {
                                            if (mConfig.getCommonDep() == null) {
                                                return;
                                            }
                                            ProgressManager.getInstance().executeNonCancelableSection(new CheckGradleTask(mConfig.getCommonDep(), project, insertionContext.getEditor()));
                                            return;
                                        }
                                        ProgressManager.getInstance().executeNonCancelableSection(new CheckGradleTask(item.getDependency(), project, insertionContext.getEditor()));
                                    } else {
                                        for (int i = 0; i < item.getDependencies().getDependency().size(); i++) {
                                            ProgressManager.getInstance().executeNonCancelableSection(new CheckGradleTask(item.getDependency(), project, insertionContext.getEditor()));
                                        }
                                    }
                                });
                                if (project == null) {
                                    return;
                                }
                                PsiFile file = insertionContext.getFile();
                                CodeStyleManager.getInstance(project).reformat(file);
                            }
                        });
                    }
                });
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        completionResultSet.addAllElements(myLookupElements);
    }

}

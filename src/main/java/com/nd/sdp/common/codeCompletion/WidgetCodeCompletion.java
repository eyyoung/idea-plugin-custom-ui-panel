package com.nd.sdp.common.codeCompletion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.nd.sdp.common.model.Config;
import com.nd.sdp.common.model.Widget;
import com.nd.sdp.common.task.Callback;
import com.nd.sdp.common.task.GetRealConfigTask;
import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.SwingXUtilities;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Code Completion
 * Created by Young on 2017/11/29.
 */
public class WidgetCodeCompletion extends CompletionContributor {

    private Config mConfig;
    private Map<String, Widget> mWidgetMap;
    private JBPopup mPopup;

    public WidgetCodeCompletion() {
        GetRealConfigTask getRealConfigTask = new GetRealConfigTask(config -> {
            afterGetConfig(config);
            return null;
        });
        ProgressManager.getInstance().executeNonCancelableSection(getRealConfigTask);
    }

    private void afterGetConfig(Config config) {
        List<Widget> widget = config.getWidgets().getWidget();
        mWidgetMap = new HashMap<>();
        widget.forEach(widget1 -> mWidgetMap.put(widget1.getName(), widget1));
        extend(CompletionType.BASIC, tag(), new WidgetProvider(config, widget));
    }

    private static ElementPattern<PsiElement> tag() {
        return psiElement().inside(XmlPatterns.xmlFile());
    }

    @Override
    public void duringCompletion(@NotNull CompletionInitializationContext context) {
        super.duringCompletion(context);
        if (mPopup != null) {
            mPopup.cancel();
        }
        LookupEx activeLookup = LookupManager.getActiveLookup(context.getEditor());
        if (activeLookup == null) {
            return;
        }
        activeLookup.addLookupListener(new LookupAdapter() {

            @Override
            public void itemSelected(LookupEvent event) {
                super.itemSelected(event);
                if (mPopup != null) {
                    mPopup.cancel();
                }
            }

            @Override
            public void lookupCanceled(LookupEvent event) {
                super.lookupCanceled(event);
                if (mPopup != null) {
                    mPopup.cancel();
                }
            }

            @Override
            public void currentItemChanged(LookupEvent event) {
                super.currentItemChanged(event);
                SwingUtilities.invokeLater(() -> {
                    if (mPopup != null) {
                        mPopup.cancel();
                    }
                    LookupElement item = event.getItem();
                    if (item == null) {
                        return;
                    }
                    Set<String> allLookupStrings = item.getAllLookupStrings();
                    Widget widget = null;
                    for (String allLookupString : allLookupStrings) {
                        widget = mWidgetMap.get(allLookupString);
                        if (widget != null) {
                            break;
                        }
                    }
                    if (widget == null || widget.getImage() == null) {
                        return;
                    }
                    Point locationOnScreen = activeLookup.getComponent().getLocationOnScreen();
                    JXImageView jxImageView = new JXImageView();
                    try {
                        jxImageView.setImage(new URL(widget.getImage()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    jxImageView.setPreferredSize(new Dimension(400, 400));
                    mPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(jxImageView, jxImageView)
                            .createPopup();
                    mPopup
                            .show(RelativePoint.fromScreen(new Point((int) (locationOnScreen.getX() + activeLookup.getComponent().getWidth() + 20), ((int) locationOnScreen.getY()))));
                });
            }
        });
    }
}

package com.shade.decima.ui.navigator.menu;

import com.shade.decima.model.app.Project;
import com.shade.decima.model.app.Workspace;
import com.shade.decima.ui.Application;
import com.shade.decima.ui.CommonDataKeys;
import com.shade.decima.ui.editor.FileEditorInput;
import com.shade.decima.ui.navigator.impl.NavigatorProjectNode;
import com.shade.platform.ui.PlatformDataKeys;
import com.shade.platform.ui.editors.Editor;
import com.shade.platform.ui.editors.EditorManager;
import com.shade.platform.ui.editors.SaveableEditor;
import com.shade.platform.ui.menus.MenuItem;
import com.shade.platform.ui.menus.MenuItemContext;
import com.shade.platform.ui.menus.MenuItemRegistration;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import javax.swing.*;

import static com.shade.decima.ui.menu.MenuConstants.CTX_MENU_NAVIGATOR_GROUP_PROJECT;
import static com.shade.decima.ui.menu.MenuConstants.CTX_MENU_NAVIGATOR_ID;

@MenuItemRegistration(parent = CTX_MENU_NAVIGATOR_ID, name = "Close Project", group = CTX_MENU_NAVIGATOR_GROUP_PROJECT, order = 1000)
public class ProjectCloseItem extends MenuItem {
    @Override
    public void perform(@NotNull MenuItemContext ctx) {
        final Workspace workspace = ctx.getData(CommonDataKeys.WORKSPACE_KEY);
        final Project project = ctx.getData(CommonDataKeys.PROJECT_KEY);

        if (confirmProjectClose(project, ctx.getData(PlatformDataKeys.EDITOR_MANAGER_KEY))) {
            workspace.closeProject(project.getContainer(), true);
        }
    }

    @Override
    public boolean isEnabled(@NotNull MenuItemContext ctx) {
        return ctx.getData(CommonDataKeys.PROJECT_KEY) != null;
    }

    @Override
    public boolean isVisible(@NotNull MenuItemContext ctx) {
        return ctx.getData(PlatformDataKeys.SELECTION_KEY) instanceof NavigatorProjectNode;
    }

    public static boolean confirmProjectClose(@NotNull Project project, @Nullable EditorManager manager) {
        if (isProjectDirty(project, manager)) {
            return JOptionPane.showConfirmDialog(
                Application.getFrame(),
                "Project '%s' has unsaved changes.\n\nAre you sure you want to close it?".formatted(project.getContainer().getName()),
                "Confirm Close",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            ) == JOptionPane.OK_OPTION;
        } else {
            return true;
        }
    }

    public static boolean isProjectDirty(@NotNull Project project, @Nullable EditorManager manager) {
        if (project.getPersister().hasChanges()) {
            return true;
        }

        if (manager != null) {
            for (Editor editor : manager.getEditors()) {
                if (editor instanceof SaveableEditor e && e.isDirty() && e.getInput() instanceof FileEditorInput i && i.getProject() == project) {
                    return true;
                }
            }
        }

        return false;
    }
}
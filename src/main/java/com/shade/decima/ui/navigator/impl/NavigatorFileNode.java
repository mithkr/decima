package com.shade.decima.ui.navigator.impl;

import com.shade.decima.model.app.runtime.ProgressMonitor;
import com.shade.decima.model.util.NotNull;
import com.shade.decima.model.util.Nullable;
import com.shade.decima.ui.Application;
import com.shade.decima.ui.UIUtils;
import com.shade.decima.ui.editor.NodeEditorInput;
import com.shade.decima.ui.icon.Icons;
import com.shade.decima.ui.navigator.NavigatorNode;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.Optional;

public class NavigatorFileNode extends NavigatorNode implements NavigatorNode.ActionListener {
    private final String name;
    private final long hash;
    private final int size;

    public NavigatorFileNode(@Nullable NavigatorNode parent, @NotNull String name, long hash) {
        super(parent);
        this.name = name;
        this.hash = hash;
        this.size = Optional.ofNullable(UIUtils.getPackfile(this).getFileEntry(hash))
            .map(entry -> entry.span().size())
            .orElse(0);
    }

    @NotNull
    @Override
    public String getLabel() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        if (name.indexOf('.') < 0) {
            return Icons.NODE_BINARY;
        } else {
            return super.getIcon();
        }
    }

    @NotNull
    @Override
    public NavigatorNode[] getChildren(@NotNull ProgressMonitor monitor) throws Exception {
        return EMPTY_CHILDREN;
    }

    public long getHash() {
        return hash;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void actionPerformed(@NotNull InputEvent event) {
        Application.getFrame().getEditorManager().openEditor(new NodeEditorInput(this), !event.isControlDown());
        event.consume();
    }
}

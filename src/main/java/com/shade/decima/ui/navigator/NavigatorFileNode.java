package com.shade.decima.ui.navigator;

import com.shade.decima.archive.Archive;
import com.shade.decima.util.NotNull;
import com.shade.decima.util.Nullable;

import java.util.Collections;
import java.util.List;

public class NavigatorFileNode extends NavigatorNode {
    private final NavigatorNode parent;
    private final String label;
    private final Archive.FileEntry file;

    public NavigatorFileNode(@Nullable NavigatorNode parent, @NotNull String label, @NotNull Archive.FileEntry file) {
        this.parent = parent;
        this.label = label;
        this.file = file;
    }

    @NotNull
    public Archive.FileEntry getFile() {
        return file;
    }

    @Nullable
    @Override
    public String getLabel() {
        return label;
    }

    @NotNull
    @Override
    public List<NavigatorNode> getChildren() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public NavigatorNode getParent() {
        return parent;
    }
}
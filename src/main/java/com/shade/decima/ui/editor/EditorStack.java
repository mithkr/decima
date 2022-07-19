package com.shade.decima.ui.editor;

import com.formdev.flatlaf.FlatClientProperties;
import com.shade.decima.model.app.DataKey;
import com.shade.decima.model.app.ProjectChangeListener;
import com.shade.decima.model.app.ProjectContainer;
import com.shade.decima.model.app.Workspace;
import com.shade.decima.model.util.NotNull;
import com.shade.decima.model.util.Nullable;
import com.shade.decima.ui.action.Actions;
import com.shade.decima.ui.editor.binary.BinaryEditor;
import com.shade.decima.ui.editor.property.PropertyEditorPane;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class EditorStack extends JTabbedPane implements EditorManager {
    public static final DataKey<Editor> EDITOR_KEY = new DataKey<>("editor", Editor.class);

    private final List<EditorChangeListener> listeners = new ArrayList<>();

    public EditorStack(@NotNull Workspace workspace) {
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (IntConsumer) index -> {
            final JComponent component = (JComponent) getComponentAt(index);
            final Editor editor = EDITOR_KEY.get(component);
            closeEditor(editor);
        });

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        setFocusable(false);

        addChangeListener(e -> {
            final JComponent component = (JComponent) getSelectedComponent();

            if (component != null) {
                fireEditorChangeEvent(EditorChangeListener::editorOpened, EDITOR_KEY.get(component));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                final int index = indexAtLocation(e.getX(), e.getY());
                if (SwingUtilities.isRightMouseButton(e) && index >= 0) {
                    setSelectedIndex(index);
                    final JPopupMenu menu = new JPopupMenu();
                    Actions.contribute(menu, "popup:editor");
                    menu.show(EditorStack.this, e.getX(), e.getY());
                }
            }
        });

        workspace.addProjectChangeListener(new ProjectChangeListener() {
            @Override
            public void projectClosed(@NotNull ProjectContainer container) {
                for (Editor editor : getEditors()) {
                    if (editor.getInput().getProject().getContainer().equals(container)) {
                        closeEditor(editor);
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    public Editor findEditor(@NotNull EditorInput input) {
        final JComponent component = findEditorComponent(e -> e.getInput().equals(input));

        if (component != null) {
            return EDITOR_KEY.get(component);
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public Editor openEditor(@NotNull EditorInput input, boolean focus) {
        JComponent component = findEditorComponent(e -> e.getInput().equals(input));

        if (component == null) {
            Editor editor;

            try {
                // FIXME: Figure a better way of creating the required editor
                editor = new PropertyEditorPane(input);
            } catch (Exception ignored) {
                editor = new BinaryEditor(input);
            }

            component = editor.createComponent();
            component.putClientProperty(EDITOR_KEY, editor);

            addTab(input.getName(), input.getIcon(), component, input.getDescription());
        }

        final Editor editor = EDITOR_KEY.get(component);

        if (getSelectedComponent() != component) {
            setSelectedComponent(component);
            fireEditorChangeEvent(EditorChangeListener::editorOpened, editor);
        }

        if (focus) {
            editor.getController().getFocusComponent().requestFocusInWindow();
        }

        return editor;
    }

    @Nullable
    @Override
    public Editor getActiveEditor() {
        final JComponent component = (JComponent) getSelectedComponent();

        if (component != null) {
            return EDITOR_KEY.get(component);
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public Editor[] getEditors() {
        final Editor[] editors = new Editor[getTabCount()];

        for (int i = 0; i < editors.length; i++) {
            final JComponent component = (JComponent) getComponentAt(i);
            final Editor editor = EDITOR_KEY.get(component);
            editors[i] = editor;
        }

        return editors;
    }

    @Override
    public void closeEditor(@NotNull Editor editor) {
        final JComponent component = findEditorComponent(e -> e.equals(editor));

        if (component != null) {
            remove(component);
            fireEditorChangeEvent(EditorChangeListener::editorClosed, editor);
        }
    }

    @Nullable
    private JComponent findEditorComponent(@NotNull Predicate<Editor> predicate) {
        for (int i = 0; i < getTabCount(); i++) {
            final JComponent component = (JComponent) getComponentAt(i);
            final Editor editor = EDITOR_KEY.get(component);

            if (predicate.test(editor)) {
                return component;
            }
        }

        return null;
    }

    @Override
    public void addEditorChangeListener(@NotNull EditorChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEditorChangeListener(@NotNull EditorChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireEditorChangeEvent(@NotNull BiConsumer<EditorChangeListener, Editor> consumer, @NotNull Editor editor) {
        for (EditorChangeListener listener : listeners) {
            consumer.accept(listener, editor);
        }
    }
}

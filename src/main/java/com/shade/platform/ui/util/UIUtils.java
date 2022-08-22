package com.shade.platform.ui.util;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import com.shade.platform.model.util.IOUtils;
import com.shade.platform.ui.controls.validation.InputValidator;
import com.shade.platform.ui.controls.validation.Validation;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URI;

public final class UIUtils {
    private UIUtils() {
    }

    @NotNull
    public static Color getInactiveTextColor() {
        return UIManager.getColor("Label.disabledForeground");
    }

    public static float getSmallerFontSize() {
        final Font font = UIManager.getFont("Label.font");
        return Math.max(font.getSize() - UIScale.scale(2f), UIScale.scale(11f));
    }

    public static void setRenderingHints(@NotNull Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, UIManager.get(RenderingHints.KEY_TEXT_ANTIALIASING));
        g.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, UIManager.get(RenderingHints.KEY_TEXT_LCD_CONTRAST));
    }

    public static void removeFrom(@NotNull Rectangle rect, @Nullable Insets insets) {
        if (insets != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= insets.left + insets.right;
            rect.height -= insets.top + insets.bottom;
        }
    }

    public static void installInputValidator(@NotNull JComponent component, @NotNull InputValidator validator, @Nullable PropertyChangeListener validationListener) {
        component.setInputVerifier(validator);

        if (validationListener != null) {
            validator.getPropertyChangeSupport().addPropertyChangeListener(InputValidator.PROPERTY_VALIDATION, validationListener);
        }
    }

    public static boolean isValid(@NotNull JComponent component) {
        final InputVerifier verifier = component.getInputVerifier();
        if (verifier instanceof InputValidator validator) {
            final Validation validation = validator.getLastValidation();
            return validation == null || validation.isOK();
        }
        return true;
    }

    public static void addOpenFileAction(@NotNull JTextField component, @NotNull String title, @Nullable FileFilter filter) {
        final JToolBar toolBar = new JToolBar();

        toolBar.add(new AbstractAction(null, UIManager.getIcon("Tree.openIcon")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();

                chooser.setDialogTitle(title);
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
                    component.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        component.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
    }

    public static void addOpenDirectoryAction(@NotNull JTextField component, @NotNull String title) {
        final JToolBar toolBar = new JToolBar();

        toolBar.add(new AbstractAction(null, UIManager.getIcon("Tree.openIcon")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();

                chooser.setDialogTitle(title);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (chooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
                    component.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        component.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
    }

    public static void minimizePanel(@NotNull JSplitPane pane, boolean topOrLeft) {
        try {
            final Field field = BasicSplitPaneUI.class.getDeclaredField("keepHidden");
            field.setAccessible(true);
            field.set(pane.getUI(), true);
        } catch (Exception ignored) {
            return;
        }

        pane.setLastDividerLocation(pane.getDividerLocation());
        pane.setDividerLocation(topOrLeft ? 0.0 : 1.0);
    }

    public static void delegateKey(@NotNull JComponent source, int sourceKeyCode, @NotNull JComponent target, @NotNull String targetActionKey) {
        delegateAction(source, KeyStroke.getKeyStroke(sourceKeyCode, 0), target, targetActionKey);
    }

    public static void delegateAction(@NotNull JComponent source, @NotNull JComponent target, @NotNull String targetActionKey, int targetCondition) {
        final InputMap inputMap = target.getInputMap(targetCondition);

        for (KeyStroke keyStroke : inputMap.allKeys()) {
            if (targetActionKey.equals(inputMap.get(keyStroke))) {
                delegateAction(source, keyStroke, target, targetActionKey);
            }
        }
    }

    public static void putAction(@NotNull JComponent target, int condition, @NotNull KeyStroke keystroke, @NotNull Action action) {
        target.getInputMap(condition).put(keystroke, action);
        target.getActionMap().put(action, action);
    }

    public static void installPopupMenu(@NotNull JTree tree, @NotNull JPopupMenu menu) {
        installPopupMenu(tree, menu, new SelectionProvider<JTree, TreePath>() {
            @Nullable
            @Override
            public TreePath getSelection(@NotNull JTree component, @NotNull MouseEvent event) {
                final TreePath path = component.getPathForLocation(event.getX(), event.getY());
                if (path != null) {
                    return path;
                } else {
                    return component.getSelectionPath();
                }
            }

            @Override
            public void setSelection(@NotNull JTree component, @NotNull TreePath selection) {
                component.setSelectionPath(selection);
            }
        });
    }

    public static void installPopupMenu(@NotNull JTabbedPane pane, @NotNull JPopupMenu menu) {
        installPopupMenu(pane, menu, new SelectionProvider<JTabbedPane, Integer>() {
            @Nullable
            @Override
            public Integer getSelection(@NotNull JTabbedPane component, @NotNull MouseEvent event) {
                final int index = component.indexAtLocation(event.getX(), event.getY());
                return index < 0 ? null : index;
            }

            @Override
            public void setSelection(@NotNull JTabbedPane component, @NotNull Integer selection) {
                component.setSelectedIndex(selection);
            }
        });
    }

    public static <T extends JComponent, U> void installPopupMenu(
        @NotNull T component,
        @NotNull JPopupMenu menu,
        @NotNull SelectionProvider<T, U> provider
    ) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final U selection = provider.getSelection(component, e);
                    if (selection != null) {
                        provider.setSelection(component, selection);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final U selection = provider.getSelection(component, e);
                    if (selection != null) {
                        menu.show(component, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    public static void showErrorDialog(@NotNull Throwable throwable) {
        showErrorDialog(throwable, "An error occurred during program execution");
    }

    public static void showErrorDialog(@NotNull Throwable throwable, @NotNull String title) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        final JScrollPane pane = new JScrollPane(new JTextArea(sw.toString().replace("\t", "    ")));
        pane.setPreferredSize(new Dimension(640, 480));

        JOptionPane.showMessageDialog(null, pane, title, JOptionPane.ERROR_MESSAGE);
    }

    private static void delegateAction(@NotNull JComponent source, @NotNull KeyStroke sourceKeyStroke, @NotNull JComponent target, @NotNull String targetActionKey) {
        final String sourceActionKey = "delegate-" + targetActionKey;

        source.getInputMap().put(sourceKeyStroke, sourceActionKey);
        source.getActionMap().put(sourceActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Action action = target.getActionMap().get(targetActionKey);
                if (action != null) {
                    action.actionPerformed(new ActionEvent(target, e.getID(), targetActionKey, e.getWhen(), e.getModifiers()));
                }
            }
        });
    }

    public interface Labels {
        @NotNull
        static JLabel h1(@NotNull String text) {
            final JLabel label = new JLabel(text);
            label.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
            return label;
        }

        @NotNull
        static JLabel link(@NotNull URI uri) {
            final JLabel label = new JLabel("<html><a href=\"#\">%s</a></html>".formatted(uri));
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    IOUtils.unchecked(() -> {
                        Desktop.getDesktop().browse(uri);
                        return null;
                    });
                }
            });
            return label;
        }
    }

    public interface SelectionProvider<T extends JComponent, U> {
        @Nullable
        U getSelection(@NotNull T component, @NotNull MouseEvent event);

        void setSelection(@NotNull T component, @NotNull U selection);
    }
}
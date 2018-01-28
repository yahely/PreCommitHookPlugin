import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.rt.execution.testFrameworks.ProcessBuilder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class HookConfigurable implements Configurable {

    private static final String PATH_KEY = "pre-commit-hook.path";
    public static final String DEFAULT_FILE = ProcessBuilder.isWindows ? "pre-commit-hook.bat" : "pre-commit-hook.sh";

    private JTextField pathTextField;
    private String storedPath;

    private final Project project;

    public HookConfigurable(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Pre Commit Hook";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        storedPath = getScriptPath(project);

        pathTextField = new JTextField();

        TextFieldWithBrowseButton fieldWithButton = new TextFieldWithBrowseButton(pathTextField);
        fieldWithButton.addBrowseFolderListener("Select Hook Script", "", null,
                FileChooserDescriptorFactory.createSingleFileDescriptor());

        JPanel container = new JPanel(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), 12, 12));

        GridConstraints pathLabelConstraint = new GridConstraints();
        pathLabelConstraint.setRow(0);
        pathLabelConstraint.setColumn(0);
        pathLabelConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathLabelConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(new JLabel("Script path"), pathLabelConstraint);

        GridConstraints pathFieldConstraint = new GridConstraints();
        pathFieldConstraint.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        pathFieldConstraint.setFill(GridConstraints.FILL_HORIZONTAL);
        pathFieldConstraint.setAnchor(GridConstraints.ANCHOR_WEST);
        pathFieldConstraint.setRow(0);
        pathFieldConstraint.setColumn(1);
        pathFieldConstraint.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(fieldWithButton, pathFieldConstraint);

        JPanel spacer = new JPanel();
        GridConstraints spacerConstraints = new GridConstraints();
        spacerConstraints.setRow(1);
        spacerConstraints.setFill(GridConstraints.FILL_BOTH);
        container.add(spacer, spacerConstraints);

        return container;
    }

    @Override
    public boolean isModified() {
        String fieldValue = getTrimmedFieldValue();

        if (storedPath == null) {
            return fieldValue != null;
        }

        return !storedPath.equals(fieldValue);
    }

    @Override
    public void apply() throws ConfigurationException {
        String filePath = getTrimmedFieldValue();

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(PATH_KEY, filePath);

        storedPath = filePath;
    }

    private String getTrimmedFieldValue() {
        if (pathTextField == null) {
            return null;
        }

        final String path = pathTextField.getText();
        if (path == null) {
            return null;
        }

        return path.trim();
    }

    @Override
    public void reset() {
        if (pathTextField != null) {
            pathTextField.setText(storedPath);
        }
    }

    @Override
    public void disposeUIResources() {
        storedPath = null;
        pathTextField = null;
    }

    @NotNull
    public static String getScriptPath(Project project) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String path = properties.getValue(PATH_KEY);

        if ((path == null) || (path.trim().isEmpty())) {
            path = DEFAULT_FILE;
        } else {
            path = path.trim();
        }

        return path;
    }
}
package cuchaz.enigma.gui;

import cuchaz.enigma.mapping.entry.ClassEntry;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

public class EditorTab extends Tab {
	public CodeDisplay area;
	public ClassEntry entry;
	Button closeButton = new Button("X");

	public EditorTab(TabPane pane, ClassEntry entry) {
		this.entry = entry;
		setText(entry.getSimpleName());
		setId("editor-tab");
		area = new CodeDisplay();
		closeButton.setId("editor-tab");
		closeButton.setOnAction(event -> pane.getTabs().remove(this));
		setGraphic(closeButton);
		setContent(new VirtualizedScrollPane<>(area));
	}
}

package cuchaz.enigma;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.strobel.decompiler.languages.java.ast.CompilationUnit;
import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.analysis.SourceIndex;
import cuchaz.enigma.analysis.Token;
import cuchaz.enigma.gui.EditorTab;
import cuchaz.enigma.gui.HierarchyItem;
import cuchaz.enigma.gui.HierarchyItemType;
import cuchaz.enigma.gui.TreeItemPredicate;
import cuchaz.enigma.lib.Constants;
import cuchaz.enigma.mapping.entry.ClassEntry;
import cuchaz.enigma.mapping.entry.Entry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.JarFile;

public class Enigma {

	@FXML public BorderPane root;
	@FXML public MenuBar menuBar;
	@FXML public Menu fileMenu;
	@FXML public MenuItem openJarMenu;
	@FXML public MenuItem closeJarMenu;
	@FXML public Menu openMappingsMenu;
	@FXML public MenuItem openEnigmaMappingsMenu;
	@FXML public MenuItem openTinyMappingsMenu;
	@FXML public MenuItem saveMappingsMenu;
	@FXML public Menu saveMappingsAsMenu;
	@FXML public MenuItem saveMappingsAsEngimaSingleMenu;
	@FXML public MenuItem saveMappingsAsEnigmaDirectoryMenu;
	@FXML public MenuItem saveMappingsAsSRGMenu;
	@FXML public MenuItem closeMappingsMenu;
	@FXML public MenuItem rebuildMethodNamesMenu;
	@FXML public MenuItem exportSourceMenu;
	@FXML public MenuItem exportJarMenu;
	@FXML public MenuItem exitMenu;
	@FXML public Menu viewMenu;
	@FXML public Menu themesMenu;
	@FXML public MenuItem darkThemeMenu;
	@FXML public MenuItem lightThemeMenu;
	@FXML public MenuItem addThemeMenu;
	@FXML public MenuItem bytecodeButton;
	@FXML public Menu helpMenu;
	@FXML public MenuItem aboutMenu;
	@FXML public SplitPane mainSplitPane;
	@FXML public ImageView searchIcon;
	@FXML public TextField searchBox;
	@FXML public TabPane treesPane;
	@FXML public Tab classesTab;
	@FXML public TreeView<String> classTree;
	@FXML public Tab recentTab;
	@FXML public TreeView<String> recentsTree;
	@FXML public Tab inheritanceTab;
	@FXML public TreeView<String> inheritanceTree;
	@FXML public Tab implTab;
	@FXML public TreeView<String> implTree;
	@FXML public Tab callGraphTab;
	@FXML public TreeView<String> callGraphTree;
	@FXML public VBox centerBox;
	@FXML public TabPane editorTabPane;
	@FXML public Text identifierInfo;
	@FXML public VBox identifierInfoBox;

	public static Enigma instance;
	public static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

	public Map<TreeItem<String>, ClassEntry> classEntryMap = new HashMap<>();
	FileChooser fileChooser = new FileChooser();
	public static Deobfuscator deobfuscator;
	private SourceIndex index;
	private Deque<EntryReference<Entry, Entry>> referenceStack;

	public static Stage stage;

	private static File getFile(String path) {
		// expand ~ to the home dir
		if (path.startsWith("~")) {
			// get the home dir
			File dirHome = new File(System.getProperty("user.home"));

			// is the path just ~/ or is it ~user/ ?
			if (path.startsWith("~/")) {
				return new File(dirHome, path.substring(2));
			} else {
				return new File(dirHome.getParentFile(), path.substring(1));
			}
		}

		return new File(path);
	}

	@FXML
	public void initialize() {
		stage = Main.stage;
		instance = this;
		Platform.runLater(() -> root.requestFocus());
		treesPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateSearchText());
		searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
			HierarchyItem root = ((HierarchyItem) ((TreeView) treesPane.getSelectionModel().getSelectedItem().getContent()).getRoot());
			root.predicateProperty().bind(Bindings.createObjectBinding(()
				-> TreeItemPredicate.<String>create(string
				-> newValue.isEmpty() || string.toLowerCase().contains(searchBox.getText().toLowerCase()))));
			expandChildren(root, !newValue.isEmpty());
			root.setExpanded(true);
		});
		editorTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, tab) -> {
			if (tab instanceof EditorTab) {
				deobfuscate(((EditorTab) tab).entry, null);
			}
		});
		referenceStack = Queues.newArrayDeque();
		if (Main.args.size() >= 1) {
			try {
				openJar(new JarFile(getFile(Main.args.get(0))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (Main.args.size() >= 2) {
			openEnigmaMappings(getFile(Main.args.get(1)));
		}
	}

	private void expandChildren(TreeItem<String> item, boolean bool) {
		if (item != null && !item.isLeaf()) {
			item.setExpanded(bool);
			for (TreeItem<String> child : item.getChildren()) {
				expandChildren(child, bool);
			}
		}
	}

	public void openTab(ClassEntry classEntry) {
		for (Tab tab : editorTabPane.getTabs()) {
			if (tab instanceof EditorTab && ((EditorTab) tab).entry.equals(classEntry)) {
				editorTabPane.getSelectionModel().select(tab);
				return;
			}
		}
		EditorTab tab = new EditorTab(editorTabPane, classEntry);
		editorTabPane.setOnMousePressed(event -> {
			if (event.isMiddleButtonDown()) {
				for (Tab t : editorTabPane.getTabs()) {
					if (event.toString().contains(t.getText())) {
						((Button) t.getGraphic()).fire();
						break;
					}
				}
			}
		});
		editorTabPane.getTabs().add(tab);
		editorTabPane.getSelectionModel().select(tab);
	}

	@FXML
	public void about() {

	}

	@FXML
	public void addTheme() {

	}

	@FXML
	public void closeJar() {

	}

	@FXML
	public void closeMappings() {

	}

	@FXML
	public void exit() {

	}

	@FXML
	public void exportJar() {

	}

	@FXML
	public void exportSource() {

	}

	@FXML
	public void openEnigmaMappings() {

	}

	public void openEnigmaMappings(File file) {

	}

	@FXML
	public void openJar() throws IOException {
		fileChooser.setTitle("Open Jar File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Archive Files", "*.jar"));
		fileChooser.setInitialDirectory(null);
		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			openJar(new JarFile(file));
		}
	}

	public void openJar(JarFile jar) throws IOException {
		onStartOpenJar();
		this.deobfuscator = new Deobfuscator(jar);
		this.onFinishOpenJar(jar.getName());
		refreshClasses();
	}

	public void onStartOpenJar() {
		lockTreeTabs(true);
	}

	public void lockTreeTabs(boolean bool) {
		for (Tab tab : treesPane.getTabs()) {
			tab.setDisable(bool);
		}
		searchBox.setDisable(bool);
		if (searchBox.isDisabled()) {
			searchBox.setPromptText("Loading...");
		} else {
			updateSearchText();
		}
	}

	public void updateSearchText() {
		searchBox.setPromptText("Search " + treesPane.getSelectionModel().getSelectedItem().getText());
	}

	public void onFinishOpenJar(String jarName) {
		stage.setTitle(Constants.NAME + " - " + jarName);
		this.classTree.setRoot(newRoot());
		editorTabPane.getTabs().clear();

		// update menu
		this.closeJarMenu.setDisable(false);
		this.openTinyMappingsMenu.setDisable(false);
		this.openEnigmaMappingsMenu.setDisable(false);
		this.saveMappingsMenu.setDisable(true);
		this.saveMappingsAsEngimaSingleMenu.setDisable(false);
		this.saveMappingsAsEnigmaDirectoryMenu.setDisable(false);
		this.saveMappingsAsSRGMenu.setDisable(false);
		this.closeMappingsMenu.setDisable(false);
		this.exportSourceMenu.setDisable(false);
		this.exportJarMenu.setDisable(false);
		lockTreeTabs(false);
	}

	public void setSource(EditorTab tab, String source) {
		tab.area.replaceText(source);
		tab.area.updateHighlighting();
	}

	public void deobfuscate(final ClassEntry classEntry, final EntryReference<Entry, Entry> obfReference) {
		EditorTab tab = null;
		for (Tab t : editorTabPane.getTabs()) {
			if (t instanceof EditorTab && ((EditorTab) t).entry.equals(classEntry)) {
				tab = (EditorTab) t;
				break;
			}
		}
		if (tab != null) {
			setSource(tab, "(deobfuscating...)");

			// run the deobfuscator in a separate thread so we don't block the GUI event queue
			EditorTab finalTab = tab;
			new Service<Void>() {
				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							// decompile,deobfuscate the bytecode
							CompilationUnit sourceTree = deobfuscator.getSourceTree(classEntry.getClassName());
							if (sourceTree == null) {
								Platform.runLater(() -> {
									setSource(finalTab, "Unable to find class: " + classEntry);
								});
								return null;
							}
							String source = deobfuscator.getSource(sourceTree);
							index = deobfuscator.getSourceIndex(sourceTree, source);

							Platform.runLater(() -> setSource(finalTab, index.getSource()));
							if (obfReference != null) {
								showReference(obfReference);
							}

							// set the highlighted tokens
							List<Token> obfuscatedTokens = Lists.newArrayList();
							List<Token> deobfuscatedTokens = Lists.newArrayList();
							List<Token> otherTokens = Lists.newArrayList();
							for (Token token : index.referenceTokens()) {
								EntryReference<Entry, Entry> reference = index.getDeobfReference(token);
								if (referenceIsRenameable(reference)) {
									if (entryHasDeobfuscatedName(reference.getNameableEntry())) {
										deobfuscatedTokens.add(token);
									} else {
										obfuscatedTokens.add(token);
									}
								} else {
									otherTokens.add(token);
								}
							}
							finalTab.area.addHighlights(obfuscatedTokens, deobfuscatedTokens, otherTokens);
							return null;
						}
					};
				}
			}.start();
		}
	}

	public boolean entryHasDeobfuscatedName(Entry deobfEntry) {
		return deobfuscator.hasDeobfuscatedName(deobfuscator.obfuscateEntry(deobfEntry));
	}

	public boolean referenceIsRenameable(EntryReference<Entry, Entry> deobfReference) {
		return deobfuscator.isRenameable(deobfuscator.obfuscateReference(deobfReference), true);
	}

	private void showReference(EntryReference<Entry, Entry> obfReference) {
		EntryReference<Entry, Entry> deobfReference = this.deobfuscator.deobfuscateReference(obfReference);
		Collection<Token> tokens = this.index.getReferenceTokens(deobfReference);
		if (tokens.isEmpty()) {
			// DEBUG
			//			System.err.println(String.format("WARNING: no tokens found for %s in %s", deobfReference, this.currentObfClass));
		} else {
			//			this.gui.showTokens(tokens);
		}
	}

	public void savePreviousReference(EntryReference<Entry, Entry> deobfReference) {
		this.referenceStack.push(this.deobfuscator.obfuscateReference(deobfReference));
	}

	public void openPreviousReference() {
		if (hasPreviousLocation()) {
			//			openReference(this.deobfuscator.deobfuscateReference(this.referenceStack.pop()));
		}
	}

	public boolean hasPreviousLocation() {
		return !this.referenceStack.isEmpty();
	}

	public void refreshClasses() {
		List<ClassEntry> classes = Lists.newArrayList();
		deobfuscator.getSeparatedClasses(classes, classes);
		setClasses(classes);
	}

	public void setClasses(List<ClassEntry> classes) {
		for (ClassEntry classEntry : classes) {
			HierarchyItem parent;
			if (classEntry.getPackageName() == null) {
				parent = createChild((HierarchyItem) classTree.getRoot(), "(none)", HierarchyItemType.PACKAGE);
				parent.setExpanded(false);
			} else {
				String[] splitPackage = classEntry.getPackageName().split("/");
				parent = createChildSeries((HierarchyItem) classTree.getRoot(), splitPackage);
			}

			TreeItem<String> item = createChild(parent, classEntry.getSimpleName(), HierarchyItemType.CLASS);
			classEntryMap.put(item, classEntry);
			parent.getInternalChildren().sort(Comparator.comparing(TreeItem::getValue));
		}
		((HierarchyItem) classTree.getRoot()).getInternalChildren().sort(Comparator.comparing(TreeItem::getValue));
		classTree.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				TreeItem<String> item = classTree.getSelectionModel().getSelectedItem();

				if (classEntryMap.containsKey(item)) {
					openTab(classEntryMap.get(item));
				}

				// Create New Tab
/*				Tab tabdata = new Tab();
				Label tabALabel = new Label("Test");
				tabdata.setGraphic(tabALabel);

				DataStage.addNewTab(tabdata);*/
			}
		});
	}

	public void checkChildren() {

	}

	public HierarchyItem getChild(HierarchyItem parent, String child) {
		for (TreeItem<String> item : parent.getChildren()) {
			if (item.getValue().equals(child)) {
				return (HierarchyItem) item;
			}
		}
		return null;
	}

	public HierarchyItem createChildSeries(HierarchyItem parent, String... children) {
		HierarchyItem[] hierarchy = new HierarchyItem[children.length + 1];
		hierarchy[0] = parent;
		for (int i = 0; i < children.length; i++) {
			hierarchy[i + 1] = createChild(hierarchy[i], children[i], HierarchyItemType.PACKAGE);
		}
		return hierarchy[children.length];
	}

	public HierarchyItem createChild(HierarchyItem parent, String child, HierarchyItemType type) {
		HierarchyItem c = getChild(parent, child);
		if (c == null) {
			ImageView icon = new ImageView(type.icon);
			icon.setFitHeight(type.size);
			icon.setFitWidth(type.size);
			c = new HierarchyItem(child, icon);
			parent.getInternalChildren().add(c);
		}
		return c;
	}

	public HierarchyItem newRoot() {
		return new HierarchyItem("Root");
	}

	public void setMappingsFile(File file) {
		fileChooser.setTitle("Open Jar File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Archive Files", "*.jar"));
		saveMappingsMenu.setDisable(file == null);
	}

	@FXML
	public void openTinyMappings() {

	}

	@FXML
	public void rebuildMethodNames() {

	}

	@FXML
	public void saveEnigmaMappingsAs() {

	}

	@FXML
	public void saveEnigmaMappingsAsDirectory() {

	}

	@FXML
	public void saveMappings() {

	}

	@FXML
	public void saveSRGMappingsAs() {

	}
}

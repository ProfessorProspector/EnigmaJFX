package cuchaz.enigma.gui;

import cuchaz.enigma.mapping.entry.ClassEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class HierarchyItem extends TreeItem<String> {
	private final ObservableList<TreeItem<String>> sourceList;

	private final ObjectProperty<TreeItemPredicate<String>> predicate = new SimpleObjectProperty<>();

	public HierarchyItem(ClassEntry classEntry) {
		this(classEntry.getSimpleName(), HierarchyItemType.CLASS);
	}

	public HierarchyItem(String value, HierarchyItemType type) {
		this(value, type.makeImageView());
	}

	public HierarchyItem(String value) {
		this(value, new ImageView());
	}

	public HierarchyItem(String value, Node graphic) {
		super(value, graphic);
		sourceList = FXCollections.observableArrayList();
		FilteredList<TreeItem<String>> filteredList = new FilteredList<>(sourceList);

		filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
			// Set the predicate of child items to force filtering
			if (child instanceof HierarchyItem) {
				HierarchyItem filterableChild = (HierarchyItem) child;
				filterableChild.setPredicate(predicate.get());
			}
			// If there is no predicate, keep this tree item
			if (predicate.get() == null) {
				return true;
			}
			// If there are children, keep this tree item
			if (!child.getChildren().isEmpty()) {
				return true;
			}
			// Otherwise ask the TreeItemPredicate
			return predicate.get().test(this, child.getValue());
		}, predicate));

		setHiddenFieldChildren(filteredList);
	}

	/**
	 * Set the hidden private field {@link TreeItem#children} through reflection and hook the hidden {@link ListChangeListener} in {@link TreeItem#childrenListener} to the list
	 */
	protected void setHiddenFieldChildren(ObservableList<TreeItem<String>> list) {
		Field children = ReflectionUtils.findField(getClass(), "children");
		children.setAccessible(true);
		ReflectionUtils.setField(children, this, list);

		Field childrenListener1 = ReflectionUtils.findField(getClass(), "childrenListener");
		childrenListener1.setAccessible(true);
		Object childrenListener = ReflectionUtils.getField(childrenListener1, this);

		list.addListener((ListChangeListener<? super TreeItem<String>>) childrenListener);
	}

	/**
	 * Returns the list of children that is backing the filtered list.
	 *
	 * @return underlying list of children
	 */
	public ObservableList<TreeItem<String>> getInternalChildren() {
		return sourceList;
	}

	public final ObjectProperty<TreeItemPredicate<String>> predicateProperty() {
		return predicate;
	}

	public final TreeItemPredicate<String> getPredicate() {
		return predicate.get();
	}

	public final void setPredicate(TreeItemPredicate<String> predicate) {
		this.predicate.set(predicate);
	}
}
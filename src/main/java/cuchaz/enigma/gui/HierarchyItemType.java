package cuchaz.enigma.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public enum HierarchyItemType {
	PACKAGE(new Image("/assets/icons/folder.png"), 16),
	CLASS(new Image("/assets/icons/class.png"), 12);

	public Image icon;
	public int size;

	HierarchyItemType(Image image, int size) {
		this.icon = image;
		this.size = size;
	}

	public ImageView makeImageView() {
		ImageView imageView = new ImageView(icon);
		imageView.setFitHeight(size);
		imageView.setFitWidth(size);
		return imageView;
	}
}

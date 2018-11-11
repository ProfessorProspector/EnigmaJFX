package cuchaz.enigma.bytecode.translators;

import cuchaz.enigma.mapping.Translator;
import cuchaz.enigma.mapping.TypeDescriptor;
import cuchaz.enigma.mapping.entry.ClassEntry;
import cuchaz.enigma.mapping.entry.FieldEntry;
import org.objectweb.asm.AnnotationVisitor;

public class TranslationAnnotationVisitor extends AnnotationVisitor {
	private final Translator translator;
	private final ClassEntry annotationEntry;

	public TranslationAnnotationVisitor(Translator translator, ClassEntry annotationEntry, int api, AnnotationVisitor av) {
		super(api, av);
		this.translator = translator;
		this.annotationEntry = annotationEntry;
	}

	@Override
	public void visit(String name, Object value) {
		super.visit(name, translator.getTranslatedValue(value));
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		return this;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc) {
		TypeDescriptor type = new TypeDescriptor(desc);
		FieldEntry annotationField = translator.getTranslatedField(new FieldEntry(annotationEntry, name, type));
		return super.visitAnnotation(annotationField.getName(), annotationField.getDesc().toString());
	}

	@Override
	public void visitEnum(String name, String desc, String value) {
		TypeDescriptor type = new TypeDescriptor(desc);
		FieldEntry annotationField = translator.getTranslatedField(new FieldEntry(annotationEntry, name, type));
		FieldEntry enumField = translator.getTranslatedField(new FieldEntry(type.getTypeEntry(), value, type));
		super.visitEnum(annotationField.getName(), annotationField.getDesc().toString(), enumField.getName());
	}
}

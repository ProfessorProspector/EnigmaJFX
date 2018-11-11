package cuchaz.enigma.gui;

import cuchaz.enigma.analysis.Token;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeDisplay extends CodeArea {
	private static final String[] KEYWORDS = new String[] {
		"abstract", "assert", "boolean", "break", "byte",
		"case", "catch", "char", "class", "const",
		"continue", "default", "do", "double", "else",
		"enum", "extends", "final", "finally", "float",
		"for", "goto", "if", "implements", "import",
		"instanceof", "int", "interface", "long", "native",
		"new", "package", "private", "protected", "public",
		"return", "short", "static", "strictfp", "super",
		"switch", "synchronized", "this", "throw", "throws",
		"transient", "try", "void", "volatile", "while"
	};

	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
	private static final String PAREN_PATTERN = "\\(|\\)";
	private static final String BRACE_PATTERN = "\\{|\\}";
	private static final String BRACKET_PATTERN = "\\[|\\]";
	private static final String SEMICOLON_PATTERN = "\\;";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String BLOCK_COMMENT_PATTERN = "//[^\n]*";
	private static final String LINE_COMMENT_PATTERN = "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile(
		"(?<KEYWORD>" + KEYWORD_PATTERN + ")"
			+ "|(?<PAREN>" + PAREN_PATTERN + ")"
			+ "|(?<BRACE>" + BRACE_PATTERN + ")"
			+ "|(?<BRACKET>" + BRACKET_PATTERN + ")"
			+ "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
			+ "|(?<STRING>" + STRING_PATTERN + ")"
			+ "|(?<BLOCKCOMMENT>" + BLOCK_COMMENT_PATTERN + ")"
			+ "|(?<LINECOMMENT>" + LINE_COMMENT_PATTERN + ")"
	);

	public CodeDisplay() {
		setParagraphGraphicFactory(LineNumberFactory.get(this));
		setEditable(false);
		setShowCaret(Caret.CaretVisibility.ON);

	}

	public void updateHighlighting() {
		//		setStyleSpans(0, computeHighlighting(getText()));
	}

	private StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder
			= new StyleSpansBuilder<>();
		while (matcher.find()) {
			String styleClass =
				matcher.group("KEYWORD") != null ? "keyword" :
				matcher.group("PAREN") != null ? "paren" :
				matcher.group("BRACE") != null ? "brace" :
				matcher.group("BRACKET") != null ? "bracket" :
				matcher.group("SEMICOLON") != null ? "semicolon" :
				matcher.group("STRING") != null ? "string" :
				matcher.group("BLOCKCOMMENT") != null ? "block-comment" :
				matcher.group("LINECOMMENT") != null ? "line-comment" :
				null;
			//never happens
			assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	public void addHighlights(List<Token> obfuscatedTokens, List<Token> deobfuscatedTokens, List<Token> otherTokens) {
		if (obfuscatedTokens != null) {
			setHighlightStyle(obfuscatedTokens, "obf");
		}
		if (deobfuscatedTokens != null) {
			//			setHighlightStyle(deobfuscatedTokens, "deobf");
		}
		if (otherTokens != null) {
			//			setHighlightStyle(otherTokens, "other");
		}

	}

	public void setHighlightStyle(Iterable<Token> tokens, String style) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		int lastEnd = 0;
		for (Token token : tokens) {
			spansBuilder.add(Collections.emptyList(), token.start - lastEnd);
			spansBuilder.add(Collections.singleton(style),
				((token.end - 1) + 1 - token.start));
			lastEnd = (token.end - 1) + 1;
		}
		spansBuilder.add(Collections.emptyList(), getText().length() - lastEnd);
		setStyleSpans(0, spansBuilder.create());
	}
}

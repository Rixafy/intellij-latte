package com.jantvrdik.intellij.latte.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.jantvrdik.intellij.latte.psi.LatteTypes.*;

/**
 * Adapter for {@link LatteTopLexer} which is generated by JFlex to IntelliJ's interface
 * {@link com.intellij.lexer.Lexer}.
 *
 * Also merges any sequence of selected tokens to a single token.
 */
public class LatteTopLexerAdapter extends MergingLexerAdapter {
	public LatteTopLexerAdapter() {
		super(
			new FlexAdapter(new LatteTopLexer((java.io.Reader) null)),
			TokenSet.create(T_TEXT)
		);
	}
}
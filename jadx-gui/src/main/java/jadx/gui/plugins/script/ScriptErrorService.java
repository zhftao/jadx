package jadx.gui.plugins.script;

import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.script.experimental.api.ScriptDiagnostic;
import kotlin.script.experimental.api.SourceCode;

public class ScriptErrorService extends AbstractParser {
	private static final Logger LOG = LoggerFactory.getLogger(ScriptErrorService.class);

	private final DefaultParseResult result;
	private final ScriptCodeArea scriptArea;

	public ScriptErrorService(ScriptCodeArea scriptArea) {
		this.scriptArea = scriptArea;
		this.result = new DefaultParseResult(this);
	}

	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {
		return result;
	}

	public void clearErrors() {
		result.clearNotices();
		scriptArea.removeParser(this);
	}

	public void apply() {
		scriptArea.removeParser(this);
		scriptArea.addParser(this);
		scriptArea.addNotify();
		scriptArea.requestFocus();
		jumpCaretToFirstError();
	}

	private void jumpCaretToFirstError() {
		List<ParserNotice> parserNotices = result.getNotices();
		if (parserNotices.isEmpty()) {
			return;
		}
		ParserNotice notice = parserNotices.get(0);
		int offset = notice.getOffset();
		if (offset == -1) {
			try {
				offset = scriptArea.getLineStartOffset(notice.getLine());
			} catch (Exception e) {
				LOG.error("Failed to jump to first error", e);
				return;
			}
		}
		scriptArea.scrollToPos(offset);
	}

	public void addCompilerIssues(List<ScriptDiagnostic> issues) {
		for (ScriptDiagnostic issue : issues) {
			if (issue.getSeverity() == ScriptDiagnostic.Severity.DEBUG) {
				continue;
			}
			DefaultParserNotice notice;
			SourceCode.Location loc = issue.getLocation();
			if (loc == null) {
				notice = new DefaultParserNotice(this, issue.getMessage(), 0);
			} else {
				try {
					int line = loc.getStart().getLine();
					int offset = scriptArea.getLineStartOffset(line - 1) + loc.getStart().getCol();
					int len = loc.getEnd() == null ? -1 : loc.getEnd().getCol() - loc.getStart().getCol();
					notice = new DefaultParserNotice(this, issue.getMessage(), line, offset - 1, len);
					notice.setLevel(convertLevel(issue.getSeverity()));
				} catch (Exception e) {
					LOG.error("Failed to convert script issue", e);
					continue;
				}
			}
			addNotice(notice);
		}
	}

	private static ParserNotice.Level convertLevel(ScriptDiagnostic.Severity severity) {
		switch (severity) {
			case FATAL:
			case ERROR:
				return ParserNotice.Level.ERROR;
			case WARNING:
				return ParserNotice.Level.WARNING;
			case INFO:
			case DEBUG:
				return ParserNotice.Level.INFO;
		}
		return ParserNotice.Level.ERROR;
	}

	public void addLintErrors(List<JadxLintError> errors) {
		for (JadxLintError error : errors) {
			try {
				int line = error.getLine();
				int offset = scriptArea.getLineStartOffset(line - 1) + error.getCol() - 1;
				String word = scriptArea.getWordByPosition(offset);
				int len = word != null ? word.length() : -1;
				DefaultParserNotice notice = new DefaultParserNotice(this, error.getDetail(), line, offset, len);
				notice.setLevel(ParserNotice.Level.WARNING);
				addNotice(notice);
			} catch (Exception e) {
				LOG.error("Failed to convert lint error", e);
			}
		}
	}

	private void addNotice(DefaultParserNotice notice) {
		LOG.debug("Add notice: {}:{}:{} - {}",
				notice.getLine(), notice.getOffset(), notice.getLength(), notice.getMessage());
		result.addNotice(notice);
	}
}

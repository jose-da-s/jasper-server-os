/*
 * Copyright (C) 2025 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.AwtTextRenderer;
import net.sf.jasperreports.engine.fill.JRMeasuredText;
import net.sf.jasperreports.engine.fill.JRTextMeasurer;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.MaxFontSizeFinder;

/**
 * Slightly modified Text Measurer to support single line outputs.
 * 
 * @author Jun-Sun Whang
 * @version $Id$
 */
public class SingleLineTextMeasurer implements JRTextMeasurer {

	/**
	 * 
	 */
	private static final FontRenderContext FONT_RENDER_CONTEXT = AwtTextRenderer.LINE_BREAK_FONT_RENDER_CONTEXT;

	private JasperReportsContext jasperReportsContext;
	private JRCommonText textElement;
	private JRPropertiesHolder propertiesHolder;

	/**
	 * 
	 */
	private MaxFontSizeFinder maxFontSizeFinder = null;
	private int width = 0;
	private int height = 0;
	private int topPadding = 0;
	private int leftPadding = 0;
	private int bottomPadding = 0;
	private int rightPadding = 0;
	private float lineSpacing = 0;
	private float formatWidth = 0;
	private int maxHeight = 0;
	private boolean canOverflow;
	private Map globalAttributes;
	private TextMeasuredState measuredState;
	private TextMeasuredState prevMeasuredState;

	protected static class TextMeasuredState implements JRMeasuredText,
			Cloneable {
		protected int textOffset = 0;
		protected int lines = 0;
		protected int fontSizeSum = 0;
		protected int firstLineMaxFontSize = 0;
		protected float textHeight = 0;
		protected float firstLineLeading = 0;
		protected boolean isLeftToRight = true;
		protected boolean isParagraphCut;
		protected String textSuffix = null;
		protected float textWidth;

		public boolean isLeftToRight() {
			return isLeftToRight;
		}

		public int getTextOffset() {
			return textOffset;
		}

		public float getTextHeight() {
			return textHeight;
		}

		public float getLineSpacingFactor() {
			if (lines > 0) {
				return textHeight / fontSizeSum;
			}
			return 0;
		}

		public float getLeadingOffset() {
			return firstLineLeading - firstLineMaxFontSize
					* getLineSpacingFactor();
		}

		public boolean isParagraphCut() {
			return isParagraphCut;
		}

		public String getTextSuffix() {
			return textSuffix;
		}

		public TextMeasuredState cloneState() {
			try {
				return (TextMeasuredState) super.clone();
			} catch (CloneNotSupportedException e) {
				// never
				throw new JRRuntimeException(e);
			}
		}

		public short[] getLineBreakOffsets() {
			// no line breaks
			return null;
		}

		public void setTextWidth(float textWidth) {
			this.textWidth = textWidth;
		}

		@Override
		public float getTextWidth() {
			return textWidth;
		}

		@Override
		public float getAverageCharWidth() {
			// Calculate average char width based on text width and text offset
			if (textOffset > 0) {
				return textWidth / textOffset;
			}
			return 0;
		}
	}

	/**
	 * 
	 */
	public SingleLineTextMeasurer(JasperReportsContext jasperReportsContext, JRCommonText textElement) {
		this.jasperReportsContext = jasperReportsContext;
		this.textElement = textElement;
		this.propertiesHolder = textElement instanceof JRPropertiesHolder ? (JRPropertiesHolder) textElement
				: null;
	}

	/**
	 * 
	 */
	protected void initialize(JRStyledText styledText,
			int availableStretchHeight, boolean indentFirstLine, boolean canOverflow) {
		width = textElement.getWidth();
		height = textElement.getHeight();

		topPadding = textElement.getLineBox().getTopPadding().intValue();
		leftPadding = textElement.getLineBox().getLeftPadding().intValue();
		bottomPadding = textElement.getLineBox().getBottomPadding().intValue();
		rightPadding = textElement.getLineBox().getRightPadding().intValue();

		switch (textElement.getRotation()) {
		case LEFT: {
			width = textElement.getHeight();
			height = textElement.getWidth();
			int tmpPadding = topPadding;
			topPadding = leftPadding;
			leftPadding = bottomPadding;
			bottomPadding = rightPadding;
			rightPadding = tmpPadding;
			break;
		}
		case RIGHT: {
			width = textElement.getHeight();
			height = textElement.getWidth();
			int tmpPadding = topPadding;
			topPadding = rightPadding;
			rightPadding = bottomPadding;
			bottomPadding = leftPadding;
			leftPadding = tmpPadding;
			break;
		}
		case UPSIDE_DOWN: {
			int tmpPadding = topPadding;
			topPadding = bottomPadding;
			bottomPadding = tmpPadding;
			tmpPadding = leftPadding;
			leftPadding = rightPadding;
			rightPadding = tmpPadding;
			break;
		}
		case NONE:
		default: {
		}
		}

		/*   */
		switch (textElement.getParagraph().getLineSpacing()) {
		case SINGLE: {
			lineSpacing = 1f;
			break;
		}
		case ONE_AND_HALF: {
			lineSpacing = 1.5f;
			break;
		}
		case DOUBLE: {
			lineSpacing = 2f;
			break;
		}
		default: {
			lineSpacing = 1f;
		}
		}

		maxFontSizeFinder = MaxFontSizeFinder.getInstance(!JRCommonText.MARKUP_NONE.equals(textElement.getMarkup()));

		formatWidth = width - leftPadding - rightPadding;
		formatWidth = formatWidth < 0 ? 0 : formatWidth;
		maxHeight = height + availableStretchHeight - topPadding
				- bottomPadding;
		maxHeight = maxHeight < 0 ? 0 : maxHeight;
		this.canOverflow = canOverflow;
		this.globalAttributes = styledText.getGlobalAttributes();
		measuredState = new TextMeasuredState();
		measuredState.setTextWidth(width);//FIXME is this fine?

		prevMeasuredState = null;
	}

	/**
	 * 
	 */
	public JRMeasuredText measure(JRStyledText styledText,
			int remainingTextStart, int availableStretchHeight,
			boolean indentFirstLine, boolean canOverflow) {
		/*   */
		initialize(styledText, availableStretchHeight, indentFirstLine, canOverflow);

		AttributedCharacterIterator allParagraphs = styledText
				.getAttributedString().getIterator();

		int tokenPosition = remainingTextStart;
		int lastParagraphStart = remainingTextStart;
		String lastParagraphText = null;

		String remainingText = styledText.getText().substring(
				remainingTextStart);
		StringTokenizer tkzer = new StringTokenizer(remainingText, "\n", true);

		boolean rendered = true;
		while (tkzer.hasMoreTokens() && rendered) {
			String token = tkzer.nextToken();

			if ("\n".equals(token)) {
				rendered = renderParagraph(allParagraphs, lastParagraphStart,
						lastParagraphText);

				lastParagraphStart = tokenPosition
						+ (tkzer.hasMoreTokens() || tokenPosition == 0 ? 1 : 0);
				lastParagraphText = null;
			} else {
				lastParagraphStart = tokenPosition;
				lastParagraphText = token;
			}

			tokenPosition += token.length();
		}

		if (rendered
				&& lastParagraphStart < remainingTextStart
						+ remainingText.length()) {
			renderParagraph(allParagraphs, lastParagraphStart,
					lastParagraphText);
		}

		return measuredState;
	}

	private boolean renderParagraph(AttributedCharacterIterator allParagraphs,
			int lastParagraphStart, String lastParagraphText) {
		// compose key
		Set paragraphAttribs = allParagraphs.getAllAttributeKeys();
		Map AttribValues = allParagraphs.getAttributes();
		String keyString = "";
		for (Iterator it = paragraphAttribs.iterator(); it.hasNext();) {
			Object key = (Object) it.next();
			keyString += String.valueOf(AttribValues.get(key));
		}

		StyleInfo si = StyleInfoPool.getPoolElement(keyString);
		if (si == null) { // call original method and populate pool.
			boolean rendered = renderParagraphInner(allParagraphs,
					lastParagraphStart, lastParagraphText);
			StyleInfo nsi = new StyleInfo();
			nsi.setFirstLineLeading(measuredState.firstLineLeading);
			nsi.setFirstLineMaxFontSize(measuredState.firstLineMaxFontSize);
			nsi.setFontSizeSum(measuredState.fontSizeSum);
			nsi.setLines(1);
			nsi.setTextHeight(measuredState.textHeight);
			nsi.setLeftToRight(measuredState.isLeftToRight);
			StyleInfoPool.putPoolElement(keyString, nsi);
			System.out.println("key added: " + keyString);
			return rendered;
		} else { // use cached value.
			measuredState.firstLineLeading = si.getFirstLineLeading();
			measuredState.firstLineMaxFontSize = si.getFirstLineMaxFontSize();
			measuredState.fontSizeSum = si.getFontSizeSum();
			measuredState.lines = si.getLines();
			measuredState.textHeight = si.getTextHeight();
			measuredState.textOffset = lastParagraphStart + (lastParagraphText == null ? 1 : lastParagraphText.length());
			measuredState.isLeftToRight = si.isLeftToRight();
			return measuredState.textHeight <= maxHeight;
		}
	}

	/**
	 * 
	 */
	protected boolean renderParagraphInner(
			AttributedCharacterIterator allParagraphs, int lastParagraphStart,
			String lastParagraphText) {
		AttributedCharacterIterator paragraph = null;

		if (lastParagraphText == null) {
			paragraph = new AttributedString(" ", new AttributedString(
					allParagraphs, lastParagraphStart, lastParagraphStart + 1)
					.getIterator().getAttributes()).getIterator();
		} else {
			paragraph = new AttributedString(allParagraphs, lastParagraphStart,
					lastParagraphStart + lastParagraphText.length())
					.getIterator();
		}

		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph,
				FONT_RENDER_CONTEXT);

		measuredState.textOffset = lastParagraphStart;

		boolean rendered = true;
		boolean renderedLine = false;
		while (lineMeasurer.getPosition() < paragraph.getEndIndex() && rendered) {
			rendered = renderNextLine(lineMeasurer, paragraph);
			renderedLine = renderedLine || rendered;
		}

		// if we rendered at least one line, and the last line didn't fit
		// and the text does not overflow
		if (!rendered && prevMeasuredState != null && !canOverflow) {
			// handle last rendered row
			processLastTruncatedRow(allParagraphs, lastParagraphText,
					lastParagraphStart, renderedLine);
		}

		return rendered;
	}

	protected void processLastTruncatedRow(
			AttributedCharacterIterator allParagraphs, String paragraphText,
			int paragraphOffset, boolean lineTruncated) {
		if (lineTruncated && isToTruncateAtChar()) {
			truncateLastLineAtChar(allParagraphs, paragraphText,
					paragraphOffset);
		}

		appendTruncateSuffix(allParagraphs);
	}

	protected void truncateLastLineAtChar(
			AttributedCharacterIterator allParagraphs, String paragraphText,
			int paragraphOffset) {
		// truncate the original line at char
		measuredState = prevMeasuredState.cloneState();
		AttributedCharacterIterator lineParagraph = new AttributedString(
				allParagraphs, measuredState.textOffset, paragraphOffset
						+ paragraphText.length()).getIterator();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(lineParagraph,
				BreakIterator.getCharacterInstance(), FONT_RENDER_CONTEXT);
		// render again the last line
		// if the line does not fit now, it will remain empty
		renderNextLine(lineMeasurer, lineParagraph);
	}

	protected void appendTruncateSuffix(
			AttributedCharacterIterator allParagraphs) {
		String truncateSuffx = getTruncateSuffix();
		if (truncateSuffx == null) {
			return;
		}

		int lineStart = prevMeasuredState.textOffset;

		// advance from the line start until the next line start or the first
		// newline
		StringBuffer lineText = new StringBuffer();
		allParagraphs.setIndex(lineStart);
		while (allParagraphs.getIndex() < measuredState.textOffset
				&& allParagraphs.current() != '\n') {
			lineText.append(allParagraphs.current());
			allParagraphs.next();
		}
		int linePosition = allParagraphs.getIndex() - lineStart;

		// iterate to the beginning of the line
		boolean done = false;
		do {
			measuredState = prevMeasuredState.cloneState();

			String text = lineText.substring(0, linePosition) + truncateSuffx;
			AttributedString attributedText = new AttributedString(text);

			// set original attributes for the text part
			AttributedCharacterIterator lineAttributes = new AttributedString(
					allParagraphs, measuredState.textOffset,
					measuredState.textOffset + linePosition).getIterator();
			setAttributes(attributedText, lineAttributes, 0);

			// set global attributes for the suffix part
			setAttributes(attributedText, globalAttributes, text.length()
					- truncateSuffx.length(), text.length());

			AttributedCharacterIterator lineParagraph = attributedText
					.getIterator();

			BreakIterator breakIterator = isToTruncateAtChar() ? BreakIterator
					.getCharacterInstance() : BreakIterator.getLineInstance();
			LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(
					lineParagraph, breakIterator, FONT_RENDER_CONTEXT);

			if (renderNextLine(lineMeasurer, lineParagraph)) {
				int lastPos = lineMeasurer.getPosition();
				// test if the entire suffix fit
				if (lastPos == linePosition + truncateSuffx.length()) {
					// subtract the suffix from the offset
					measuredState.textOffset -= truncateSuffx.length();
					measuredState.textSuffix = truncateSuffx;
					done = true;
				} else {
					linePosition = breakIterator.preceding(linePosition);
					if (linePosition == BreakIterator.DONE) {
						// if the text suffix did not fit the line, only the
						// part of it that fits will show

						// truncate the suffix
						String actualSuffix = truncateSuffx.substring(0,
								measuredState.textOffset
										- prevMeasuredState.textOffset);
						// if the last text char is not a new line
						if (prevMeasuredState.textOffset > 0
								&& allParagraphs
										.setIndex(prevMeasuredState.textOffset - 1) != '\n') {
							// force a new line so that the suffix is displayed
							// on the last line
							actualSuffix = '\n' + actualSuffix;
						}
						measuredState.textSuffix = actualSuffix;

						// restore the next to last line offset
						measuredState.textOffset = prevMeasuredState.textOffset;

						done = true;
					}
				}
			} else {
				// if the line did not fit, leave it empty
				done = true;
			}
		} while (!done);
	}

	protected boolean isToTruncateAtChar() {
		//FIXME do not read each time
		return JRPropertiesUtil.getInstance(jasperReportsContext).getBooleanProperty(propertiesHolder, 
				JRTextElement.PROPERTY_TRUNCATE_AT_CHAR, false);
	}

	protected String getTruncateSuffix() {
		//FIXME do not read each time
		String truncateSuffx = JRPropertiesUtil.getInstance(jasperReportsContext).getProperty(propertiesHolder,
				JRTextElement.PROPERTY_TRUNCATE_SUFFIX);
		if (truncateSuffx != null) {
			truncateSuffx = truncateSuffx.trim();
		}
		if (truncateSuffx.length() == 0) {
			truncateSuffx = null;
		}
		return truncateSuffx;
	}

	protected boolean renderNextLine(LineBreakMeasurer lineMeasurer,
			AttributedCharacterIterator paragraph) {
		int lineStartPosition = lineMeasurer.getPosition();

		TextLayout layout = lineMeasurer.nextLayout(formatWidth);

		float newTextHeight = measuredState.textHeight + layout.getLeading()
				+ lineSpacing * layout.getAscent();
		boolean fits = newTextHeight + layout.getDescent() <= maxHeight;
		if (fits) {
			prevMeasuredState = measuredState.cloneState();

			measuredState.isLeftToRight = measuredState.isLeftToRight
					&& layout.isLeftToRight();
			measuredState.textHeight = newTextHeight;
			measuredState.lines++;

			measuredState.fontSizeSum += maxFontSizeFinder.findMaxFontSize(
					new AttributedString(paragraph, lineStartPosition,
							lineStartPosition + layout.getCharacterCount())
							.getIterator(), textElement.getFontSize());

			if (measuredState.lines == 1) {
				measuredState.firstLineLeading = measuredState.textHeight;
				measuredState.firstLineMaxFontSize = measuredState.fontSizeSum;
			}

			// here is the Y offset where we would draw the line
			// lastDrawPosY = drawPosY;
			//
			measuredState.textHeight += layout.getDescent();

			measuredState.textOffset += lineMeasurer.getPosition()
					- lineStartPosition;
		}

		return fits;
	}

	protected JRPropertiesHolder getTextPropertiesHolder() {
		return propertiesHolder;
	}

	protected void setAttributes(AttributedString string,
			AttributedCharacterIterator attributes, int stringOffset) {
		for (char c = attributes.first(); c != CharacterIterator.DONE; c = attributes
				.next()) {
			for (Iterator it = attributes.getAttributes().entrySet().iterator(); it
					.hasNext();) {
				Map.Entry attributeEntry = (Map.Entry) it.next();
				AttributedCharacterIterator.Attribute attribute = (Attribute) attributeEntry
						.getKey();
				if (attributes.getRunStart(attribute) == attributes.getIndex()) {
					Object attributeValue = attributeEntry.getValue();
					string.addAttribute(attribute, attributeValue, attributes
							.getIndex()
							+ stringOffset, attributes.getRunLimit(attribute)
							+ stringOffset);
				}
			}
		}
	}

	protected void setAttributes(AttributedString string, Map attributes,
			int startIndex, int endIndex) {
		for (Iterator it = attributes.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			AttributedCharacterIterator.Attribute attribute = (Attribute) entry
					.getKey();
			Object attributeValue = entry.getValue();
			string
					.addAttribute(attribute, attributeValue, startIndex,
							endIndex);
		}
	}
}

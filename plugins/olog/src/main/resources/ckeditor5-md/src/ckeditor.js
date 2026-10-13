/**
 * @license Copyright (c) 2014-2022, CKSource Holding sp. z o.o. All rights reserved.
 * For licensing, see LICENSE.md or https://ckeditor.com/legal/ckeditor-oss-license
 */
import {
	ClassicEditor,
	Autoformat,
	BlockQuote,
	Bold,
	Code,
	CodeBlock,
	Essentials,
	Heading,
	HorizontalLine,
	Image,
	ImageToolbar,
	Italic,
	Link,
	List,
	Markdown,
	Paragraph,
	PasteFromOffice,
	SourceEditing,
	Table,
	TableToolbar
} from 'ckeditor5';

import 'ckeditor5/ckeditor5.css';

// GitHub flavoured Markdown tables only hold inline content.
function DisableIncompatibleInTables(editor) {
	const disabledInTable = ['table', 'blockQuote', 'softBreak', 'codeBlock', 'horizontalLine'];
	const schema = editor.model.schema;

	schema.addChildCheck((context, childDefinition) => {
		if (context.endsWith('tableCell') || Array.from(context.getNames()).includes('table')) {
			if (disabledInTable.includes(childDefinition.name)) {
				return false;
			}
		}
	});

	// List items are paragraphs with list attributes.
	schema.addAttributeCheck((context, attributeName) => {
		if (attributeName.startsWith('list') && Array.from(context.getNames()).includes('table')) {
			return false;
		}
	});
}

ClassicEditor
	.create(document.getElementById('editor'), {
		licenseKey: 'GPL',
		plugins: [
			Autoformat,
			BlockQuote,
			Bold,
			Code,
			CodeBlock,
			DisableIncompatibleInTables,
			Essentials,
			Heading,
			HorizontalLine,
			Image,
			ImageToolbar,
			Italic,
			Link,
			List,
			Markdown,
			Paragraph,
			PasteFromOffice,
			SourceEditing,
			Table,
			TableToolbar
		],
		toolbar: {
			items: [
				'sourceEditing',
				'|',
				'heading',
				'|',
				'bold',
				'italic',
				'bulletedList',
				'numberedList',
				'|',
				'horizontalLine',
				'link',
				'blockQuote',
				'insertTable',
				'code',
				'codeBlock',
				'|',
				'undo',
				'redo'
			]
		},
		language: 'en',
		image: {
			toolbar: [
				'imageTextAlternative'
			]
		},
		table: {
			contentToolbar: [
				'tableColumn',
				'tableRow'
			],
			defaultHeadings: {
				rows: 1
			}
		}
	})
	.then(editor => {
		window.CKEDITOR = editor;
		editor.sourceElement.nextElementSibling.setAttribute("id", 'fullscreeneditor');
		document.body.setAttribute("id", "fullscreenoverlay");
	})
	.catch(error => {
		console.error(error);
	});

/**
 * @license Copyright (c) 2014-2022, CKSource Holding sp. z o.o. All rights reserved.
 * For licensing, see LICENSE.md or https://ckeditor.com/legal/ckeditor-oss-license
 */
import ClassicEditor from '@ckeditor/ckeditor5-editor-classic/src/classiceditor.js';
import Autoformat from '@ckeditor/ckeditor5-autoformat/src/autoformat.js';
import BlockQuote from '@ckeditor/ckeditor5-block-quote/src/blockquote.js';
import Bold from '@ckeditor/ckeditor5-basic-styles/src/bold.js';
import Code from '@ckeditor/ckeditor5-basic-styles/src/code.js';
import CodeBlock from '@ckeditor/ckeditor5-code-block/src/codeblock.js';
import Essentials from '@ckeditor/ckeditor5-essentials/src/essentials.js';
import Heading from '@ckeditor/ckeditor5-heading/src/heading.js';
import HorizontalLine from '@ckeditor/ckeditor5-horizontal-line/src/horizontalline.js';
import Image from '@ckeditor/ckeditor5-image/src/image.js';
import ImageToolbar from '@ckeditor/ckeditor5-image/src/imagetoolbar.js';
import Italic from '@ckeditor/ckeditor5-basic-styles/src/italic.js';
import Link from '@ckeditor/ckeditor5-link/src/link.js';
import List from '@ckeditor/ckeditor5-list/src/list.js';
import Markdown from '@ckeditor/ckeditor5-markdown-gfm/src/markdown.js';
import Paragraph from '@ckeditor/ckeditor5-paragraph/src/paragraph.js';
import PasteFromOffice from '@ckeditor/ckeditor5-paste-from-office/src/pastefromoffice.js';
import SourceEditing from '@ckeditor/ckeditor5-source-editing/src/sourceediting.js';
import Table from '@ckeditor/ckeditor5-table/src/table.js';
import TableToolbar from '@ckeditor/ckeditor5-table/src/tabletoolbar.js';

function DisableIncompatibleInTables(editor) {
	const disabledInTable = ['table', 'blockQuote', 'listItem', 'softBreak', 'codeBlock', 'horizontalLine'];
	editor.model.schema.addChildCheck((context, childDefinition) => {
		if (Array.from(context.getNames()).includes('table')) {
			if (disabledInTable.includes(childDefinition.name)) {
				return false;
			}
		}
	});
}

class Editor extends ClassicEditor { }

// Plugins to include in the build.
Editor.builtinPlugins = [
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
];

// Editor configuration.
Editor.defaultConfig = {
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
};

Editor
	.create(document.getElementById('editor'))
	.then(editor => {
		editor.sourceElement.nextElementSibling.setAttribute("id", 'fullscreeneditor');
		document.body.setAttribute("id", "fullscreenoverlay");
	})
	.catch(error => {
		console.error(error);
	});

export default Editor;


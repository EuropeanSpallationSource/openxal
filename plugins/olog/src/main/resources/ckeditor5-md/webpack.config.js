'use strict';

/* eslint-env node */

const path = require('path');

// The bundle is inlined in a <script> element by OlogPostEntryController, so it must be a single classic script with
// the styles included.
module.exports = {
	devtool: false,
	performance: { hints: false },

	entry: path.resolve(__dirname, 'src', 'ckeditor.js'),

	output: {
		path: path.resolve(__dirname, 'build'),
		filename: 'ckeditor.js',
		iife: true,
		clean: true
	},

	module: {
		rules: [
			{
				test: /\.css$/,
				use: [
					{
						loader: 'style-loader',
						options: {
							injectType: 'singletonStyleTag',
							attributes: {
								'data-cke': true
							}
						}
					},
					'css-loader'
				]
			}
		]
	}
};

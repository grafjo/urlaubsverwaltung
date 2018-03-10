var packageJSON = require('../../../package.json');
var path = require('path');
var webpack = require('webpack');

const PATHS = {
    build: path.join(__dirname, '../../../target/classes/static/lib')
};

module.exports = {
    entry: {
        head: './src/head-bundle.js',
        login: './src/login-bundle.js'
    },
    output: {
        filename: '[name].js',
        path: PATHS.build
    },

    module: {
        rules: [
            { parser: { amd: false } }
        ],
        loaders: [
            { test: /\.js$/, loader: 'babel-loader', exclude: /node_modules/ },
            { test: /\.jsx$/, loader: 'babel-loader', exclude: /node_modules/ }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx'],
        modules: ['node_modules','src/main/webapp']
    },

    plugins: [
        new webpack.ProvidePlugin({
            jQuery: 'jquery',
            $: 'jquery',
            jquery: 'jquery'
        })
    ]

};
var packageJSON = require('./package.json');
var path = require('path');
var webpack = require('webpack');

console.log(__dirname);
console.log(packageJSON.name);
console.log(packageJSON.version);

const PATHS = {
    build: path.join(__dirname, 'target', 'classes', 'META-INF', 'resources', 'webjars', packageJSON.name, packageJSON.version)
};

module.exports = {
    entry: './src/index.js',

    output: {
        path: PATHS.build,
        filename: 'app-bundle.js'
    },
    module: {
        loaders: [
            { test: /\.js$/, loader: 'babel-loader', exclude: /node_modules/ },
            { test: /\.jsx$/, loader: 'babel-loader', exclude: /node_modules/ }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx'],
        modules: ['node_modules','src/main/webapp']
    }
};
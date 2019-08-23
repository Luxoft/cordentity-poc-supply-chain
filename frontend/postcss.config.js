module.exports = {
    modules: true,
    plugins: {
        'postcss-modules': {
            camelCase: true,
            globalModulePaths: ['/static/index.css']
        }
    }
};

const path = require('path')

/**
 * Determine the array of extensions that should be used to resolve modules.
 */
module.exports = {
  alias: {
    Components: path.resolve(__dirname, 'src/components/'),
    Navigation: path.resolve(__dirname, 'src/navigation/'),
    Classes: path.resolve(__dirname, 'src/classes/'),
    Services: path.resolve(__dirname, 'src/services/'),
    Routes: path.resolve(__dirname, 'src/routes/'),
    Redux: path.resolve(__dirname, 'src/redux/'),
    Theme: path.resolve(__dirname, 'src/theme/'),
    Data: path.resolve(__dirname, 'src/data/'),
    Assets: path.resolve(__dirname, 'src/assets/'),
  },
  extensions: ['.js', '.jsx', '.css', '.png', '.jpg', '.gif', '.jpeg'],
}

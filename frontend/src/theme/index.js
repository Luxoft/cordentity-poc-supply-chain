// Importing this requires changing a value in the font imports in
// @salesforce-ux/design-system/_config.scss:
// - 30: $static-font-path: '../fonts/webfonts' !default;
// + 30: $static-font-path: '../assets/fonts/webfonts' !default;
// import LightningBaseStyles from '@salesforce-ux/design-system/scss/index.scss'

import LightningBaseStyles from '@salesforce-ux/design-system/assets/styles/salesforce-lightning-design-system.css'

import theme from './theme'

export { theme }

export default { LightningBaseStyles }

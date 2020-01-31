import fontSizes from './definitions/typography'
import palette from './definitions/palette'

const BASE = 8

const UNITS = {
  halfBase: BASE / 2,
  base: BASE,
  xs: BASE * 1.5,
  sm: BASE * 2,
  md: BASE * 3,
  lg: BASE * 4,
  xl: BASE * 5,
}

const theme = {
  /**
   * I don't know that all these values are necessary,
   * but I'm trying to be explicit in assigning gutters & margins
   * only where actually used by the gutters (intra-column spacing) or
   * margins (page padding). Otherwise just use the agnostic values.
   */
  units: {
    ...UNITS,
  },
  // Gutters and margins mirror the unit structure...
  // kept separate for easier understanding inline.
  gutters: {
    ...UNITS,
  },
  margins: {
    ...UNITS,
  },
  breakpoints: {
    forTabletLandscapeUp: '@media (min-width: 0px)',
    forDesktopUp: '@media (min-width: 1200px)',
    forBigDesktopUp: '@media (min-width: 1400px)',
  },
  withBreakpoints: {
    // defined below to reuse theme variables
  },
  palette: {
    ...palette,
  },
  typography: {
    sizes: {
      ...fontSizes,
    },
  },
  borders: {
    standard: `1px solid ${palette.grays.gray5}`,
  },
}

theme.table = {
  border: theme.borders.standard,
  borderCollapse: 'collapse',
}

theme.typography.styles = {
  largestMessage: {
    ...theme.typography.sizes.size11,
  },
  largeMessage: {
    ...theme.typography.sizes.size8,
  },
  pageTitle: {
    ...theme.typography.sizes.size6,
  },
  sectionTitle: {
    ...theme.typography.sizes.size5,
  },
  sectionSubTitle: {
    ...theme.typography.sizes.size4,
  },
  body: {
    ...theme.typography.sizes.size3,
  },
  label: {
    ...theme.typography.sizes.size2,
  },
  footnote: {
    ...theme.typography.sizes.size1,
  },
}

/**
 * This is an alignment helper.
 *
 * It pulls the inner content outwards to align it with the margin
 */
theme.withBreakpoints.guttersNegativeMargin = {
  [theme.breakpoints.forTabletLandscapeUp]: {
    marginLeft: `-${theme.gutters.xs}px`,
    marginRight: `-${theme.gutters.xs}px`,
  },
  [theme.breakpoints.forDesktopUp]: {
    marginLeft: `-${theme.gutters.sm}px`,
    marginRight: `-${theme.gutters.sm}px`,
  },
  [theme.breakpoints.forBigDesktopUp]: {
    marginLeft: `-${theme.gutters.md}px`,
    marginRight: `-${theme.gutters.md}px`,
  },
}

/**
 * This sets intra-column padding (space between columns)
 */
theme.withBreakpoints.guttersColumnPadding = {
  [theme.breakpoints.forTabletLandscapeUp]: {
    paddingLeft: `${theme.gutters.xs / 2}px`,
    paddingRight: `${theme.gutters.xs / 2}px`,
  },
  [theme.breakpoints.forDesktopUp]: {
    paddingLeft: `${theme.gutters.sm / 2}px`,
    paddingRight: `${theme.gutters.sm / 2}px`,
  },
  [theme.breakpoints.forBigDesktopUp]: {
    paddingLeft: `${theme.gutters.md / 2}px`,
    paddingRight: `${theme.gutters.md / 2}px`,
  },
}

/**
 * This sets the margin on the main outer page content wrapper.
 */
theme.withBreakpoints.pageMargins = {
  [theme.breakpoints.forTabletLandscapeUp]: {
    paddingLeft: `${theme.margins.sm}px`,
    paddingRight: `${theme.margins.sm}px`,
    '&.slds-p-horizontal_large': {
      paddingBottom: `${theme.margins.sm}px`,
    },
  },
  [theme.breakpoints.forDesktopUp]: {
    paddingLeft: `${theme.margins.md}px`,
    paddingRight: `${theme.margins.md}px`,
    '&.slds-p-horizontal_large': {
      paddingBottom: `${theme.margins.md}px`,
    },
  },
  [theme.breakpoints.forBigDesktopUp]: {
    paddingLeft: `${theme.margins.md}px`,
    paddingRight: `${theme.margins.md}px`,
    '&.slds-p-horizontal_large': {
      paddingBottom: `${theme.margins.md}px`,
    },
  },
}

/**
 * This is an alignment helper.
 *
 * It is used on interior elements to make them
 * match the width of their parent slds-col container.
 *
 * Used, for example, on ::after pseudo element in LUMCardGroup
 */
theme.withBreakpoints.matchHorizontalPageMargin = {
  [theme.breakpoints.forTabletLandscapeUp]: {
    marginLeft: `${theme.gutters.xs / 2}px`,
    marginRight: `${theme.gutters.xs / 2}px`,
  },
  [theme.breakpoints.forDesktopUp]: {
    marginLeft: `${theme.gutters.sm / 2}px`,
    marginRight: `${theme.gutters.sm / 2}px`,
  },
  [theme.breakpoints.forBigDesktopUp]: {
    marginLeft: `${theme.gutters.md / 2}px`,
    marginRight: `${theme.gutters.md / 2}px`,
  },
}

export default theme

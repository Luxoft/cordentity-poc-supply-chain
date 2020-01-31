import withStyles from 'react-jss'

export const styles = (theme) => ({
  '@global': {
    '.print-display': {
      display: 'none',
    },
    '@media print': {
      body: {
        '-webkit-print-color-adjust': 'exact !important',
        'color-adjust': 'exact !important',
      },
      '.no-print-display': {
        display: 'none',
      },
      '.print-display': {
        display: 'inherit',
      },
    },
    '::placeholder': {
      /* Chrome, Firefox, Opera, Safari 10.1+ */
      color: theme.palette.grays.gray9,
      opacity: 0.4, /* Firefox */
    },
    ':-ms-input-placeholder': {
      /* Internet Explorer 10-11 */
      color: theme.palette.grays.gray5,
    },
    '::-ms-input-placeholder': {
      /* Microsoft Edge */
      color: theme.palette.grays.gray9,
      opacity: 0.4,
    },
    html: {
      // This is actually set in the index.html file to override the default SLDS color.
      backgroundColor: theme.palette.brand.background,
      color: theme.palette.grays.gray11,
      fontFamily: 'Lato',
    },
    a: {
      color: theme.palette.brand.dark,
      '&:focus': {
        color: theme.palette.brand.darker,
      },
      '&:hover': {
        color: theme.palette.brand.darker,
      },
    },
    '.lum-body': {
      overflow: 'hidden',
    },
    '.lum-global-header__logo': {
      flex: 5,
    },
    '.lum-global-header__nav': {
      flex: 10,
      justifyContent: 'flex-end',
    },
    '.lum-global-header__right': {
      alignItems: 'center',
      flex: 1,
      justifyContent: 'flex-end',
      '& ul': {
        alignItems: 'center',
        height: '100%',
        '& #header-profile-popover-id-dialog-body a': {
          lineHeight: `${theme.units.md}px`,
        },
        '& > li': {
          height: '100%',
        },
      },
    },
    '.lum-global-header__icon-link': {
      height: '100%',
      justifyContent: 'center',
      padding: '0',
      width: `${theme.units.base * 7}px`,
      '&.slds-is-active:not(:hover):after': {
        backgroundColor: '#fff',
        bottom: 0,
        content: '""',
        height: '4px',
        left: 0,
        position: 'absolute',
        right: 0,
      },
      '&:hover': {
        backgroundColor: theme.palette.brand.primary,
      },
    },
    '.lum-stack-chart': {
      backgroundColor: theme.palette.white,
      padding: `${theme.units.md}px ${theme.units.md}px ${theme.units.lg}px`,
    },
    '.lum-stack-chart svg': {
      overflow: 'visible !important',
      width: '100%',
    },
    '.lum-stack-chart svg .axis line, .lum-stack-chart svg .x.axis path, .lum-stack-chart svg .y-grid line': {
      fill: 'none',
      stroke: theme.palette.grays.gray4,
      shapeRendering: 'crispEdges',
    },
    '.lum-stack-chart svg .y-grid line': {
      opacity: 1,
      strokeDasharray: '2,2',
    },
    '.lum-stack-chart svg .axis text': {
      fontFamily: 'Lato',
      fontSize: '11px',
      fontStyle: 'normal',
      fontWeight: 'normal',
      lineHeight: '15px',
      textTransform: 'uppercase',
    },
    '.lum-stack-chart svg .x.axis text': {
      fill: theme.palette.brand.dark,
    },
    '.lum-stack-chart svg .y.axis text': {
      fill: theme.palette.grays.gray9,
    },
    '.lum-stack-chart svg .y-grid .tick': {
      opacity: 1,
      stroke: theme.palette.grays.gray4,
    },
    '.lum-stack-chart svg .axis line, .lum-stack-chart svg .y-grid path, .lum-stack-chart svg .y.axis path': {
      opacity: 0,
    },
    // Remove chunky iframe border from PowerBI reports
    '.report iframe': {
      border: 'none',
    },
  },
})

export const GlobalStyles = function GlobalStyles() {
  return null
}

export default withStyles(styles)(GlobalStyles)

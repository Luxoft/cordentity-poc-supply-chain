import withStyles from 'react-jss'
import { HEADER_HEIGHT } from 'Data/constants'
import sldsButtonOverride from './slds/slds-button-override'
import sldsModalOverride from './slds/slds-modal-override'
import sldsRadioOverride from './slds/slds-radio-override'

export const styles = (theme) => ({
  '@global': {
    '.slds-nav-vertical': {
      overflow: 'hidden',
      padding: '12px 0',
      whiteSpace: 'nowrap',
    },
    '.slds-nav-vertical__title': {
      ...theme.typography.styles.label,
      padding: `${theme.units.base}px ${theme.units.sm}px`,
      textTransform: 'uppercase',
    },
    '.slds-nav-vertical__item .slds-nav-vertical__action': {
      ...theme.typography.styles.label,
      color: theme.palette.grays.gray9,
      fontWeight: 'normal',
      padding: `${theme.units.base}px ${theme.units.sm}px`,
    },
    '.slds-nav-vertical__item.slds-is-active .slds-nav-vertical__action': {
      boxShadow: `inset 0 0 0 ${theme.palette.brand.primary}`,
      color: theme.palette.brand.darker,
      fontWeight: 'normal',
    },
    '.slds-nav-vertical__item:hover:before': {
      background: theme.palette.grays.gray3,
    },
    '.slds-nav-vertical__item.slds-is-active:before': {
      background: theme.palette.brand.lighter,
    },
    '.slds-nav-vertical__item .slds-nav-vertical__action, .slds-nav-vertical__item .slds-nav-vertical__action:hover': {
      boxShadow: `inset 0 0 0 ${theme.palette.brand.primary}`,
    },
    '.slds-context-bar': {
      ...theme.withBreakpoints.pageMargins,
      backgroundColor: theme.palette.black,
      borderBottom: 'none',
      height: HEADER_HEIGHT,
    },
    '.slds-context-bar__item': {
      alignItems: 'center',
    },
    '.slds-context-bar__item:not(.slds-no-hover):hover:after': {
      background: 'unset',
      content: 'unset',
      mixBlendMode: 'unset',
    },
    '.slds-context-bar__item.lum-global-header-title': {
      paddingLeft: `${theme.units.base}px`,
    },
    '.slds-context-bar__label-action': {
      paddingLeft: 0,
    },
    '.slds-context-bar__label-title': {
      ...theme.typography.styles.pageTitle,
      color: theme.palette.white,
    },
    '.slds-avatar__initials': {
      color: '#181818',
      textShadow: 'none',
      '&:hover': {
        color: '#181818',
        cursor: 'pointer',
      },
    },
    /* Override avatar background color on hover */
    '.slds-global-actions__avatar': {
      backgroundColor: theme.palette.transparencies.transparent,
      padding: 0,
      transition: 'none',
      '&:focus, &:hover': {
        backgroundColor: `${theme.palette.transparencies.transparent} !important`,
        boxShadow: 'none',
        transition: 'none',
      },
    },
    '.slds-icon-standard-user': {
      backgroundColor: theme.palette.brand.primary,
      color: theme.palette.white,
      '&:focus, &:hover': {
        backgroundColor: theme.palette.brand.dark,
        color: theme.palette.white,
      },
    },
    /* Important for correct grid alignment */
    '.slds-gutters': {
      ...theme.withBreakpoints.guttersNegativeMargin,
    },
    '.slds-gutters .slds-col': {
      ...theme.withBreakpoints.guttersColumnPadding,
    },
    '.slds-p-horizontal_large': {
      ...theme.withBreakpoints.pageMargins,
      // Explicit width was added specifically as a fix for IE11 issues
      // but it should play nicely with the layout regardless, as this
      // should always be filling its parent container.
      width: '100%',
    },
    '.slds-box': {
      borderRadius: 0,
    },
    '.slds-dropdown:not(.slds-global-actions__dropdown)': {
      '& > .dropdown__list': {
        opacity: 1,
        '& > li': {
          '& > a': {
            paddingLeft: `${theme.units.lg}px`,
          },
          '& svg': {
            fill: theme.palette.brand.dark,
          },
          '&.slds-is-selected > a': {
            paddingLeft: `${theme.units.base}px`,
          },
        },
      },
    },
    '.slds-form-element__help': {
      marginTop: 0,
    },
    '.slds-form-element__label:empty': {
      margin: 0,
      padding: 0,
    },
    '.slds-table td': {
      whiteSpace: 'normal',
    },
    '.slds-table th': {
      color: `${theme.palette.grays.gray9}`,
      lineHeight: '18px',
    },
    '.slds-form-element.checkbox__label--left .slds-checkbox': {
      '& .slds-checkbox__label': {
        display: 'flex',
        flexDirection: 'row-reverse',
        '& > .slds-checkbox_faux': {
          marginRight: '0 !important',
        },
        '& > .slds-form-element__label': {
          color: `${theme.palette.grays.gray9} !important`,
          fontSize: 'inherit',
          marginBottom: 0,
          marginRight: `${theme.units.xs}px`,
          padding: 0,
        },
      },
    },
    // Button overrides to assign brand colors
    ...sldsButtonOverride(theme),
    ...sldsModalOverride(theme),
    ...sldsRadioOverride(theme),
  },
})

export const SLDSOverrides = function SLDSOverrides() {
  return null
}

export default withStyles(styles)(SLDSOverrides)

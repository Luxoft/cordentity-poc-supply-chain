export default (theme) => ({
  '.slds-button': {
    color: theme.palette.brand.dark,
    '&:disabled': {
      color: theme.palette.grays.gray5,
      opacity: 0.5,
    },
    '&:focus:not(:disabled)': {
      boxShadow: `0 0 3px ${theme.palette.brand.darker}`,
      color: theme.palette.brand.darker,
    },
    '&:hover:not(:disabled)': {
      color: theme.palette.brand.darker,
    },
    '&:not(.slds-button_icon-border) &:not(.slds-global-actions__avatar)': {
      padding: `0 ${theme.units.sm}px`,
    },
    // eslint-disable-next-line
    '&:not(.slds-button_brand) &:not(.slds-button_destructive) &:not(.slds-button_text-destructive) &:not(.slds-button_neutral) &:not(.slds-button_icon-border)': {
      backgroundColor: 'transparent',
      color: theme.palette.brand.darker,
      '&:disabled': {
        backgroundColor: 'transparent',
        color: theme.palette.brand.darker,
      },
      '&:focus:not(:disabled)': {
        backgroundColor: 'transparent',
        color: theme.palette.brand.darker,
      },
      '&:hover:not(:disabled)': {
        backgroundColor: 'transparent',
        color: theme.palette.brand.darker,
      },
    },
    // eslint-disable-next-line
    '&.lum-icon-button': {
      backgroundColor: theme.palette.transparencies.transparent,
      border: 0,
      color: theme.palette.grays.gray9,
      padding: 0,
      '&:focus:not(:disabled)': {
        backgroundColor: theme.palette.transparencies.transparent,
        boxShadow: 'none',
        color: theme.palette.grays.gray9,
      },
      '&:hover:not(:disabled)': {
        backgroundColor: theme.palette.transparencies.transparent,
        color: theme.palette.grays.gray9,
      },
    },
    '&.slds-button_brand': {
      backgroundColor: theme.palette.brand.dark,
      borderColor: theme.palette.brand.dark,
      color: theme.palette.white,
      '&:disabled': {
        backgroundColor: theme.palette.grays.gray5,
        borderColor: theme.palette.grays.gray5,
        color: theme.palette.grays.gray8,
      },
      '&:focus:not(:disabled)': {
        backgroundColor: theme.palette.brand.darker,
        borderColor: theme.palette.brand.darker,
        color: theme.palette.white,
      },
      '&:hover:not(:disabled)': {
        backgroundColor: theme.palette.brand.darker,
        borderColor: theme.palette.brand.darker,
        color: theme.palette.white,
      },
    },
    '&.slds-button_destructive, &.slds-button_destructive:hover, &.slds-button_destructive:focus': {
      backgroundColor: theme.palette.reds.textDestructive,
      color: theme.palette.white,
    },
    '&.slds-button_icon-border': {
      backgroundColor: theme.palette.white,
    },
    '&.slds-button_neutral:not(.lum-icon-button), &.slds-button_icon-border': {
      '&:focus:not(:disabled)': {
        backgroundColor: theme.palette.brand.background,
      },
      '&:hover:not(:disabled)': {
        backgroundColor: theme.palette.brand.background,
      },
    },
    '&.slds-button_outline-brand': {
      borderColor: theme.palette.brand.dark,
      '&:focus:not(:disabled)': {
        backgroundColor: theme.palette.white,
        boxShadow: `0 0 3px ${theme.palette.brand.darker}`,
      },
      '&:hover:not(:disabled)': {
        backgroundColor: theme.palette.white,
        textDecoration: 'underline',
      },
    },
    '&.slds-button_text-destructive, &.slds-button_text-destructive:hover, &.slds-button_text-destructive:focus': {
      backgroundColor: theme.palette.white,
      color: theme.palette.reds.textDestructive,
    },
    '&_reset.slds-text-link': {
      ...theme.typography.styles.label,
      color: theme.palette.brand.dark,
      fontWeight: 'normal',
      outline: 'none',
      '&:focus:not(:disabled)': {
        color: theme.palette.brand.darker,
      },
      '&:hover:not(:disabled)': {
        color: theme.palette.brand.darker,
      },
    },
  },
  '.slds-dropdown-trigger': {
    '& > .slds-button': {
      color: theme.palette.grays.gray9,
      display: 'block',
      overflow: 'hidden',
      textAlign: 'left',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      '&:not(.slds-global-actions__avatar)': {
        paddingRight: `${theme.units.lg}px`,
      },
    },
    '& > .slds-button > svg': {
      position: 'absolute',
      right: `${theme.units.sm}px`,
      top: '50%',
      transform: 'translateY(-50%)',
    },
    '& > .slds-button.slds-has-error': {
      border: `2px solid ${theme.palette.reds.textDestructive}`,
    },
    '& > .slds-button[class*="slds-button_icon-"]': {
      paddingLeft: `${theme.units.sm}px`,
    },
  },
})

export default (theme) => ({
  '.slds-modal__header > .slds-modal__close': {
    right: `${theme.units.base}px`,
    top: `${theme.units.base}px`,
    '& svg': {
      height: `${theme.units.sm}px`,
      width: `${theme.units.sm}px`,
      fill: `${theme.palette.black}`,
    },
  },
  '.slds-modal__header': {
    borderRadius: 0,
    paddingBottom: `${theme.units.xs}px`,
    paddingTop: `${theme.units.xs}px`,
    textAlign: 'left',
  },
  '.slds-modal__header .slds-text-heading_medium': {
    ...theme.typography.styles.sectionTitle,
  },
  '.slds-modal__container.modal_small': {
    minWidth: 'auto',
    width: `${theme.units.base * 40}px`,
  },
  '.slds-backdrop': {
    background: theme.palette.transparencies.black40,
  },
  '.slds-modal__content': {
    '&:last-child': {
      borderRadius: 0,
    },
  },
})

export default (theme) => ({
  '.slds-form-element__control .slds-radio': {
    '&:not(:first-child):not(:last-child)': {
      margin: `${theme.units.halfBase}px 0`,
    },
    '&:nth-child(2):last-child': {
      marginTop: `${theme.units.halfBase}px`,
    },
  },
  '.slds-radio .slds-radio__label': {
    cursor: 'pointer',
  },
  '.slds-radio .slds-form-element__label': {
    ...theme.typography.styles.label,
    fontWeight: 'normal',
  },
  '.slds-radio .slds-radio_faux, .slds-radio .slds-radio--faux': {
    height: '14px',
    outline: 'none',
    width: '14px',
  },
  '.slds-radio [type="radio"] + .slds-radio_faux, .slds-radio [type="radio"] + .slds-radio--faux, .slds-radio [type="radio"] ~ .slds-radio_faux, .slds-radio [type="radio"] ~ .slds-radio--faux, .slds-radio [type="radio"] + .slds-radio__label .slds-radio_faux, .slds-radio [type="radio"] + .slds-radio__label .slds-radio--faux': {
    marginRight: '4px',
  },
  '.slds-radio [type="radio"]:checked + .slds-radio_faux, .slds-radio [type="radio"]:checked + .slds-radio--faux, .slds-radio [type="radio"]:checked ~ .slds-radio_faux, .slds-radio [type="radio"]:checked ~ .slds-radio--faux, .slds-radio [type="radio"]:checked + .slds-radio__label .slds-radio_faux, .slds-radio [type="radio"]:checked + .slds-radio__label .slds-radio--faux': {
    borderColor: theme.palette.blues.dark,
  },
  '.slds-radio [type="radio"]:checked + .slds-radio_faux:after, .slds-radio [type="radio"]:checked + .slds-radio--faux:after, .slds-radio [type="radio"]:checked ~ .slds-radio_faux:after, .slds-radio [type="radio"]:checked ~ .slds-radio--faux:after, .slds-radio [type="radio"]:checked + .slds-radio__label .slds-radio_faux:after, .slds-radio [type="radio"]:checked + .slds-radio__label .slds-radio--faux:after': {
    background: theme.palette.blues.dark,
  },
})

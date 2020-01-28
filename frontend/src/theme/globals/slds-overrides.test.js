import { SLDSOverrides, styles } from './slds-overrides'

const theme = {
  palette: {
    grays: {},
    brand: {},
    reds: {},
    transparencies: {},
    blues: {},
  },
  units: {},
  borders: {},
  typography: {
    styles: {},
  },
  withBreakpoints: {

  },
}

const globalKey = '@global'
const globalKeysToCheck = [
  '.slds-nav-vertical',
  '.slds-context-bar',
  '.slds-gutters',
  '.slds-button',
  '.slds-dropdown-trigger',
  '.slds-modal__header',
  '.slds-backdrop',
  '.slds-radio .slds-radio__label',
]

describe('the GlobalStyles component', () => {
  it('should be null', () => {
    expect(SLDSOverrides()).toBeNull()
  })
})

describe('the global styles', () => {
  it('should have the correct high-level keys', () => {
    const gs = styles(theme)

    expect(gs).toBeDefined()

    const global = gs[globalKey]

    expect(global).toBeDefined()

    globalKeysToCheck.forEach((key) => {
      expect(global[key]).toBeDefined()
    })
  })
})

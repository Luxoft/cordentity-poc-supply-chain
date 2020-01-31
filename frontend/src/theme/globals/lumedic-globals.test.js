import { styles, GlobalStyles } from './lumedic-globals'

const theme = {
  palette: {
    grays: {},
    brand: {},
  },
  units: {},
  borders: {},
}

const globalKey = '@global'
const globalKeysToCheck = [
  '.print-display',
  '@media print',
  'html',
  'a',
  '.lum-body',
  '.report iframe',
]

describe('the GlobalStyles component', () => {
  it('should be null', () => {
    expect(GlobalStyles()).toBeNull()
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

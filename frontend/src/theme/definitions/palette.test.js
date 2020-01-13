import palette from './palette'

const PALETTE_KEYS = {
  white: 1,
  black: 1,
  transparencies: 8,
  grays: 9,
  brand: 8,
  chart: 4,
  reds: 6,
  oranges: 4,
  yellows: 2,
  greens: 4,
  blues: 2,
}

describe('the palette object', () => {
  it('should have all listed keys with a lenght equal to key value', () => {
    Object.keys(PALETTE_KEYS).forEach((key) => {
      const value = PALETTE_KEYS[key]
      const paletteProp = palette[key]

      expect(palette[key]).toBeDefined()

      if (value === 1) {
        expect(typeof paletteProp).toBe('string')
      }
      else {
        expect(Object.keys(paletteProp).length).toBe(value)
      }
    })
  })
})

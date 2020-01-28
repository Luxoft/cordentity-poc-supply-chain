import typography from './typography'

const EXPECTED_KEYS = [
  'size1',
  'size2',
  'size3',
  'size4',
  'size5',
  'size6',
  'size7',
  'size8',
  'size9',
  'size10',
  'size11',
]

describe('the typography object', () => {
  it('should have the same keys as the expected object keys', () => {
    EXPECTED_KEYS.forEach((key) => {
      expect(typography[key]).toBeDefined()
    })
  })
})

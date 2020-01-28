import React from 'react'
import { storiesOf } from '@storybook/react'
import typography from './typography'
import theme from '../theme'

const makeStoryObject = (label, component) => ({
  label,
  component,
})

const storyObjects = [
  makeStoryObject('Font Size 11', <p style={{ ...typography.size11 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- largestMessage', <p style={{ ...theme.typography.styles.largestMessage }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 10', <p style={{ ...typography.size10 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 9', <p style={{ ...typography.size9 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 8', <p style={{ ...typography.size8 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- largeMessage', <p style={{ ...theme.typography.styles.largeMessage }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 7', <p style={{ ...typography.size7 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 6', <p style={{ ...typography.size6 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- pageTitle', <p style={{ ...theme.typography.styles.pageTitle }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 5', <p style={{ ...typography.size5 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- sectionTitle', <p style={{ ...theme.typography.styles.sectionTitle }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 4', <p style={{ ...typography.size4 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- sectionSubTitle', <p style={{ ...theme.typography.styles.sectionSubTitle }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 3', <p style={{ ...typography.size3 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- body', <p style={{ ...theme.typography.styles.body }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 2', <p style={{ ...typography.size2 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- label', <p style={{ ...theme.typography.styles.label }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('Font Size 1', <p style={{ ...typography.size1 }}>The quick brown fox jumps over the lazy dog.</p>),
  makeStoryObject('- footnote', <p style={{ ...theme.typography.styles.footnote }}>The quick brown fox jumps over the lazy dog.</p>),
]

const stories = storiesOf('Atoms/Typography', module)
  .add('all typography', () => (
    <div>
      {storyObjects.map((o) => o.component)}
    </div>
  ))

storyObjects.forEach((o) => {
  stories.add(o.label, () => o.component)
})

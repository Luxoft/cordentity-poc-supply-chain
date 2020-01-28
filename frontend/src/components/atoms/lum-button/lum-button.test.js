import React from 'react'
import { shallow } from 'enzyme'
import { Button } from '@salesforce/design-system-react'
import { Link } from 'react-router-dom'
import LUMButton from './lum-button'

/**
 * This test is an example of two ways to test the component UI:
 *  - Snapshot Tests:
 *     These provide an implicit test to verify that nothing in the rendered component
 *     has changed since the previous snapshot.
 *
 *     These are probably most valuable in the more primitive components (atoms,
 *     molecules) that aren't as likely to change.
 *
 *     They also provide a safeguard against changes in underlying libraries (SLDS) if
 *     structure or properties of the components change.
 *
 *     It is worth looking through the snapshot `lum-button.test.js.snap` to see what the
 *     serialied structure looks like. Also note that it captures the state of the SLDS
 *     component, and all of the props it automatically assigns.
 *
 * - Direct Assertions:
 *    These provide an explicit test to verify what we expect to be present in the
 *    rendered component.
 *
 *    These are probably most valuable in the larger, more complex components
 *    (organisms, pages, templates) where the underlying structure may be more likely
 *    to change, but where we also want to verify the presence of core pieces of
 *    the DOM or component props.
 */

describe('the LUMButton', () => {
  it('should render correctly with no props', () => {
    const wrapper = shallow(<LUMButton />)

    expect(wrapper).toBeDefined()
    expect(wrapper.type()).toBe(Button)

    expect(wrapper).toMatchSnapshot()
  })

  it('should assign the `slds-truncate` style when `canTruncate` is passed', () => {
    const wrapper = shallow(<LUMButton canTruncate />)

    expect(wrapper).toBeDefined()
    expect(wrapper.hasClass('slds-truncate')).toBeTruthy()
    expect(wrapper).toMatchSnapshot()
  })
})

describe('the LUMButton with link', () => {
  it('should render an anchor tag for external link', () => {
    const wrapper = shallow(<LUMButton link={{ href: 'https://portal.lumedic.io' }} />)

    expect(wrapper).toBeDefined()
    expect(wrapper.type()).toBe('a')
    expect(wrapper.prop('href')).toBe('https://portal.lumedic.io')
    expect(wrapper).toMatchSnapshot()
  })

  it('should render correctly with internal link', () => {
    const wrapper = shallow(<LUMButton link={{ type: 'internal', href: '/' }} />)

    expect(wrapper).toBeDefined()
    expect(wrapper.type()).toBe(Link)
    expect(wrapper.childAt(0).type()).toBe(Button)
    expect(wrapper.prop('to')).toBe('/')
    expect(wrapper).toMatchSnapshot()
  })
})

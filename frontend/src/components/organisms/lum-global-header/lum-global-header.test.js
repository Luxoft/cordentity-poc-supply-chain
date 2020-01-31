import React from 'react'
import { shallow } from 'enzyme'
import { LUMGlobalHeader } from './lum-global-header'

it('should render correctly with no props', () => {
  const wrapper = shallow(
    <LUMGlobalHeader isAuthenticated={false} userMeta={{}} authInProgress={false} />,
  )

  expect(wrapper).toBeDefined()
})

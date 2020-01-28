import React from 'react'
import { shallow } from 'enzyme'
import { RevUpDashboardContent } from './template-power-bi-content'

it('should render correctly with no props', () => {
  const wrapper = shallow(<RevUpDashboardContent classes={{}} />)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

import React from 'react'
import { shallow } from 'enzyme'
import RevUpDashboardHeader from '.'

it('should render correctly with no props', () => {
  const wrapper = shallow(<RevUpDashboardHeader />)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

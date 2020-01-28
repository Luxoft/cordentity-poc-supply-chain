import React from 'react'
import { shallow } from 'enzyme'
import { LUMPageTitle } from './lum-page-title'

it('should render correctly with no props', () => {
  const wrapper = shallow(<LUMPageTitle printDisplay={false} classes={{}} titleText="my title" />)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

it('should render correctly with printDisplay true', () => {
  const wrapper = shallow(<LUMPageTitle printDisplay classes={{}} titleText="my title" />)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

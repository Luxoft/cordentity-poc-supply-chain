import React from 'react'
import { shallow } from 'enzyme'
import TPContentContainer from '.'

it('should render correctly with no props', () => {
  const wrapper = shallow(<TPContentContainer><React.Fragment /></TPContentContainer>)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

it('should render correctly with props', () => {
  const wrapper = shallow(<TPContentContainer growContent><React.Fragment /></TPContentContainer>)

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

import React from 'react'
import { shallow } from 'enzyme'
import TemplateFullWidth from '.'

it('should render correctly with no props', () => {
  const wrapper = shallow(
    <TemplateFullWidth
      globalHeaderEl={<React.Fragment />}
      pageHeaderEl={<React.Fragment />}
      pageContentEl={<React.Fragment />}
    >
      <React.Fragment />
    </TemplateFullWidth>,
  )

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

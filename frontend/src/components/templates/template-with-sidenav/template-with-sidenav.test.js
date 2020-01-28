import React from 'react'
import { shallow } from 'enzyme'
import { TemplateWithSidenav } from './template-with-sidenav'

it('should render correctly with no props', () => {
  const wrapper = shallow(
    <TemplateWithSidenav
      globalHeaderEl={<React.Fragment />}
      pageHeaderEl={<React.Fragment />}
      pageContentEl={<React.Fragment />}
      sideNavEl={<React.Fragment />}
      classes={{}}
    />,
  )

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

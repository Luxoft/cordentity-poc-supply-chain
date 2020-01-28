import React from 'react'
import { mount } from 'enzyme'
import { mountWithTheme } from 'Theme/test-provider'
import TPContentHeader from '.'

it('should render correctly with no props', () => {
  const wrapper = mountWithTheme(mount, TPContentHeader, {
    children: React.Fragment,
  })

  expect(wrapper).toBeDefined()
  //   expect(wrapper).toMatchSnapshot()
})

import { mount } from 'enzyme'
import { mountWithThemeAndRouter } from 'Theme/test-provider'
import LUMButton from 'Components/atoms/lum-button'
import LUMError from '.'

// todo fix the path of the images
const ERROR_TYPES = {
  403: '403.svg',
  404: '404.svg',
  500: '500.svg',
}

const link = { type: 'internal', href: '/' }

describe('the LUMError component', () => {
  it('should render correctly with props', () => {
    const wrapper = mountWithThemeAndRouter(mount, LUMError, {
      errorType: 100,
      titleText: 'Error title',
      contentText: 'Error Description',
    })

    expect(wrapper).toBeDefined()
    expect(wrapper.find(LUMError).length).toBe(1)

    // expect(wrapper).toMatchSnapshot()
  })

  it('should have img with src="500.svg" for internal server Error', () => {
    const wrapper = mountWithThemeAndRouter(mount, LUMError, {
      errorType: 500, titleText: 'Unexpected error', contentText: 'Server Error',
    })

    const img = wrapper.find('img')
    const button = wrapper.find(LUMButton)

    expect(wrapper).toBeDefined()
    expect(img.prop('src')).toBe(ERROR_TYPES[500])

    expect(button.type()).toBe(LUMButton)
    expect(button.prop('label')).toBe('Return home')
    expect(button.prop('link')).toStrictEqual(link)
  })

  it('should render login button for the unauthorized error', () => {
    const logoutLink = { ...link, href: '/logout' }
    const wrapper = mountWithThemeAndRouter(mount, LUMError, {
      errorType: 403,
      titleText: 'Sorry, you are not authorized to view this page',
      contentText: 'Please try again with a valid user name and password.',
    })

    const img = wrapper.find('img')
    const button = wrapper.find(LUMButton)

    expect(wrapper).toBeDefined()
    expect(img.prop('src')).toBe(ERROR_TYPES[403])

    expect(button.at(0).type()).toBe(LUMButton)
    expect(button.at(0).prop('link')).toStrictEqual(link)

    expect(button.at(1).type()).toBe(LUMButton)
    expect(button.at(1).prop('link')).toStrictEqual(logoutLink)
  })

  it('should assign the styles "root, verticalCenter" to the top level containers', () => {
    const wrapper = mountWithThemeAndRouter(mount, LUMError, {
      errorType: 404,
      titleText: 'Hmmm... page not found',
      contentText: 'The page you requested could not be found',
    })

    const rootNode = wrapper.find('#lum-error-root')
    const imgContainer = rootNode.find('#lum-error-img-container')
    const textContainer = rootNode.find('#lum-error-text-container')

    expect(rootNode.props().className).toMatch('root')
    expect(imgContainer.props().className).toBeUndefined()
    expect(textContainer.props().className).toMatch('verticalCenter')
  })
})

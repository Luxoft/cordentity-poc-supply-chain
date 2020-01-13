// import React from 'react'
import { mount } from 'enzyme'
import { mountWithThemeAndRouter } from 'Theme/test-provider'
import './template-power-bi-data-loaders'
import TemplatePowerBIReport from '.'

jest.mock('./template-power-bi-data-loaders')

const ReportTemplateProps = {
  reportName: 'title',
  reportId: 'reportId',
  activePageName: 'activePageName',
  workspaceId: 'workspaceId',
  navTree: {
    topNav: [],
    sideNav: {
      appModules: [{
        id: 'module_id',
        label: 'ModuleLabel',
        requires: ['perm1', 'perm2'],
        items: [],
        icon: {
          defaultIcon: '/path/to/icon.svg',
          activeIcon: '/path/to/icon2.svg',
        },
      }],
      company: [],
    },
    otherNav: [],
  },
}

it('should render correctly with no props', () => {
  const wrapper = mountWithThemeAndRouter(mount, TemplatePowerBIReport, ReportTemplateProps)

  expect(wrapper).toBeDefined()
})

/**
 * These methods were moved from other components.
 * These may be useful in the future if these tests are relevant after
 * functionality is approved.
 */
// describe('buildFilters', () => {
//   const allPageFiltersStructure = {
//     Homepage: [
//       {
//         $schema: 'http://powerbi.com/product/schema#basic',
//         filterType: 1,
//         operator: 'In',
//         target: { column: 'Region Name', table: 'Region_DIM' },
//         values: [],
//       }, {
//         $schema: 'http://powerbi.com/product/schema#basic',
//         filterType: 1,
//         operator: 'All',
//         target: { column: 'Facility Name', table: 'Facility_DIM' },
//         values: [],
//       },
//     ],
//     'Numeric Details': [
//       {
//         $schema: 'http://powerbi.com/product/schema#basic',
//         filterType: 1,
//         operator: 'In',
//         target: { column: 'Metric', table: 'Metric_DIM' },
//         values: [],
//       },
//     ],
//     'Percent Details': [
//       {
//         $schema: 'http://powerbi.com/product/schema#basic',
//         filterType: 1,
//         operator: 'In',
//         target: { column: 'Metric', table: 'Metric_DIM' },
//         values: [],
//       },
//     ],
//   }

// describe('getSelectedOptionObjectByPageFilter', () => {
//   const filterA = {
//     target: {
//       table: 'tableA',
//     },
//     values: ['a'],
//     key: 'tableA',
//   }
//   const filterB = {
//     target: {
//       table: 'tableB',
//     },
//     values: ['b'],
//     key: 'tableB',
//   }

//   it('should return the nextFilterValues given a pageName & filterObject', () => {
//     const pageName = 'Homepage'
//     const instance = wrapper.instance()

//     instance.state = {
//       rawFilterData: [
//         {
//           name: 'Homepage',
//           filters: [{
//             filter: filterA,
//             name: 'Homepage',
//             key: 'tableA',
//             values: [{ value: 'a' }],
//           },
//           ],
//         },
//         {
//           name: 'Otherpage',
//           filters: [{
//             filter: filterB,
//             name: 'Otherpage',
//             key: 'tableB',
//             values: [{ value: 'b' }],
//           },
//           ],
//         },
//       ],
//     }

//     const result = instance.getSelectedOptionObjectByPageFilter(pageName, filterA)

//     expect(result).toBe(instance.state.rawFilterData[0].filters[0].values[0])
//   })
// })

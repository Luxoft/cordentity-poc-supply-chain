import React from 'react'
import PropTypes from 'prop-types'
import { RENDERING_STATES, GLOBAL_HEADER_TITLE } from 'Data/constants'
import { TemplateFullWidth, TemplateWithSidenav } from 'Components/templates'
import LUMGlobalHeader from 'Components/organisms/lum-global-header'
import LUMPrimaryNavigation from 'Components/organisms/lum-primary-navigation'
import { PBI } from 'Classes/analytics'
import TemplatePowerBIContent from './template-power-bi-content'
import TemplatePowerBIHeader from './template-power-bi-header'
import { fetchPowerBIReportConfig, fetchPowerBIReportFilters } from './template-power-bi-data-loaders'

class TemplatePowerBIReport extends React.Component {
  constructor({ reportName }) {
    super()

    this.reportName = reportName
    this.currentAnalyticsEvent = null
  }

  state = {
    reportConfig: undefined,
    reportFilters: undefined,
    reportStatus: RENDERING_STATES.loading,
    report: undefined, // eslint-disable-line
    currentPage: undefined,
    selectedFilterOption: undefined,
    pageFiltersCollection: undefined,
    pages: undefined,
    pagesVisited: [],
    error: null,
  }

  componentDidMount = async () => {
    try {
      const { reportId, workspaceId } = this.props
      const [reportConfig, reportFilters] = await Promise.all([
        fetchPowerBIReportConfig(workspaceId, reportId),
        fetchPowerBIReportFilters(reportId),
      ])

      if (reportConfig) {
        if (reportConfig.TokenUtilizationPercentage) {
          // eslint-disable-next-line no-console
          console.log(`Power BI Embed Token Utilization: ${reportConfig.TokenUtilizationPercentage}%`)
        }

        if (reportFilters) {
          this.setState({
            reportConfig,
            reportFilters: reportFilters.pages,
            pageFiltersCollection: this.buildFilters(reportFilters.pages),
          })
        }
        else {
          this.setState({ reportConfig })
        }
      }
    }
    catch (error) {
      this.setState({ error })
    }
  }

  reportEvent = (event) => {
    if (this.currentAnalyticsEvent) {
      this.currentAnalyticsEvent.addEvent(event)
    }
    else {
      this.currentAnalyticsEvent = new PBI.AnalyticsEvent(this.reportName)
      this.currentAnalyticsEvent.addEvent(event)
    }
  }

  finishEvent = () => {
    if (!this.currentAnalyticsEvent) {
      this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.untracked))
    }

    PBI.sendToMixpanel(this.currentAnalyticsEvent)

    this.currentAnalyticsEvent = null
  }

  pbiOnLoad = async (report) => {
    const { activePageName } = this.props

    if (report) {
      this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.reportLoaded, report))

      const pages = await report.getPages()
      const currentPage = activePageName
        ? report.page(activePageName)
        : pages.find((page) => page.isActive)

      if (activePageName) currentPage.setActive()

      // eslint-disable-next-line
      this.setState({ report, pages, currentPage })
    }
  }

  pbiOnRender = () => {
    this.finishEvent()
    this.setState({ reportStatus: RENDERING_STATES.success })
  }

  pbiNewFilterSelected = async (selectedOption, filtersCollection) => {
    const { pages } = this.state

    this.setState({ selectedFilterOption: selectedOption, reportStatus: RENDERING_STATES.loading })

    const newPage = selectedOption.page

    const reportPage = pages.find((page) => page.displayName === newPage)

    this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.newFilters, selectedOption))

    if (!reportPage.isActive) {
      await reportPage.setActive()
    }

    reportPage.setFilters(filtersCollection)
  }

  pbiAllFiltersBuilt = (filtersCollection) => {
    this.setState({ pageFiltersCollection: filtersCollection })
  }

  pbiPageChanged = async (event) => {
    const { currentPage, pagesVisited } = this.state

    pagesVisited.push(event.newPage)

    this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.newPage, event.newPage))

    this.setState({ reportStatus: RENDERING_STATES.loading, pagesVisited })

    if (currentPage) {
      const newPageFilters = await event.newPage.getFilters()
      const selectedValues = []

      newPageFilters.forEach((filter) => {
        const sO = this.getSelectedOptionObjectByPageFilter(event.newPage.displayName, filter)

        if (sO) {
          selectedValues.push(sO)
        }
      })

      if (selectedValues.length === 1) {
        this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.foundFilters, selectedValues[0]))

        this.setState({ selectedFilterOption: selectedValues[0], currentPage: event.newPage })
      }
      else if (event.newPage.displayName === 'Homepage') {
        // this is a little hacky. I currently don't have a better idea
        // for how to set the 'All Regions' option on the Homepage.
        // TODO: look into assigning if all filters have prop: `operator` value: 'All'
        this.setState({ selectedFilterOption: undefined, currentPage: event.newPage })
      }
      else {
        throw new Error('Currently only one selected filter option is supported.')
      }
    }
  }

  pbiButtonClicked = (/* event */) => {
    // console.log('BUTTON CLICKED', event)
  }

  pbiFiltersApplied = (/* event */) => {
    // console.log('FILTERS APPLIED', event)
  }

  pbiCommandTriggered = (/* event */) => {
    // console.log('COMMAND TRIGGERED', event)
  }

  pbiSelectedData = (event) => {
    this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.dataSelected, event))
  }

  pbiBackButtonPressed = () => {
    const { pagesVisited } = this.state
    let pageToLoad = pagesVisited.splice(pagesVisited.length - 1, 1)

    this.setState({ reportStatus: RENDERING_STATES.loading })

    this.reportEvent(new PBI.ReportEvent(PBI.eventTypes.wentBack, pageToLoad))

    while (pageToLoad.displayName !== 'Homepage') {
      [pageToLoad] = pagesVisited.splice(pagesVisited.length - 1, 1)
    }

    pageToLoad.setActive()
  }

  buildFilters = (filterData) => {
    const pageFilters = {}

    filterData.forEach((page) => {
      pageFilters[page.name] = []

      page.filters.forEach((filterMeta) => {
        pageFilters[page.name].push(filterMeta.filter)
      })
    })

    return pageFilters
  }

  getSelectedOptionObjectByPageFilter = (pageName, filterObject) => {
    const { reportFilters } = this.state

    // This will return the filter meta information for the current page, as an array.
    const nextPageFilterMeta = reportFilters.find(
      (entry) => entry.name === pageName,
    )

    // A page may have multiple filters. This will get only the filter
    // we're currently dealing with, as an array.
    const nextPageFilterValues = nextPageFilterMeta.filters.find(
      (filterMeta) => filterMeta.key === filterObject.target.table,
    )

    // The values for the filter are where we get the options from.
    // Find the values that match the current filter value
    if (nextPageFilterValues) {
      const nextFilterValues = nextPageFilterValues.values.find(
        // Currently this only supports a single applied filter option.
        // This may need to change in the future.
        (option) => option.value === filterObject.values[0],
      )

      return nextFilterValues
    }

    // This will return null when irrelevant filters are present,
    // such as the first time leaving the Homepage for a Detail page.
    return null
  }

  powerBIHeader = () => {
    const {
      reportFilters, reportStatus, pagesVisited,
      currentPage, selectedFilterOption, pageFiltersCollection,
    } = this.state
    const {
      reportName, supportBtn,
    } = this.props

    return (
      <TemplatePowerBIHeader
        filterData={reportFilters}
        filterByPage={pageFiltersCollection}
        currentPage={currentPage}
        selectedOption={selectedFilterOption}
        onFilterChange={this.pbiNewFilterSelected}
        onBackButtonPressed={this.pbiBackButtonPressed}
        canGoBack={pagesVisited.length > 1}
        reportTitle={reportName}
        reportStatus={reportStatus}
        supportBtn={supportBtn}
      />
    )
  }

  powerBIContent = () => {
    const {
      reportConfig, reportStatus,
    } = this.state

    return (
      <TemplatePowerBIContent
        reportConfig={reportConfig}
        reportStatus={reportStatus}
        onLoad={this.pbiOnLoad}
        onRender={this.pbiOnRender}
        onPageChange={this.pbiPageChanged}
        onButtonClicked={this.pbiButtonClicked}
        onFiltersApplied={this.pbiFiltersApplied}
        onCommandTriggered={this.pbiCommandTriggered}
        onSelectedData={this.pbiSelectedData}
      />
    )
  }

  render() {
    const { navTree, reportName } = this.props
    const { error } = this.state

    return (
      navTree
        ? (
          <TemplateWithSidenav
            globalHeaderEl={<LUMGlobalHeader title={GLOBAL_HEADER_TITLE.AnalyticsAndInsights} />}
            pageError={error}
            pageHeaderEl={this.powerBIHeader()}
            pageContentEl={this.powerBIContent()}
            pageTitle={reportName}
            sideNavEl={<LUMPrimaryNavigation navList={navTree.sideNav.appModules} />}
            growContent
          />
        )
        : (
          <TemplateFullWidth
            globalHeaderEl={<LUMGlobalHeader title={GLOBAL_HEADER_TITLE.AnalyticsAndInsights} />}
            pageError={error}
            pageHeaderEl={this.powerBIHeader()}
            pageContentEl={this.powerBIContent()}
            pageTitle={reportName}
            growContent
          />
        )
    )
  }
}

TemplatePowerBIReport.defaultProps = {
  navTree: undefined,
  reportName: 'PowerBI Analytics',
  supportBtn: null,
  activePageName: undefined,
}

TemplatePowerBIReport.propTypes = {
  navTree: PropTypes.object,
  reportName: PropTypes.string,
  reportId: PropTypes.string.isRequired,
  activePageName: PropTypes.string,
  workspaceId: PropTypes.string.isRequired,
  supportBtn: PropTypes.node,
}

export default TemplatePowerBIReport

import React from 'react'
import PropTypes from 'prop-types'
import LUMPageTitle from 'Components/atoms/lum-page-title'
import PowerBIFilters from 'Components/organisms/lum-powerbi-report/powerbi-filters'
import LUMHeaderContent from 'Components/atoms/lum-header-content'

const RevUpDashboardHeader = function RevUpDashboardHeader({ reportTitle, supportBtn, ...rest }) {
  return (
    <>
      <LUMPageTitle titleText={reportTitle} />
      <LUMHeaderContent>
        <PowerBIFilters {...rest} />
        <div className="slds-col" />
        {supportBtn}
      </LUMHeaderContent>
    </>
  )
}

RevUpDashboardHeader.defaultProps = {
  reportTitle: 'PowerBI Report',
  supportBtn: null,
}

RevUpDashboardHeader.propTypes = {
  reportTitle: PropTypes.string,
  supportBtn: PropTypes.node,
}

export default RevUpDashboardHeader

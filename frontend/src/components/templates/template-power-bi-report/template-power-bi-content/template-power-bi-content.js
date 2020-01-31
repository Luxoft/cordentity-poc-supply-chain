import React from 'react'
import PropTypes from 'prop-types'
import LUMPowerBIReport from 'Components/organisms/lum-powerbi-report'
import { RENDERING_STATES } from 'Data/constants'
import LUMLoader from 'Components/molecules/lum-loader'
import withStyles from 'react-jss'

export const styles = (theme) => ({
  powerbiReportsWrapper: {
    height: '100%',
    position: 'relative',
    width: '100%',
  },
  loadingContainer: {
    backgroundColor: theme.palette.brand.background,
    bottom: '0',
    left: '0',
    padding: '5rem 0',
    position: 'absolute',
    right: '0',
    top: '0',
  },
})

export const RevUpDashboardContent = function RevUpDashboardContent(
  { reportStatus, classes, ...rest },
) {
  return (
    <div className={`${classes.powerbiReportsWrapper}`}>
      { reportStatus === RENDERING_STATES.loading
        ? (
          <div className={classes.loadingContainer}>
            <LUMLoader />
          </div>
        )
        : null }
      <LUMPowerBIReport
        {...rest}
      />
    </div>
  )
}

RevUpDashboardContent.propTypes = {
  classes: PropTypes.object.isRequired,
  reportStatus: PropTypes.string.isRequired,
}

export default withStyles(styles)(RevUpDashboardContent)

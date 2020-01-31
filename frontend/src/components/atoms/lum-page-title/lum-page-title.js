import React from 'react'
import PropTypes from 'prop-types'
import withStyles from 'react-jss'

const styles = (theme) => ({
  root: {
    ...theme.typography.styles.pageTitle,
  },
})

export const LUMPageTitle = function LUMPageTitle({ titleText, classes, printDisplay }) {
  let printClass = ''

  if (printDisplay === true) {
    printClass = 'print-display'
  }
  else if (printDisplay === false) {
    printClass = 'no-print-display'
  }

  return (
    <h1 className={`${classes.root} slds-col ${printClass}`}>{titleText}</h1>
  )
}

LUMPageTitle.defaultProps = {
  printDisplay: undefined,
}

LUMPageTitle.propTypes = {
  titleText: PropTypes.string.isRequired,
  classes: PropTypes.object.isRequired,
  printDisplay: PropTypes.bool,
}

export default withStyles(styles)(LUMPageTitle)

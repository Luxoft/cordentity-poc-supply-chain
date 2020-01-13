import React from 'react'
import PropTypes from 'prop-types'
import { createUseStyles, useTheme } from 'react-jss'

const useStyles = createUseStyles((theme) => ({
  root: {
    background: theme.palette.white,
    borderBottom: theme.borders.standard,
    padding: `${theme.units.md}px ${theme.units.sm}px ${theme.units.sm}px`,
    width: '100%',
    '&:empty': {
      padding: '0',
    },
    [theme.breakpoints.forDesktopUp]: {
      padding: `${theme.units.lg}px ${theme.units.md}px ${theme.units.sm}px`,
    },
  },
  transparent: {
    background: theme.palette.transparencies.transparent,
    borderBottom: 'none',
  },
}))

const TPContentHeader = function TPContentHeader({ variant, children }) {
  const classes = useStyles(useTheme())

  return (
    <div className={`${classes.root} ${classes[variant]}`}>
      {children}
    </div>
  )
}

TPContentHeader.defaultProps = {
  variant: '',
}

TPContentHeader.propTypes = {
  children: PropTypes.oneOfType([PropTypes.object, PropTypes.string]).isRequired,
  variant: PropTypes.string,
}

export default TPContentHeader

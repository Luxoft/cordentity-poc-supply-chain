import React from 'react'
import PropTypes from 'prop-types'
import { createUseStyles, useTheme } from 'react-jss'
import { ERROR_TYPES, APP_ROUTES } from 'Data/constants'
import LUMButton from 'Components/atoms/lum-button'

import Forbidden from 'Assets/images/error/403.svg'
import ServerError from 'Assets/images/error/500.svg'
import PageNotFound from 'Assets/images/error/404.svg'

const useStyles = createUseStyles((theme) => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    margin: '0 auto',
    maxWidth: '1024px',
    paddingTop: `${theme.units.sm}px`,
    '& > div': {
      width: '49%',
    },
    '@supports (display: grid)': {
      display: 'grid',
      flexWrap: 'unset',
      gridGap: '40px',
      gridTemplateColumns: '1fr 1fr',
      justifyContent: 'unset',
      width: '100%',
      '& > div': {
        width: '100%',
      },
    },
  },
  verticalCenter: {
    alignItems: 'center',
    display: 'flex',
  },
  titleText: {
    ...theme.typography.styles.largestMessage,
  },
  contentText: {
    ...theme.typography.styles.largeMessage,
    paddingBottom: `${theme.units.sm}px`,
    paddingTop: `${theme.units.sm}px`,
  },
  buttonContainer: {
    display: 'flex',
  },
  loginButton: {
    paddingLeft: `${theme.units.sm}px`,
  },
}))

const LUMError = function LUMError({ errorType, titleText, contentText }) {
  const classes = useStyles(useTheme())

  function getErrorImage(error) {
    switch (error) {
      case ERROR_TYPES.Forbidden:
        return Forbidden
      case ERROR_TYPES.ServerError:
        return ServerError
      case ERROR_TYPES.PageNotFound:
      default:
        return PageNotFound
    }
  }

  return (
    <div id="lum-error-root" className={classes.root}>
      <div id="lum-error-img-container">
        <img src={getErrorImage(errorType)} alt={titleText} />
      </div>
      <div id="lum-error-text-container" className={classes.verticalCenter}>
        <div>
          <div className={classes.titleText}>{titleText}</div>
          <div className={classes.contentText}>{contentText}</div>
          <div className={classes.buttonContainer}>
            <LUMButton
              variant="brand"
              label="Return home"
              link={{
                type: 'internal',
                href: APP_ROUTES.Home,
              }}
            />
            {errorType === ERROR_TYPES.Forbidden ? (
              <div className={classes.loginButton}>
                <LUMButton
                  variant="brand"
                  label="Login as a different user"
                  link={{
                    type: 'internal',
                    href: APP_ROUTES.Logout,
                  }}
                />
              </div>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  )
}

LUMError.propTypes = {
  errorType: PropTypes.number.isRequired,
  titleText: PropTypes.string.isRequired,
  contentText: PropTypes.string.isRequired,
}

export default LUMError

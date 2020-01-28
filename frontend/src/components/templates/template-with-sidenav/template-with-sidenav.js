import React from 'react'
import PropTypes from 'prop-types'
import withStyles from 'react-jss'
import { ERROR_TYPES } from '../../../data/constants'
import { TPMainContainer, TPContentContainer, TPContentHeader } from 'Components/templates/template-parts/'
import LUMError from 'Components/molecules/lum-error'
import LUMPageTitle from 'Components/atoms/lum-page-title'

const styles = {
  root: {
    display: 'flex',
    flexDirection: 'row',
  },
}

export const TemplateWithSidenav = function TemplateWithSidenav({
  classes, globalHeaderEl, growContent, pageError,
  pageHeaderEl, pageHeaderTransparentEl, pageContentEl, pageTitle, sideNavEl,
}) {
  const headerEl = pageHeaderEl || pageHeaderTransparentEl
  let headerVariant

  if (headerEl) {
    if (pageHeaderTransparentEl) {
      headerVariant = 'transparent'
    }
    else {
      headerVariant = ''
    }
  }

  return (
    <div>
      <div>{globalHeaderEl}</div>
      <div className={classes.root}>
        <aside className="no-print-display">
          {sideNavEl}
        </aside>
        <TPMainContainer>
          {pageError ? (
            <>
              {pageTitle && (
                <TPContentHeader>
                  <LUMPageTitle titleText={pageTitle} />
                </TPContentHeader>
              )}
              <TPContentContainer growContent={growContent}>
                <LUMError
                  errorType={ERROR_TYPES.ServerError}
                  titleText="Unexpected error"
                  contentText="Uh oh, there seems to be a problem. Let me help you find a way out of here."
                />
              </TPContentContainer>
            </>
          ) : (
            <>
              {headerEl && (
              <TPContentHeader variant={headerVariant}>
                {headerEl}
              </TPContentHeader>
              )}
              <TPContentContainer growContent={growContent}>
                {pageContentEl}
              </TPContentContainer>
            </>
          )}
        </TPMainContainer>
      </div>
    </div>
  )
}

TemplateWithSidenav.defaultProps = {
  growContent: false,
  pageError: null,
  pageTitle: '',
  pageHeaderEl: null,
  pageHeaderTransparentEl: null,
}

TemplateWithSidenav.propTypes = {
  globalHeaderEl: PropTypes.node.isRequired,
  pageError: PropTypes.object,
  pageHeaderEl: PropTypes.node,
  pageHeaderTransparentEl: PropTypes.node,
  pageContentEl: PropTypes.node.isRequired,
  pageTitle: PropTypes.string,
  sideNavEl: PropTypes.node.isRequired,
  classes: PropTypes.object.isRequired,
  growContent: PropTypes.bool,
}

export default withStyles(styles)(TemplateWithSidenav)

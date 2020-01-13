import React from 'react'
import PropTypes from 'prop-types'
import { ERROR_TYPES } from 'Data/constants'
import { TPMainContainer, TPContentContainer, TPContentHeader } from 'Components/templates/template-parts/'
import LUMError from 'Components/molecules/lum-error'
import LUMPageTitle from 'Components/atoms/lum-page-title'

const TemplateFullWidth = function TemplateFullWidth({
  globalHeaderEl, pageError, pageHeaderEl, pageContentEl, pageTitle, growContent,
}) {
  return (
    <div>
      <div>
        {globalHeaderEl}
      </div>
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
            {pageHeaderEl && (
            <TPContentHeader>
              {pageHeaderEl}
            </TPContentHeader>
            )}
            <TPContentContainer growContent={growContent}>
              {pageContentEl}
            </TPContentContainer>
          </>
        )}
      </TPMainContainer>
    </div>
  )
}

TemplateFullWidth.defaultProps = {
  growContent: false,
  pageHeaderEl: false,
  pageError: null,
  pageTitle: '',
}

TemplateFullWidth.propTypes = {
  globalHeaderEl: PropTypes.node.isRequired,
  pageError: PropTypes.object,
  pageHeaderEl: PropTypes.node,
  pageContentEl: PropTypes.node.isRequired,
  pageTitle: PropTypes.string,
  growContent: PropTypes.bool,
}

export default TemplateFullWidth

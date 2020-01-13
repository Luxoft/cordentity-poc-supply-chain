import React from 'react'
import PropTypes from 'prop-types'

/**
 *
 * TPContentContainer is the main grid assignment for post-header content within a page
 *
 *
 */
const TPContentContainer = function TPContentContainer({ children, growContent }) {
  const growStyle = growContent ? { flexGrow: 1 } : { flexGrow: 0 }
  const growClass = growContent ? '' : 'slds-p-horizontal_large'

  return (
    /*
      this is the full-width container that sets the content side padding
      in the lumedic design language these are the "grid margins"
    */
    <div style={{
      ...growStyle,
    }}
    >
      {/*
        this is the foundation grid element for the 12 columns.
        it extends to the very edges of it's parent components.
      */}
      <div
        className="slds-grid"
        style={{
          flexWrap: 'wrap',
          height: '100%',
        }}
      >
        <div className={`slds-col ${growClass}`}>
          { children }
        </div>
      </div>
    </div>
  )
}

TPContentContainer.defaultProps = {
  growContent: false,
}

TPContentContainer.propTypes = {
  children: PropTypes.oneOfType([PropTypes.node, PropTypes.array]).isRequired,
  growContent: PropTypes.bool,
}

export default TPContentContainer

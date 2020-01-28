import React from 'react'
import PropTypes from 'prop-types'
import { HEADER_HEIGHT } from 'Data/constants'

/**
 *
 * TPMainContainer sets the height and scroll for page content, including the page header.
 *
 */
const TPMainContainer = function TPMainContainer({ children }) {
  return (
    <main
      className=""
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: `calc(100vh - ${HEADER_HEIGHT})`,
        overflowY: 'scroll',
        width: '100%',
      }}
    >
      {children}
    </main>
  )
}

TPMainContainer.propTypes = {
  children: PropTypes.oneOfType([PropTypes.node, PropTypes.array]).isRequired,
}

export default TPMainContainer

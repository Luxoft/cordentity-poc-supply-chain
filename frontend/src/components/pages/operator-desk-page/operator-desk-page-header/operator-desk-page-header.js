import React from 'react'
import PropTypes from 'prop-types'
import LUMPageTitle from 'Components/atoms/lum-page-title'

export default function OperatorDeskPageHeader({ userMeta }) {
  return (
    <LUMPageTitle titleText={'Please scan the code below to receive your Providence digital ID'} />
  )
}

OperatorDeskPageHeader.propTypes = {
  userMeta: PropTypes.object.isRequired,
}

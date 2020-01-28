import React from 'react'
import SLDSOverrides from './slds-overrides'
import LumedicGlobals from './lumedic-globals'

const GlobalStyles = function GlobalStyles() {
  return (
    <>
      <SLDSOverrides />
      <LumedicGlobals />
    </>
  )
}

export default GlobalStyles

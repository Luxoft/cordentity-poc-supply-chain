import api from 'Classes/api'

export const fetchPowerBIReportFilters = async (reportId) => {
  try {
    const data = await api.powerBI.getPowerBIReportFilters(reportId)

    return data
  }
  catch (error) {
    console.error('There was an error fetching the report filters', error)

    throw error
  }
}

export const fetchPowerBIReportConfig = async (workspaceId, reportId) => {
  try {
    const data = await api.powerBI.getPowerBIReportToken(workspaceId, reportId)

    return data
  }
  catch (error) {
    console.error('There was an error fetching the report token', error)

    throw error
  }
}

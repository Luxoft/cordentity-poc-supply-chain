import api from 'Classes/api'
import { fetchPowerBIReportConfig, fetchPowerBIReportFilters } from './template-power-bi-data-loaders'

jest.mock('Classes/api')

describe('The powerbi data loaders happy paths', () => {
  jest.resetAllMocks()

  it('should fetch report config from the api', async () => {
    api.powerBI.getPowerBIReportToken.mockReturnValueOnce({ data: 'token' })

    const payload = await fetchPowerBIReportConfig('workspaceId', 'reportId')

    expect(api.powerBI.getPowerBIReportToken).toHaveBeenCalledTimes(1)
    expect(api.powerBI.getPowerBIReportToken).toBeCalledWith('workspaceId', 'reportId')
    expect(payload.data).toBe('token')
  })

  it('should fetch report filters from the api', async () => {
    api.powerBI.getPowerBIReportFilters.mockReturnValueOnce({ data: 'filters' })

    const payload = await fetchPowerBIReportFilters('reportId')

    expect(api.powerBI.getPowerBIReportFilters).toHaveBeenCalledTimes(1)
    expect(api.powerBI.getPowerBIReportFilters).toBeCalledWith('reportId')
    expect(payload.data).toBe('filters')
  })
})

describe('The powerbi data loaders error paths', () => {
  jest.resetAllMocks()

  it('should throw an error if there is a problem fetching the token', async () => {
    api.powerBI.getPowerBIReportToken.mockImplementationOnce(() => {
      throw new Error('tokenError')
    })

    try {
      await fetchPowerBIReportConfig(null)
    }
    catch (err) {
      expect(err.message).toBe('tokenError')
    }
  })

  it('should throw an error if there is a problem fetching the filters', async () => {
    api.powerBI.getPowerBIReportFilters.mockImplementationOnce(() => {
      throw new Error('filterError')
    })

    try {
      await fetchPowerBIReportFilters(null)
    }
    catch (err) {
      expect(err.message).toBe('filterError')
    }
  })
})

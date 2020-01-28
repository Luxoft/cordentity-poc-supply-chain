import fetch from 'unfetch'

class CredentialsService {
    async auth(request) {
        const response = await fetch(
            `${process.env.API_BASE_URL}/api/hospital/auth`,
            {
                method: 'POST',
                body: JSON.stringify(request),
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            }
        );
        return await response.json()
    }
    
    async checkStatus(requestId) {
        const response = await fetch(
            `${process.env.API_BASE_URL}/api/hospital/auth/${requestId}`,
            {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            }
        );
        return await response.json()
    }
}
const credentialsService = new CredentialsService()
export default credentialsService

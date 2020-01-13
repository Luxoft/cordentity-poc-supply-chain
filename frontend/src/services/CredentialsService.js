import fetch from 'unfetch'

class CredentialsService {
    async issueCredentials(request) {
        const response = await fetch(
            `${process.env.API_BASE_URL}/api/credential/issueCredentials`,
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
}
const credentialsService = new CredentialsService()
export default credentialsService

//const baseUrl = window.location.origin;
const baseUrl = 'http://localhost:8080';

function makeUrl(path: string) {
    return `${baseUrl}/${path}`
}

export interface IInvite {
    invite: string;
    clientUUID?: string;
}

export async function getInvite(): Promise<IInvite> {
    const response = await fetch(makeUrl('api/tc/invite'), {method: 'GET'});
    return response.json()
}

const fs = require('fs')

console.log('Generating .env file')

const data = fs.readFileSync('example.azure.env', 'utf8')

console.log('-- example file read')

const result = data
.replace(/#{ENV_NAME}#/g, 'local')
.replace(/#{API_BASE_URL}#/g, 'http://13.92.115.239:8081')

console.log('-- contents replaced')

fs.writeFileSync('.env', result, 'utf8')

console.log('-- .env file written')

console.log('')

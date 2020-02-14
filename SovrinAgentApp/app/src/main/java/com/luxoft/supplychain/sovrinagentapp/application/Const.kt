package com.luxoft.supplychain.sovrinagentapp.application

const val AUTHORITIES = "authorities"
const val EXTRA_SERIAL = "serial"
const val EXTRA_STATE = "state"
const val EXTRA_COLLECTED_AT = "collected_at"
const val TIME = "time"

const val NOTIFICATION_CHANNEL_ID = "10001"
const val NOTIFICATION_CHANNEL_NAME = "INDY_NOTIFICATION_CHANNEL"

const val GENESIS_PATH = "./docker.txn"
const val TAILS_PATH = "./tails"

//const val nodeIp = """52.15.95.244"""  // amazon
//const val GENESIS_IP = """52.15.95.244"""  // azure
const val GENESIS_IP = """52.224.217.165"""  // azure


//const val WS_ENDPOINT = "ws://localhost:8094/ws"
//const val WS_ENDPOINT = "ws://13.92.115.239:8094/ws"  // azure
//const val WS_ENDPOINT = "ws://3.17.65.252:8094/ws"  // amazon
const val WS_ENDPOINT = "ws://52.224.104.135:8094/ws"  // client

const val WS_LOGIN = "medical-supplychain"
const val WS_PASS = "secretPassword"

const val QR_SCANNER_CODE_EXTRA = "com.blikoon.qrcodescanner.got_qr_scan_relult"

const val sharedPreferencesRequstedDataName = "REQUESTED_DATA_SP"
const val sharedPreferencesRequstedDataKey = "REQUESTED_DATA_KEY"

const val sharedPreferencesLastConnectionDiDName = "LAST_CONNECTION_DID_SP"
const val sharedPreferencesLastConnectionDiDKey = "LAST_CONNECTION_DID_KEY"

const val sharedPreferencesLastInviteUrlName = "LAST_INVITE_URL"
const val sharedPreferencesLastInviteUrlKey = "LAST_INVITE_URL_KEY"

const val BASE_URL = "http://localhost:8082"

const val FIELD_KEY = "key"
const val NAME = "name"
const val FIELD_COLLECTED_AT = "collectedAt"
const val FIELD_REQUESTED_AT = "requestedAt"
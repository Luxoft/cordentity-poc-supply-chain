package com.luxoft.supplychain.sovrinagentapp.application

const val AUTHORITIES = "authorities"
const val EXTRA_SERIAL = "serial"
const val EXTRA_STATE = "state"
const val EXTRA_COLLECTED_AT = "collected_at"

const val NOTIFICATION_CHANNEL_ID = "10001"
const val NOTIFICATION_CHANNEL_NAME = "INDY_NOTIFICATION_CHANNEL"

const val GENESIS_PATH = "/sdcard/docker.txn"
const val TAILS_PATH = "/sdcard/tails"

//const val nodeIp = """52.15.95.244"""  // amazon
const val GENESIS_IP = """52.224.217.165"""  // azure


//const val WS_ENDPOINT = "ws://localhost:8094/ws"
//const val WS_ENDPOINT = "ws://13.92.115.239:8094/ws"  // azure
//const val WS_ENDPOINT = "ws://3.17.65.252:8094/ws"  // amazon
const val WS_ENDPOINT = "ws://52.224.104.135:8094/ws"  // client

const val WS_LOGIN = "medical-supplychain"
const val WS_PASS = "secretPassword"

const val QR_SCANNER_CODE_EXTRA = "com.blikoon.qrcodescanner.got_qr_scan_relult"
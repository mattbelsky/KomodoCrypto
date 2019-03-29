# Komodo Crypto

Komodo Crypto is an automated cryptocurrency trading application that makes trades when a profitable arbitrage opportunity is detected between exchanges, and its functionalities are accessible via HTTP endpoints. Komodo employs Knowm's excellent [XChange](https://github.com/knowm/XChange) library to interact with the exchanges, retrieving ticker data and user account information, as well as executing trades.
  
## Deployment

In order to run the application, the user will need verified accounts, API keys and secret keys for each of the exchanges, as well as wallets for each of the currencies you want to trade on each exchange. These should all be stored in the application.properties file. Be aware that some currencies may be traded under different names on each exchange, and some wallets may require a tag in addition to an address. Please ensure that all accounts have been verified on each exchange before running the application, as verification may take some time.

The following are the current variables used to store these data in application.properties:

```
##----Binance----
binance.apiKey
binance.secretKey
binance.wallet.btc.id
binance.wallet.eth.id
binance.wallet.bch.id
binance.wallet.ltc.id
binance.wallet.xrp.id
binance.wallet.xrp.tag

##----Bittrex----
bittrex.username
bittrex.apiKey
bittrex.secretKey
bittrex.wallet.btc.id
bittrex.wallet.eth.id
bittrex.wallet.bch.id
bittrex.wallet.ltc.id
bittrex.wallet.xrp.id

##----Coinbase Pro----
coinbasepro.apiKey
coinbasepro.secretKey
coinbasepro.passphrase
coinbasepro.wallet.btc.id
coinbasepro.wallet.eth.id
coinbasepro.wallet.bch.id
coinbasepro.wallet.ltc.id
coinbasepro.wallet.usdc.id

#----Kraken----
kraken.apiKey
kraken.privateKey
kraken.wallet.btc.id
kraken.wallet.eth.id
kraken.wallet.bch.id
kraken.wallet.ltc.id
kraken.wallet.xrp.id
kraken.wallet.xrp.tag
```

More exchanges and currencies can be added as desired following this format. However, in addition to modifying application.properties, the user will need to add the exchanges to the methods formatExchangeName() and createExchange() in ExchangeService and follow the pattern within ExchangesConfig to add the relevant fields and methods.

## Security

The API is secured with JSON web tokens. To obtain a token, a registered user must pass their credentials as a JSON object in the body of a POST request at the `/login` endpoint:
```
{
	"username": "<username>",
	"password": "<password>"
}
```
The resulting token must then be passed with every request in the Authorization header.

## Endpoints

### Users

```
/user
```
[POST] Registers a new user. Username, password, and email must be included in the request body as JSON in the following form:
```
{
	"username": "<usename>",
	"password": "<password>",
	"email": "<email>"
}
```

### Exchanges: User Account and Supported Currency Data

```
/api/{exchange}/currencypairs
```
[GET] Gets a list of currency pairs supported by the specified exchange. If base is null, returns all supported pairs with the specified counter. If counter is null, returns all supported pairs with the specified base. Otherwise, if base and counter are either both or neither included, returns all supported pairs.

Request parameters (both optional):
* base - i.e. BTC in the currency pair BTC/USD
* counter - i.e. USD in the pair BTC/USD

```
/api/{exchange}/account
```
[GET] Gets the user's account information for the specified exchange -- trading fees, wallet data, etc.

```
/api/{exchange}/wallet
```
[GET] Gets the wallet balance for the specified exchange and currency.

Request parameter (required):
* currency - i.e. BTC

```
/api/{exchange}/wallet/address
```
[GET] Gets the wallet address for the specified exchange and currency. Generally used for testing purposes.

Request parameter (required):
* currency

### Make Trades and Withdraw Currency

```
/api/{exchange}/trade/market
```
[POST] Makes market trades of the specified amount and base and counter currency on the specified exchange.

Request parameters (required):
* base
* counter
* amount

```
/api/{exchange}/withdraw
```
[POST] Withdraws the specified amount of currency from the wallet of one exchange to that of another.

Request parameters (required):
* toexchange - the exchange to withdraw to
* currency
* amount

### Scan for and Trade Arbitrage Opportunities

```
/api/arbitrage
```
[POST] Detects and trades on arbitrage opportunities on a fixed delay.

Request parameter (required):
* milliseconds - the interval after the previous execution has finished to begin the next

[DELETE] Stops the scanning and trading operation. No request parameter needed

## Tech Stack
* Java 8
* Maven
* Spring Boot
* MyBatis
* MySQL


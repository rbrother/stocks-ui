# Finnish Stock Monitor

A ClojureScript re-frame SPA for monitoring Finnish stocks from the Helsinki Exchange (HEX).

## Features

- **Stock Selection**: Search and select from 15 major Finnish stocks including Nokia, Neste, Sampo, UPM, and more
- **Interactive Charts**: View stock price development over different time scales (1 day to 2 years) using Chart.js
- **Real-time Data**: Fetch current stock prices and historical data from Yahoo Finance API
- **Company Financials**: Display key financial metrics including P/E ratio, market cap, dividend yield, and more
- **Responsive Design**: Fully responsive UI that works on desktop, tablet, and mobile devices
- **Local Storage**: Persist user's selected stocks and preferences across browser sessions
- **Market Status**: Shows whether the Helsinki market is currently open or closed

## Technology Stack

- **ClojureScript**: Modern Lisp for functional programming in the browser
- **re-frame**: Reactive framework for building SPAs with unidirectional data flow
- **Reagent**: ClojureScript interface to React
- **Shadow-cljs**: Build tool for ClojureScript
- **Chart.js**: Interactive charts for stock price visualization
- **Yahoo Finance API**: Free stock data source for Finnish stocks

## Finnish Stocks Included

The application includes 15 major Finnish stocks from the HEX exchange:

- **NOKIA.HE** - Nokia Oyj (Technology)
- **NESTE.HE** - Neste Oyj (Energy)
- **SAMPO.HE** - Sampo Oyj (Financial Services)
- **UPM.HE** - UPM-Kymmene Oyj (Materials)
- **FORTUM.HE** - Fortum Oyj (Utilities)
- **STORA.HE** - Stora Enso Oyj (Materials)
- **KESKO.HE** - Kesko Oyj (Consumer Discretionary)
- **METSO.HE** - Metso Outotec Oyj (Industrials)
- **ELISA.HE** - Elisa Oyj (Communication Services)
- **TELIA1.HE** - Telia Company AB (Communication Services)
- **NORDEA.HE** - Nordea Bank Abp (Financial Services)
- **ORION.HE** - Orion Oyj (Healthcare)
- **KOJAMO.HE** - Kojamo Oyj (Real Estate)
- **KONE.HE** - KONE Oyj (Industrials)
- **WARTSILA.HE** - Wärtsilä Oyj (Industrials)

## Getting Started

### Prerequisites

- Node.js (v14 or later)
- Java 8 or later (for ClojureScript compilation)

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd stocks-ui
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

4. Open your browser and navigate to `http://localhost:8082`

### Building for Production

```bash
npm run build
```

The compiled application will be in the `resources/public` directory.

## Usage

1. **Select Stocks**: Use the search bar to find stocks by symbol, name, or sector. Click on stocks to add them to your watchlist.

2. **View Charts**: Selected stocks will appear in the main chart area. Use the time period buttons to change the chart timeframe.

3. **Stock Details**: The sidebar shows detailed information about the first selected stock, including current price, financial metrics, and company information.

4. **Persistent Preferences**: Your selected stocks and time period preferences are automatically saved and restored when you return to the application.

## Data Source

**Current Implementation**: The application uses realistic mock data for demonstration purposes to avoid CORS issues and ensure reliable functionality. The mock data includes:
- Realistic Finnish stock prices with daily variations
- Historical price trends over different time periods
- Simulated network delays for authentic user experience

**Production Ready**: To use real stock data, replace the mock functions in `src/stocks_ui/api.cljs`:
- `enhanced-fetch-quote` - Replace with actual Yahoo Finance API calls
- `enhanced-fetch-historical` - Replace with real historical data fetching
- Consider using a backend proxy server to handle CORS restrictions

The mock data provides a fully functional demonstration of all features while eliminating external dependencies.

## Development

### Project Structure

```
src/stocks_ui/
├── core.cljs          # Application entry point
├── config.cljs        # Configuration and stock definitions
├── db.cljs           # Database schema
├── events.cljs       # re-frame events
├── subs.cljs         # re-frame subscriptions
├── views.cljs        # UI components
├── charts.cljs       # Chart components
├── api.cljs          # API client
└── storage.cljs      # Local storage utilities
```

### Key Components

- **Stock Selector**: Search and selection interface
- **Charts Section**: Interactive price charts with time controls
- **Stock Details Panel**: Company information and financials
- **Market Status**: Real-time market open/closed indicator

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

MIT License - see LICENSE file for details.

## Disclaimer

This application is for educational and informational purposes only. Stock data is delayed and should not be used for actual trading decisions. Always consult with a financial advisor before making investment decisions.

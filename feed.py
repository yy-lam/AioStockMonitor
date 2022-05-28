import os
import time
from polygon import RESTClient
import pandas as pd

# tickers = list(pd.read_csv("S&P500.csv")["Symbol"])

if __name__ == '__main__':
    api_key = os.getenv('POLYGON_API')
    client = RESTClient(api_key=api_key)
    response = client.list_ticker_news('SPY', limit=10)
    while True:
        try:
            news = next(response)
        except Exception as e:
            print(e)
            break
        else:
            if not news or not news.tickers or not news.description:
                continue
            tickers = ', '.join(news.tickers)
            headline = '. '.join(news.description.split('\n'))
            print(f"{tickers}|{headline}")
            time.sleep(0.1)

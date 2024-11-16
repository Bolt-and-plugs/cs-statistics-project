from pickle import load
import pandas as pd
import sys

data = pd.read_csv('carlos.csv', index_col='Unnamed: 0')

with open("model.pkl", "rb") as f:
    model = load(f)
    pred = model.predict(pd.DataFrame(data.loc[int(sys.argv[1])]).T)

    print(pred[0], end='')

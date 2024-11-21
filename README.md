# cs-statistics-project

[Documentation](docs/main.pdf)


# Running locally
## Server:
```bash
cd csgo-api
npm i
npm run start
```

## Client
``` bash
hostname i # get your ip
```

Insert your 192.*.* ip in the client on [cljs.db](./pred-cs/src/main/csgo/db.cljs)

You will need to have expo @52.0.0 installed and also the deps
```
npm i -g expo-cli
npx expo install
npm run compile
npm run start
```

Now, just connects the development server into a emulator or your smartphone. 
You will need to install expo go in order to see the result in your smartphone.

To understand the project, see our [Oficial Documentation](docs/main.pdf)

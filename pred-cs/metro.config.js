const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

config.resolver.assetExts.push('csv');

config.watchFolders = [
  ...config.watchFolders || [],
  `${__dirname}/assets`
];

config.resolver.extraNodeModules = new Proxy({}, {
  get: (target, name) => {
    if (name === 'assets') {
      return `${__dirname}/assets`;
    }
    return name;
  },
});

module.exports = config;

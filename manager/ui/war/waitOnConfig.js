module.exports = {
    auth: {
      username: 'admin',
      password: 'admin123!'
    },
    strictSSL: false,
    followRedirect: false,
    validateStatus: function (status) {
        // 401 is a hack because someone disabled direct grants api
        return status == 200 || status == 401;
    }
  };
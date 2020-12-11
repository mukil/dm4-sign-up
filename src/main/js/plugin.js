export default ({dmx, axios}) => ({

  components: [
    {
      comp: require('./components/sign-up-menu').default,
      mount: 'toolbar-right'
    }
  ],
  loginExtensions: [
      require('./components/forgot-password-link').default
  ]

})
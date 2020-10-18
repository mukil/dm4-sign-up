export default ({dm5, axios}) => ({

  components: [
    {
      comp: require('./components/sign-up-menu').default,
      mount: 'toolbar-right'
    }
  ]

})
<template>
    <div class="dm5-sign-up">
        <el-button size="mini" v-if="!isLoggedIn && showSignupButton">
            Sign up
        </el-button>
    </div>
</template>

<script>
export default {

  inject: {
    http: 'axios'
  },

  created: function () {
    this.checkSelfRegistrationActive()
  },

  data: {
    showSignupButton: false
  },

  computed: {
    isLoggedIn () {
      return (this.$store.state.accesscontrol.username)
    }
  },

  methods: {
    checkSelfRegistrationActive: function() {
      this.http.get(`/sign-up/self-registration-active`).then(response => {
        this.showSignupButton = (response.data == true)
      })
    }
  }

}
</script>


<style>
.dm5-sign-up button {
    margin-left: .75em;
    /** To align with Login State Menu: Padding and Font-Size copied from .dm5-login-state .el-button.sign-in **/
    font-size: 12px !important;
    padding: 5px 8px !important;
}
</style>

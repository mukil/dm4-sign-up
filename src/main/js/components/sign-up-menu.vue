<template>
    <div class="sign-up-menu">
        <el-button size="mini" v-if="!isLoggedIn && showSignupButton">
            <el-link href="/sign-up" :underline="false">Sign up</el-link>
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
.sign-up-menu button {
    font-size: var(--label-font-size);
    margin-left: 1em;
}
</style>

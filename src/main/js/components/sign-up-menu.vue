<template>
    <div class="sign-up-menu">
        <el-link v-if="!isLoggedIn" href="/sign-up" :underline="false">Sign-up<!--i class="fa fa-user-plus"></i--></el-link>
    </div>
</template>

<script>
export default {
  computed: {
    isLoggedIn () {
      return (this.$store.state.accesscontrol.username)
    }
  }
}
</script>


<style>
.sign-up-menu {
    font-size: var(--label-font-size) !important;
    margin-left: 1em;
}
</style>

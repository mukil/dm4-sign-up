<template>
    <div class="sign-up-menu">
        <el-button size="mini" v-if="!isLoggedIn">
            <el-link href="/sign-up" :underline="false">Sign up</el-link>
        </el-button>
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
.sign-up-menu button {
    font-size: var(--label-font-size);
    margin-left: 1em;
}
</style>

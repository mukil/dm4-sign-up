<template>
    <div class="forgot-password label">
        <el-link v-if="showForgotPasswordLink" href="/sign-up/request-password/" :underline="false">Forgot password?</el-link>
    </div>
</template>

<script>
export default {
  computed: {
    showForgotPasswordLink () {
      return true
    }
  }
}
</script>

<style>
.forgot-password {
    margin-top: 1em;
}
</style>

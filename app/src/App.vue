<script>
export default {
  onLaunch() {
    console.log('App Launch')
  }
}
</script>

<style lang="scss">
/* 引入设计系统 */
@import '@/styles/design-system.scss';

/* 全局样式 */
page {
  font-family: $font-family-base;
  color: $color-text-primary;
  background-color: $color-background;
}

.container {
  padding: $space-md;
}

/* 安全区域适配 */
.safe-area-bottom {
  @include safe-area-bottom;
}
</style>

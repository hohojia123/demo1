<template>
  <el-card id="introduction">
    <!--<hr />-->
    <p>
      <span style="color:#67C23A" class="el-icon-location-information">本站介绍</span>
    </p>
    <hr />
    {{ introduction }}
    <br/><br/><br/>
  </el-card>
</template>

<script>
  // Deleted:import site from '@/api/site'

  export default {
    name: 'introduction',
    data() {
      return {
        introduction: '欢迎来到我的博客系统，这里分享技术文章和学习心得。'
      }
    }
    // Deleted:,
    // Deleted:created() {
    // Deleted:  site.getSite().then(responese => {
    // Deleted:    this.introduction = responese.data;
    // Deleted:  });
    // Deleted:}

  }
</script>
<style scoped>
  #introduction {
    /*-moz-box-shadow: 0px 6px 0px #333333;*/
    /*-webkit-box-shadow: 0px 6px 0px #333333;*/
    /*box-shadow: 0px 3px 10px #333333;*/
    text-align: center;

    margin: 20px 0;
  }
</style>

<template>
  <el-card id="newDiscuss">
    <!--<hr />-->
    <p>
      <span style="color:#67C23A" class="el-icon-chat-line-square">最新评论</span>
    </p>
    <hr/>
    <div v-for="discuss in discussList" style="margin: 10px 0; text-align: left;">
      <span style="cursor: pointer; color: #909399;" @click="router(discuss.blog.id)">
        <img v-if="discuss.user.avatar" :src="discuss.user.avatar" style="width: 24px; height: 24px; border-radius: 50%; vertical-align: middle; margin-right: 5px; object-fit: cover;">
        <i v-else class="el-icon-user-solid" style="font-size: 24px; vertical-align: middle; margin-right: 5px;"></i>
        {{discuss.user.name}}&nbsp;:&nbsp;{{discuss.body}}《{{discuss.blog.title}}》
      </span>
    </div>
    <br/>
  </el-card>
</template>

<script>
  import discuss from '@/api/discuss'

  export default {
    name: 'introduction',
    data() {
      return {
        discussList: ''
      }
    },
    created() {
      discuss.getNewDiscuss().then(responese => {
        this.discussList = responese.data;
      });
    },
    methods: {
      router(id) {
        scrollTo(0, 0);
        this.$router.push({ //路由跳转
          path: '/blog/'+id
        })
      }
    }

  }
</script>
<style scoped>
  #newDiscuss {
    /*-moz-box-shadow: 0px 6px 0px #333333;*/
    /*-webkit-box-shadow: 0px 6px 0px #333333;*/
    /*box-shadow: 0px 3px 10px #333333;*/
    text-align: center;

    margin: 20px 0;
  }
</style>

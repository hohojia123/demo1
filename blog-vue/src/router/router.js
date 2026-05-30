import Vue from 'vue'
import Router from 'vue-router'

import index from '@/views/index'
import newBlog from '@/views/newBlog'
import account from '@/views/account'
import admins from '@/views/admins'
import forgetPwd from '@/views/forgetPwd'
import searchBlog from '@/views/searchBlog'
import blog from '@/views/blog'
import myBlog from '@/views/myBlog'
import editBlog from '@/views/editBlog'
import chat from '@/views/chat'
import notfound from '@/views/notfound'

import userManage from '@/views/userManage'
import blogManage from '@/views/blogManage'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      component: index
    },
    {
      path: '/newBlog',
      name: 'newBlog',
      component: newBlog
    },
    {
      path: '/account',
      name: 'account',
      component: account
    },
    {
      path: '/admins',
      name: 'admins',
      component: admins,
      children: [  //这里就是二级路由的配置
        {
          path: 'userManage',
          name: 'userManage',
          component: userManage
        },
        {
          path: 'blogManage',
          name: 'blogManage',
          component: blogManage
        }
      ]
    },
    {
      path: '/forgetPwd',
      name: 'forgetPwd',
      component: forgetPwd
    },
    {
      path: '/searchBlog/:searchTxt',
      name: 'searchBlog',
      component: searchBlog
    },
    {
      path: '/blog/:blogId',
      name: 'blog',
      component: blog
    },
    {
      path: '/myBlog',
      name: 'myBlog',
      component: myBlog
    },
    {
      path: '/editBlog/:blogId',
      name: 'editBlog',
      component: editBlog
    },
    {
      path: '/chat',
      name: 'chat',
      component: chat
    },
    {
      path: '*',
      name: 'notfound',
      component: notfound
    }
  ]
})

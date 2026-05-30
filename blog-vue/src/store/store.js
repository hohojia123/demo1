import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    roles: localStorage.getItem('roles') == null ? '' : localStorage.getItem('roles'),
    token: localStorage.getItem('token') == null ? '' : localStorage.getItem('token'),
    name: localStorage.getItem('name') == null ? '' : localStorage.getItem('name'),
    avatar: localStorage.getItem('avatar') == null ? '' : localStorage.getItem('avatar')

  }, mutations: {
    login(state, data) {
      // 变更状态
      this.state.token = data.token;
      localStorage.setItem('token', data.token);

      // 存储用户名（从 user 对象中获取）
      if (data.user && data.user.name) {
        this.state.name = data.user.name;
        localStorage.setItem('name', data.user.name);
      }

      // 存储角色（从 user 对象中的 roles 数组获取）
      if (data.user && data.user.roles && data.user.roles.length > 0) {
        const roleNames = data.user.roles.map(role => role.name).join(',');
        this.state.roles = roleNames;
        localStorage.setItem('roles', roleNames);
      }

      // 存储头像
      if (data.user && data.user.avatar) {
        this.state.avatar = data.user.avatar;
        localStorage.setItem('avatar', data.user.avatar);
      }
    },
    logout(state) {
      localStorage.removeItem('token');
      this.state.token = '';
      localStorage.removeItem('name');
      this.state.name = '';
      localStorage.removeItem('roles');
      this.state.roles = '';
      localStorage.removeItem('avatar');
      this.state.avatar = '';
    },
    refresh(state, token) {
      this.state.token = token;
      localStorage.setItem('token', token);
    },
    updateAvatar(state, avatar) {
      this.state.avatar = avatar;
      localStorage.setItem('avatar', avatar);
    }
  }
})

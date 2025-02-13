import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    userAccount: localStorage.getItem('userAccount') || '',
    userAvatar: localStorage.getItem('userAvatar') || '',
    userRole: localStorage.getItem('userRole') || '',  // 添加角色状态
    isAuthenticated: !!localStorage.getItem('token'),
    hasActiveComplement: {
      type1: false, // 辅导补客资
      type2: false  // 申诉补客资
    },
    complementType: null  // 添加补客资类型状态
  },
  mutations: {
    setUserInfo(state, { account, avatarUrl, role }) {
      state.userAccount = account;
      state.userAvatar = avatarUrl;
      state.userRole = role;
      state.isAuthenticated = true;
      // 保存到 localStorage
      localStorage.setItem('userAccount', account);
      localStorage.setItem('userAvatar', avatarUrl);
      localStorage.setItem('userRole', role);
    },
    clearUserInfo(state) {
      state.userAccount = '';
      state.userAvatar = '';
      state.userRole = '';
      state.isAuthenticated = false;
      // 清除 localStorage
      localStorage.removeItem('userAccount');
      localStorage.removeItem('userAvatar');
      localStorage.removeItem('userRole');
      localStorage.removeItem('token');
    },
    setHasActiveComplement(state, { value, type }) {
      if (type === 1) {
        state.hasActiveComplement.type1 = value;
      } else if (type === 2) {
        state.hasActiveComplement.type2 = value;
      }
    }
  },
  actions: {
    login({ commit }, userInfo) {
      commit('setUserInfo', userInfo);
    },
    logout({ commit }) {
      commit('clearUserInfo');
    }
  },
  getters: {
    userAccount: state => state.userAccount,
    userAvatar: state => state.userAvatar,
    isAuthenticated: state => state.isAuthenticated,
    userRole: state => state.userRole,
    isAdmin: state => state.userRole === 'admin',
    hasActiveComplement: state => state.hasActiveComplement
  }
});
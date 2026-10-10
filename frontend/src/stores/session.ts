import { defineStore } from 'pinia'

export const useSession = defineStore('session', {
  state: () => ({
    token: localStorage.getItem('clinic_token') || '',
    displayName: localStorage.getItem('clinic_name') || ''
  }),
  actions: {
    save(token: string, displayName: string) {
      this.token = token
      this.displayName = displayName
      localStorage.setItem('clinic_token', token)
      localStorage.setItem('clinic_name', displayName)
    },
    clear() {
      this.token = ''
      this.displayName = ''
      localStorage.removeItem('clinic_token')
      localStorage.removeItem('clinic_name')
    }
  }
})

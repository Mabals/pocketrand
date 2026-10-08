export type User = {
  id: number
  fullName: string
  email: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  user: User
}
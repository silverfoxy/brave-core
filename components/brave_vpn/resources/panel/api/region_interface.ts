export interface Region {
  continent: string
  name: string
  namePretty: string
}
export interface RegionState {
  all: any,
  current: Region,
  hasError: boolean
}

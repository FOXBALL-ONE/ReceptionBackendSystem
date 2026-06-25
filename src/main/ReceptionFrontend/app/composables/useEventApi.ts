import type { Event, EventBasicPayload } from '~/types/event'

export const useEventApi = () => {
  const { post } = useHttp()
  const apiBase = (useRuntimeConfig().public.apiBase as string) || 'http://127.0.0.1:8080/api'

  const normalizeId = (id: string | number) => {
    const numericId = Number(id)

    return Number.isFinite(numericId) ? numericId : id
  }

  const toBasicPayload = (data: EventBasicPayload): EventBasicPayload => ({
    ...(data.id ? { id: data.id } : {}),
    url: data.url,
    masterTitle: data.masterTitle,
    subtitle: data.subtitle,
    name: data.name,
    startTime: data.startTime,
    endTime: data.endTime,
    bannerTags: data.bannerTags,
    bannerUrl: data.bannerUrl,
    isAnimation: data.isAnimation,
    isTopNavigationBar: data.isTopNavigationBar,
    icp: data.icp,
    technicalSupport: data.technicalSupport,
    isOpen: data.isOpen,
  })

  return {
    /**
     * 获取活动列表
     */
    getEventList: async () => {
      return await post<Event[]>('/activities/list', {}, { payloadMode: 'json' })
    },

    /**
     * 获取活动详情
     */
    getEventDetail: async (id: string | number) => {
      return await post<Event>('/activities/find-by-id', { id: normalizeId(id) }, { payloadMode: 'json' })
    },

    /**
     * 根据 URL 查询活动
     */
    getEventByUrl: async (url: string) => {
      return await post<Event>('/activities/find-by-url', { url }, { payloadMode: 'json' })
    },

    /**
     * 创建活动
     */
    createEvent: async (data: EventBasicPayload) => {
      return await post<Event>('/activities/save-one', toBasicPayload(data), { payloadMode: 'json' })
    },

    /**
     * 更新活动
     */
    updateEvent: async (id: string | number, activity: EventBasicPayload) => {
      return await post<Event>('/activities/update-one', {
        ...toBasicPayload(activity),
        id: normalizeId(id),
      }, { payloadMode: 'json' })
    },

    /**
     * 更新活动开放状态
     */
    updateEventOpenStatus: async (id: string | number, isOpen: boolean) => {
      return await post<Partial<Event> | null>('/activities/update-is-open', {
        id: normalizeId(id),
        isOpen,
      }, { payloadMode: 'json' })
    },

    /**
     * 删除活动
     */
    deleteEvent: async (id: string | number) => {
      return await post<void>('/activities/delete-one', { id: normalizeId(id) }, { payloadMode: 'json' })
    },

    /**
     * 获取已开放的活动列表
     */
    getOpenEvents: async () => {
      return await post<Event[]>('/activities/find-open', {}, { payloadMode: 'json' })
    },

    /**
     * 根据活动查询日程列表
     */
    getSchedulesByActivity: async (activityId: string | number) => {
      return await post<any[]>('/schedules/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    /**
     * 保存日程（创建或更新）
     */
    saveSchedules: async (activityId: string | number, schedules: any[]) => {
      return await post<any>('/schedules/save', {
        activityId: normalizeId(activityId),
        schedules,
      }, { payloadMode: 'json' })
    },

    /**
     * 上传日程相关文件
     */
    uploadScheduleFile: async (
      file: File,
      options: {
        activityId: string | number
        usageType: string
        uploadedBy?: string
      },
    ) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('activityId', String(normalizeId(options.activityId)))
      formData.append('usageType', options.usageType)
      formData.append('uploadedBy', options.uploadedBy ?? 'reception-frontend')

      return await post<any, FormData>('/images/upload', formData, {
        payloadMode: 'json',
        params: { activityId: normalizeId(options.activityId) },
      })
    },

    uploadInspectionPointImage: async (file: File, activityId: string | number) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('activityId', String(normalizeId(activityId)))
      formData.append('usageType', 'inspection')
      formData.append('uploadedBy', 'reception-frontend')

      return await post<any, FormData>('/images/upload', formData, {
        payloadMode: 'json',
        params: { activityId: normalizeId(activityId) },
      })
    },

    uploadBannerImage: async (file: File, activityId: string | number) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('activityId', String(normalizeId(activityId)))
      formData.append('usageType', 'banner')
      formData.append('uploadedBy', 'reception-frontend')

      return await post<any, FormData>('/images/upload', formData, {
        payloadMode: 'json',
        params: { activityId: normalizeId(activityId) },
      })
    },

    uploadOverviewImage: async (file: File, activityId: string | number) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('activityId', String(normalizeId(activityId)))
      formData.append('usageType', 'overview')
      formData.append('uploadedBy', 'reception-frontend')

      return await post<any, FormData>('/images/upload', formData, {
        payloadMode: 'json',
        params: { activityId: normalizeId(activityId) },
      })
    },

    /**
     * 根据活动查询人员列表
     */
    getPersonsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/persons/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    /**
     * 保存人员信息
     */
    savePersons: async (activityId: string | number, persons: any[]) => {
      return await post<any>('/persons/save', {
        activityId: normalizeId(activityId),
        persons,
      }, { payloadMode: 'json' })
    },

    /**
     * 根据活动查询车辆列表
     */
    getCarsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/cars/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    /**
     * 保存车辆信息
     */
    saveCars: async (activityId: string | number, cars: any[]) => {
      return await post<any>('/cars/save', {
        activityId: normalizeId(activityId),
        cars,
      }, { payloadMode: 'json' })
    },

    getMealsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/meals/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    saveMeals: async (activityId: string | number, meals: any[]) => {
      return await post<any>('/meals/save', {
        activityId: normalizeId(activityId),
        meals,
      }, { payloadMode: 'json' })
    },

    getLodgingsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/lodgings/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    getColorTagsByActivity: async (activityId: string | number, type?: string) => {
      return await post<any[]>('/color-tags/find-by-activity-id', {
        activityId: normalizeId(activityId),
        ...(type ? { type } : {}),
      }, { payloadMode: 'json' })
    },

    saveColorTags: async (activityId: string | number, items: any[]) => {
      return await post<any>('/color-tags/save-batch', {
        activityId: normalizeId(activityId),
        items,
      }, { payloadMode: 'json' })
    },

    deleteColorTags: async (ids: Array<string | number>) => {
      return await post<void>('/color-tags/delete-batch', {
        ids: ids.map(normalizeId),
      }, { payloadMode: 'json' })
    },

    saveLodgings: async (activityId: string | number, lodgings: any[], colorTags: any[]) => {
      return await post<any>('/lodgings/save', {
        activityId: normalizeId(activityId),
        colorTags,
        lodgings,
      }, { payloadMode: 'json' })
    },

    getInspectionPointsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/inspection-points/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    saveInspectionPoints: async (activityId: string | number, inspectionPoints: any[]) => {
      return await post<any>('/inspection-points/save', {
        activityId: normalizeId(activityId),
        inspectionPoints,
      }, { payloadMode: 'json' })
    },

    getOverviewsByActivity: async (activityId: string | number) => {
      return await post<any[]>('/overviews/find-by-activity-id', {
        activityId: normalizeId(activityId),
      }, { payloadMode: 'json' })
    },

    saveOverviews: async (activityId: string | number, items: any[]) => {
      return await post<any>('/overviews/save-batch', {
        activityId: normalizeId(activityId),
        items,
      }, { payloadMode: 'json' })
    },

    /**
     * 创建日程
     */
    createSchedule: async (activityId: string | number, schedule: any) => {
      return await post<any>('/schedules/create', { activityId, ...schedule }, { payloadMode: 'json' })
    },

    /**
     * 更新日程
     */
    updateSchedule: async (id: string | number, schedule: any) => {
      return await post<any>('/schedules/update', { id, schedule }, { payloadMode: 'json' })
    },

    /**
     * 删除日程
     */
    deleteSchedule: async (id: string | number) => {
      return await post<void>('/schedules/delete', { id }, { payloadMode: 'json' })
    },

    /**
     * 创建人员
     */
    createPerson: async (activityId: string | number, person: any) => {
      return await post<any>('/persons/create', { activityId, ...person }, { payloadMode: 'json' })
    },

    /**
     * 更新人员
     */
    updatePerson: async (id: string | number, person: any) => {
      return await post<any>('/persons/update', { id, person }, { payloadMode: 'json' })
    },

    /**
     * 删除人员
     */
    deletePerson: async (id: string | number) => {
      return await post<void>('/persons/delete', { id }, { payloadMode: 'json' })
    },

    /**
     * 获取考察组列表
     */
    getInspectionTeamItems: async (activityId?: string | number) => {
      if (activityId === undefined) {
        return await post<any[]>('/inspection-teams/find-by-activity-id', {}, { payloadMode: 'json' })
      }

      return await post<any[]>('/inspection-teams/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    /**
     * 整体保存考察组（身份 + 各天行程）：考察组跨天共用同一身份，每天行程独立。
     */
    saveInspectionTeams: async (activityId: string | number, items: any[]) => {
      return await post<any>('/inspection-teams/save-by-activity', {
        activityId: normalizeId(activityId),
        items,
      }, { payloadMode: 'json' })
    },

    /**
     * 根据活动获取温馨服务配置
     */
    getPromptServiceByActivity: async (activityId: string | number) => {
      return await post<any>('/prompt-services/find-by-activity-id', { activityId: normalizeId(activityId) }, { payloadMode: 'json' })
    },

    /**
     * 保存温馨服务配置
     */
    savePromptService: async (activityId: string | number, data: any) => {
      return await post<any>('/prompt-services/save', {
        activityId: normalizeId(activityId),
        ...data,
      }, { payloadMode: 'json' })
    },

    getWeatherForecast: async (location: string) => {
      return await post<any[]>('/weather/forecast', { location }, { payloadMode: 'json' })
    },

    /**
     * Excel 导入模板下载地址（GET，浏览器直接下载）
     */
    getExcelTemplateUrl: () => `${apiBase}/excel/template`,

    /**
     * 某个活动的 Excel 导出地址（GET，浏览器直接下载）
     */
    getExcelExportUrl: (id: string | number) =>
      `${apiBase}/excel/export?id=${encodeURIComponent(String(normalizeId(id)))}`,

    /**
     * 上传 Excel 并导入。传入 activityId 表示覆盖当前活动，否则创建新活动。
     */
    importExcel: async (
      file: File,
      options: { activityId?: string | number; name?: string } = {},
    ) => {
      const formData = new FormData()
      formData.append('file', file)
      if (options.activityId !== undefined && options.activityId !== null) {
        formData.append('activityId', String(normalizeId(options.activityId)))
      }
      if (options.name) {
        formData.append('name', options.name)
      }
      return await post<any, FormData>('/excel/import', formData, { payloadMode: 'json' })
    },
  }
}

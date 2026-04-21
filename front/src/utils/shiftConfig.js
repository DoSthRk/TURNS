export const MODULE_CATALOG = [
  {
    moduleKey: 'normal',
    moduleName: '官号轮班',
    countFieldName: 'countNormal',
    orderStatusFieldName: 'orderStatusNormal',
    waitingFieldName: 'waitingNormal'
  },
  {
    moduleKey: 'single1',
    moduleName: '主动留资轮班',
    countFieldName: 'countSingle1',
    orderStatusFieldName: 'orderStatusSingle1',
    waitingFieldName: 'waitingSingle1'
  },
  {
    moduleKey: 'sem',
    moduleName: 'SEM/托福轮班',
    countFieldName: 'countSem',
    orderStatusFieldName: 'orderStatusSem',
    waitingFieldName: 'waitingSem'
  },
  {
    moduleKey: 'extra1',
    moduleName: '自定义分类1',
    countFieldName: 'countExtra1',
    orderStatusFieldName: 'orderStatusExtra1',
    waitingFieldName: 'waitingExtra1'
  },
  {
    moduleKey: 'extra2',
    moduleName: '自定义分类2',
    countFieldName: 'countExtra2',
    orderStatusFieldName: 'orderStatusExtra2',
    waitingFieldName: 'waitingExtra2'
  },
  {
    moduleKey: 'extra3',
    moduleName: '自定义分类3',
    countFieldName: 'countExtra3',
    orderStatusFieldName: 'orderStatusExtra3',
    waitingFieldName: 'waitingExtra3'
  }
]

export const DEFAULT_TYPE_CONFIGS = [
  { consultantType: 1, typeLabel: '辅导', enabled: 1, displayOrder: 1 },
  { consultantType: 2, typeLabel: '申诉', enabled: 1, displayOrder: 2 },
  { consultantType: 4, typeLabel: '国际课程', enabled: 1, displayOrder: 3 },
  { consultantType: 3, typeLabel: '推月申诉', enabled: 1, displayOrder: 4 },
  { consultantType: 5, typeLabel: '推月辅导', enabled: 1, displayOrder: 5 },
  { consultantType: 6, typeLabel: '汇诺辅导', enabled: 1, displayOrder: 6 },
  { consultantType: 7, typeLabel: '汇诺申诉', enabled: 1, displayOrder: 7 },
  { consultantType: 8, typeLabel: '智云辅导', enabled: 1, displayOrder: 8 },
  { consultantType: 9, typeLabel: '留学堂辅导', enabled: 1, displayOrder: 9 },
  { consultantType: 12, typeLabel: '留学堂申诉', enabled: 1, displayOrder: 10 },
  { consultantType: 10, typeLabel: '集好家辅导', enabled: 1, displayOrder: 11 },
  { consultantType: 11, typeLabel: '集好家申诉', enabled: 1, displayOrder: 12 }
]

export const DEFAULT_TYPE_MODULES = {
  1: [
    { moduleKey: 'normal', moduleLabel: '官号(拉群)', enabled: 1, displayOrder: 1 },
    { moduleKey: 'single1', moduleLabel: '主动添加', enabled: 1, displayOrder: 2 },
    { moduleKey: 'sem', moduleLabel: 'SEM', enabled: 1, displayOrder: 3 }
  ],
  2: [
    { moduleKey: 'normal', moduleLabel: '官号', enabled: 1, displayOrder: 1 },
    { moduleKey: 'sem', moduleLabel: 'SEM', enabled: 1, displayOrder: 2 }
  ],
  3: [{ moduleKey: 'single1', moduleLabel: '推月申诉', enabled: 1, displayOrder: 1 }],
  4: [
    { moduleKey: 'normal', moduleLabel: '国际课程', enabled: 1, displayOrder: 1 },
    { moduleKey: 'single1', moduleLabel: '国际课程主动留资', enabled: 1, displayOrder: 2 },
    { moduleKey: 'sem', moduleLabel: '托福', enabled: 1, displayOrder: 3 }
  ],
  5: [{ moduleKey: 'single1', moduleLabel: '推月辅导', enabled: 1, displayOrder: 1 }],
  6: [{ moduleKey: 'single1', moduleLabel: '汇诺辅导', enabled: 1, displayOrder: 1 }],
  7: [{ moduleKey: 'single1', moduleLabel: '汇诺申诉', enabled: 1, displayOrder: 1 }],
  8: [{ moduleKey: 'single1', moduleLabel: '智云辅导', enabled: 1, displayOrder: 1 }],
  9: [{ moduleKey: 'single1', moduleLabel: '留学堂辅导', enabled: 1, displayOrder: 1 }],
  10: [{ moduleKey: 'single1', moduleLabel: '集好家辅导', enabled: 1, displayOrder: 1 }],
  11: [{ moduleKey: 'single1', moduleLabel: '集好家申诉', enabled: 1, displayOrder: 1 }],
  12: [{ moduleKey: 'single1', moduleLabel: '留学堂申诉', enabled: 1, displayOrder: 1 }]
}

export function normalizeTypeConfigs(configs = []) {
  const source = Array.isArray(configs) && configs.length > 0 ? configs : DEFAULT_TYPE_CONFIGS
  const typeSet = new Set()
  return source
    .map((item, index) => ({
      consultantType: Number(item.consultantType),
      typeLabel: item.typeLabel || `类型${item.consultantType}`,
      enabled: Number(item.enabled) === 1 ? 1 : 0,
      displayOrder: Number(item.displayOrder) > 0 ? Number(item.displayOrder) : index + 1
    }))
    .filter(item => item.consultantType > 0 && !typeSet.has(item.consultantType) && typeSet.add(item.consultantType))
    .sort((a, b) => a.displayOrder - b.displayOrder)
}

export function buildModuleConfig(typeValue, rawConfig) {
  if (!rawConfig || !rawConfig.moduleKey) {
    return null
  }
  const moduleMeta = MODULE_CATALOG.find(item => item.moduleKey === rawConfig.moduleKey)
  if (!moduleMeta) {
    return null
  }
  return {
    consultantType: Number(typeValue),
    moduleKey: rawConfig.moduleKey,
    moduleName: moduleMeta.moduleName,
    moduleLabel: rawConfig.moduleLabel || moduleMeta.moduleName,
    countFieldName: moduleMeta.countFieldName,
    orderStatusFieldName: moduleMeta.orderStatusFieldName,
    waitingFieldName: moduleMeta.waitingFieldName,
    enabled: Number(rawConfig.enabled) === 1 ? 1 : 0,
    displayOrder: Number(rawConfig.displayOrder) > 0 ? Number(rawConfig.displayOrder) : 1
  }
}

export function defaultModulesByType(typeValue) {
  const defaults = DEFAULT_TYPE_MODULES[typeValue] || []
  return defaults.map(item => buildModuleConfig(typeValue, item)).filter(Boolean)
}


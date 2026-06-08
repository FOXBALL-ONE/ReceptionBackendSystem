# 数字接待系统 API 文档

## 概述

本文档描述了为 `index-red.html` 前端页面提供的 RESTful API 接口。

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **所有接口均为 GET 请求**

## API 接口列表

### 1. 获取元数据配置

**接口**: `GET /api/meta`

**功能**: 获取页面标题、副标题、Logo、横幅图等元数据配置。

**响应示例**:
```json
{
  "title": "数字接待系统",
  "subtitle": "欢迎参加XX活动",
  "logo": null,
  "heroImage": "https://example.com/banner.jpg",
  "showLoading": true
}
```

---

### 2. 获取日程安排

**接口**: `GET /api/schedule`

**功能**: 获取活动日程，支持多日多组的日程安排。

**响应示例**:
```json
{
  "days": [
    {
      "date": "06月08日",
      "groups": [
        {
          "name": "第一考察组",
          "mapImage": "https://example.com/route.jpg",
          "route": ["地点A", "地点B", "地点C"],
          "meetings": [
            {
              "time": "09:00",
              "content": "开幕式",
              "location": "会议中心"
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 3. 获取人员信息

**接口**: `GET /api/people`

**功能**: 获取参会人员列表。

**响应示例**:
```json
[
  {
    "name": "张三",
    "unit": "XX单位",
    "type": "参会人员",
    "typeColor": "#2563eb",
    "group": "XX单位",
    "avatar": null
  }
]
```

---

### 4. 获取车辆安排

**接口**: `GET /api/vehicles`

**功能**: 获取车辆及乘客安排信息。

**响应示例**:
```json
[
  {
    "id": 1,
    "plate": "闽A12345",
    "driver": {
      "name": "李四",
      "phone": "13800138000"
    },
    "passengers": ["张三", "李四", "王五"],
    "leaders": [
      {
        "name": "赵六",
        "phone": "13900139000"
      }
    ]
  }
]
```

---

### 5. 获取用餐安排

**接口**: `GET /api/meals`

**功能**: 获取用餐时间和地点安排。

**响应示例**:
```json
[
  {
    "time": "2024-06-08T12:00:00",
    "label": "午餐",
    "remark": "地点：XX酒店一楼餐厅"
  }
]
```

---

### 6. 获取住宿信息

**接口**: `GET /api/hotel`

**功能**: 获取参会人员住宿房间安排。

**响应示例**:
```json
[
  {
    "name": "张三",
    "unit": "XX单位",
    "type": "参会人员",
    "typeColor": "#2563eb",
    "room": "2101"
  }
]
```

---

### 7. 获取考察点信息

**接口**: `GET /api/sites`

**功能**: 获取考察点介绍和图片。

**响应示例**:
```json
[
  {
    "name": "考察点A",
    "intro": "这是考察点的详细介绍...",
    "images": ["https://example.com/site1.jpg"]
  }
]
```

---

### 8. 获取服务信息

**接口**: `GET /api/service`

**功能**: 获取工作人员联系方式、天气信息、注意事项等。

**响应示例**:
```json
{
  "staff": [
    {
      "type": "联系人",
      "members": [
        {
          "name": "客服",
          "phone": "13800138000"
        }
      ]
    }
  ],
  "weather": [],
  "notices": []
}
```

---

### 9. 获取概况介绍

**接口**: `GET /api/overview`

**功能**: 获取市县概况或活动概况介绍。

**响应示例**:
```json
{
  "image": "https://example.com/overview.jpg",
  "sections": [
    {
      "title": "活动概况",
      "content": "本次活动旨在..."
    }
  ]
}
```

---

## 前端配置

在 `index-red.html` 中修改 API 基础地址：

```javascript
const API_BASE = 'http://localhost:8080/api';
```

生产环境请修改为实际的服务器地址。

---

## 数据来源

所有数据均从数据库中的 `Activities` 实体及其关联实体中获取：

- **日程**: `Activities.schedules` -> `Schedule` -> `InspectionTeamItem`
- **人员**: `Person` 表
- **车辆**: `Car` 表
- **用餐**: `Activities.mealList`
- **住宿**: `Activities.hostedList`
- **考察点**: `Activities.inspectionPoints`
- **概况**: `Activities.overviewOfTheCityAndCounty`

---

## 注意事项

1. 所有接口均已配置CORS，支持跨域访问
2. 目前实现为简化版本，获取第一个活动的数据
3. 生产环境建议通过URL参数或路由指定具体活动ID
4. 建议添加缓存机制以提高性能

---

## 开发者

Controller: `ReceptionViewController`  
Service: `ReceptionViewService`  
创建时间: 2026-06-08

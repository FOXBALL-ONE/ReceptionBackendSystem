export interface Event {
  id: number;
  url: string;
  masterTitle: string;
  subtitle: string;
  name: string;
  startTime: string;
  endTime: string;
  bannerTags: string;
  bannerUrl: string;
  isAnimation: boolean;
  isTopNavigationBar: boolean;
  icp: string;
  technicalSupport: string;
  isOpen: boolean;
  schedules?: Schedule[];
  mealList?: Meal[];
}

export interface EventBasicPayload {
  id?: number;
  url: string;
  masterTitle: string;
  subtitle: string;
  name: string;
  startTime: string;
  endTime: string;
  bannerTags: string;
  bannerUrl: string;
  isAnimation: boolean;
  isTopNavigationBar: boolean;
  icp: string;
  technicalSupport: string;
  isOpen: boolean;
}

export interface Meal {
  name: string;
  description: string;
  position: string;
  time: string;
}

export interface Schedule {
  id: number;
  scheduleTags: string;
}

export interface InspectionTeamItem {
  id: number;
  name: string;
  itineraries?: InspectionTeamItinerary[];
}

export interface InspectionTeamItinerary {
  id: number;
  scheduleId: number;
  routeUrl: string;
  scheduleUrl: string;
  routeNode?: string[];
  eventArrangements?: Array<{
    startTime?: string | null;
    endTime?: string | null;
    content?: string | null;
    location?: string | null;
  }>;
}

export interface EventListResponse {
  total: number;
  list: Event[];
}

export type EventStatus = 'open' | 'closed' | 'pending';

export const EventStatusMap: Record<EventStatus, { label: string; type: 'success' | 'warning' | 'error' }> = {
  open: { label: '已开放', type: 'success' },
  pending: { label: '未开放', type: 'warning' },
  closed: { label: '已关闭', type: 'error' },
};

export interface EventDisplayItem {
  id: number;
  path: string;
  name: string;
  status: EventStatus;
  createdAt: string;
}

export const convertEventToDisplayItem = (event: Event): EventDisplayItem => {
  return {
    id: event.id,
    path: event.url,
    name: event.name,
    status: event.isOpen ? 'open' : 'closed',
    createdAt: event.startTime || '',
  };
};

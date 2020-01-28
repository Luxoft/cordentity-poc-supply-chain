export const SIDEBAR_STATES = Object.freeze({
  open: 'open',
  closed: 'closed',
  opening: 'opening',
  closing: 'closing',
})

export const UI_WIDTH_TYPES = Object.freeze({
  roomy: 'roomy',
  compact: 'compact',
})

export const HEADER_HEIGHT = '48px'

export const AUTHZ_CLAIM_KEYS = Object.freeze({
  roles: `${process.env.AUTH0_NAMESPACE}/authorization/roles/`,
  groups: `${process.env.AUTH0_NAMESPACE}/authorization/groups/`,
  permissions: `${process.env.AUTH0_NAMESPACE}/authorization/permissions/`,
})

export const LEGACY_ROLES = Object.freeze({
  priorauth: 'priorauth',
  revup: 'revup',
  revupStandalone: 'revup-standalone',
  dailyCashSummary: 'dailycashsummary',
  dailySelfPayCash: 'dailyselfpaycash',
})

export const RENDERING_STATES = Object.freeze({
  initial: 'initial',
  loading: 'loading',
  success: 'success',
  failed: 'failed',
  cancelled: 'cancelled',
})

export const PRIMARY_NAVIGATION = Object.freeze({
  expandedWidth: '280px',
  collapsedWidth: '64px',
  transitionDuration: 0.5,
})

export const PRIOR_AUTH_SEARCH = Object.freeze({
  searchByReferralId: 'searchByReferralId',
  searchByMRN: 'searchByMRN',
  searchByPatient: 'searchByPatient',
})

export const PRIOR_AUTH_STATUS = Object.freeze({
  NoAuthRequired: 'NoAuthRequired',
  AuthRequired: 'AuthRequired',
  UnableToDetermine: 'UnableToDetermine',
  Error: 'Error',
})

export const CAN = Object.freeze({
  promote: {
    users: 'users:promote',
  },
  demote: {
    users: 'users:demote',
  },
  list: {
    users: 'users:list',
  },
  edit: {
    users: 'users:edit',
  },
  add: {
    users: 'users:add',
  },
  delete: {
    users: 'users:delete',
  },
  resetpassword: {
    users: 'users:resetpassword',
  },
  view: {
    portalMyprofilePage: 'portal.myprofile.page:view',
    portalCompanydetailsPage: 'portal.companydetails.page:view',
    portalUsermanagementPage: 'portal.usermanagement.page:view',
    portalSettingsPage: 'portal.settings.page:view',
    paAnalyticsPage: 'pa.analytics.page:view',
    paAuthlookupPage: 'pa.authlookup.page:view',
    aiRevupPage: 'ai.revup.page:view',
    aiDailycashPage: 'ai.dailycash.page:view',
    aiDailyselfpayPage: 'ai.dailyselfpay.page:view',
    isPayorplanmappingPage: 'is.payorplanmapping.page:view',
  },
  accessany: {
    aiModule: 'ai.module:accessany',
    paModule: 'pa.module:accessany',
    isModule: 'is.module:accessany',
  },
  access: {
    aiModule: 'ai.module:access',
    paModule: 'pa.module:access',
    isModule: 'is.module:access',
  },
  search: {
    paReports: 'pa.reports:search',
    paPatients: 'pa.patients:search',
    paMrn: 'pa.mrn:search',
  },
})

export const ERROR_TYPES = Object.freeze({
  PageNotFound: 404,
  Forbidden: 403,
  ServerError: 500,
})

export const BlankValue = '-'

export const GLOBAL_HEADER_TITLE = Object.freeze({
  Dashboard: 'Dashboard',
  MyProfile: 'My Profile',
  CompanyAccount: 'Company Account',
  PriorAuthorization: 'Prior Authorization',
  AnalyticsAndInsights: 'Analytics And Insights',
  IntelligentServices: 'Intelligent Services',
  NotFoundError: '404 Error',
  ForbiddenError: '403 Error',
})

export const APP_ROUTES = Object.freeze({
  Home: '/',
  NoMatch: '*',
  Login: '/login',
  Logout: '/logout',
  Callback: '/callback',
  Authorize: '/authorize',
  MyProfile: '/my_profile',
  PriorAuthorization: {
    Base: 'prior_authorization',
    Analytics: '/prior_authorization/analytics',
    AuthorizationLookup: '/prior_authorization/search',
  },
  AnalyticsAndInsights: {
    Base: 'analytics_insights',
    RevUp: '/analytics_insights/revup',
    DailyCashReporting: '/analytics_insights/daily_cash_reporting',
    DailySelfPayCashPOS: '/analytics_insights/daily_self-pay_cash_pos',
    DailySelfPayCashPostPOS: '/analytics_insights/daily_self-pay_cash_post_pos',
  },
  IntelligentServices: {
    Base: 'intelligent_services',
    PayorPlanMapping: '/intelligent_services/payor_plan_mapping',
  },
  CompanyAccount: {
    Base: 'company_account',
    CompanyDetails: '/company_account/company_details',
    UserManagement: '/company_account/user_management',
    Settings: '/company_account/settings',
  },
})

export const ORIENTS = Object.freeze({
  bottom: 'bottom',
  left: 'left',
  right: 'right',
  top: 'top',
})

export const DATE_FORMATS = Object.freeze({
  monthDate: 'MM/DD',
  monthDateYear: 'MM/DD/YYYY',
  monthDateYearHourMin: 'MM/DD/YYYY, HH:mm',
  yearMonthDate: 'YYYY-MM-DD',
})

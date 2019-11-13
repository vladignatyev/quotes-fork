from django.contrib import admin

from .models import *
from longjob.admin import LongJobAdmin


class DeviceSessionAdmin(admin.ModelAdmin):
    date_hierarchy = 'timestamp'


class PushSubscriptionAdmin(admin.ModelAdmin):
    pass


class GooglePlayIAPSubscriptionAdmin(admin.ModelAdmin):
    pass


class GooglePlayIAPPurchaseAdmin(admin.ModelAdmin):
    pass


# class AppStoreIAPSubscriptionAdmin(admin.ModelAdmin):
#     pass
#
#
# class AppStoreIAPPurchaseAdmin(admin.ModelAdmin):
#     pass
#

class CredentialsAdmin(admin.ModelAdmin):
    date_hierarchy = 'date_added'


class GooglePlayProductAdmin(admin.ModelAdmin):
    pass

# class AppStoreProductAdmin(admin.ModelAdmin):
#     pass


admin.site.register(DeviceSession, DeviceSessionAdmin)
# admin.site.register(PushSubscription, PushSubscriptionAdmin)

admin.site.register(GooglePlayProduct, GooglePlayProductAdmin)
# admin.site.register(GooglePlayIAPSubscription, GooglePlayIAPSubscriptionAdmin)
admin.site.register(GooglePlayIAPPurchase, GooglePlayIAPPurchaseAdmin)
#
# admin.site.register(AppStoreProduct, AppStoreProductAdmin)
# admin.site.register(AppStoreIAPSubscription, AppStoreIAPSubscriptionAdmin)
# admin.site.register(AppStoreIAPPurchase, AppStoreIAPPurchaseAdmin)

admin.site.register(Credentials, CredentialsAdmin)


@admin.register(PushNotificationQueueItem)
class PushNotificationQueueItemAdmin(LongJobAdmin):
    list_display = ['title'] + LongJobAdmin.list_display

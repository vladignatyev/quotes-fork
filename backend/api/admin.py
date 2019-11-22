from django.contrib import admin

from .models import *
from longjob.admin import LongJobAdmin


class DeviceSessionAdmin(admin.ModelAdmin):
    date_hierarchy = 'timestamp'
    list_display = ('timestamp', 'token', 'auth_token')
    search_fields = ['token', 'auth_token', 'timestamp']


class PushSubscriptionAdmin(admin.ModelAdmin):
    list_display = ('device_session', 'token')

    autocomplete_fields = ['device_session']
    search_fields = ['token']

    def get_queryset(self, request):
        qs = super(PushSubscriptionAdmin, self).get_queryset(request)
        return qs.select_related('device_session')



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
    list_display = ('google_play_bundle_id', 'date_added', 'shared_secret')


class GooglePlayProductAdmin(admin.ModelAdmin):
    list_display = ('id', 'sku',)
    pass

# class AppStoreProductAdmin(admin.ModelAdmin):
#     pass


admin.site.register(DeviceSession, DeviceSessionAdmin)
admin.site.register(PushSubscription, PushSubscriptionAdmin)

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
    list_display = ['title', 'is_broadcast'] + LongJobAdmin.list_display

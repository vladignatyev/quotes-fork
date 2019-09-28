from django.contrib import admin

from .models import *


class DeviceSessionAdmin(admin.ModelAdmin):
    pass


class PushSubscriptionAdmin(admin.ModelAdmin):
    pass


class GooglePlayIAPSubscriptionAdmin(admin.ModelAdmin):
    pass


class GooglePlayIAPPurchaseAdmin(admin.ModelAdmin):
    pass


class AppStoreIAPSubscriptionAdmin(admin.ModelAdmin):
    pass


class AppStoreIAPPurchaseAdmin(admin.ModelAdmin):
    pass


class CredentialsAdmin(admin.ModelAdmin):
    pass


class GooglePlayProductAdmin(admin.ModelAdmin):
    pass

class AppStoreProductAdmin(admin.ModelAdmin):
    pass


admin.site.register(DeviceSession, DeviceSessionAdmin)
admin.site.register(PushSubscription, PushSubscriptionAdmin)

admin.site.register(GooglePlayProduct, GooglePlayProductAdmin)
admin.site.register(GooglePlayIAPSubscription, GooglePlayIAPSubscriptionAdmin)
admin.site.register(GooglePlayIAPPurchase, GooglePlayIAPPurchaseAdmin)

admin.site.register(AppStoreProduct, AppStoreProductAdmin)
admin.site.register(AppStoreIAPSubscription, AppStoreIAPSubscriptionAdmin)
admin.site.register(AppStoreIAPPurchase, AppStoreIAPPurchaseAdmin)
admin.site.register(Credentials, CredentialsAdmin)

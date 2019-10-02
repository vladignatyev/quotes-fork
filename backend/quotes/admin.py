from django.contrib import admin

from .models import *


class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category')


class QuoteCategoryAdmin(admin.ModelAdmin):
    list_display = ('title',)


class TopicAdmin(admin.ModelAdmin):
    list_display = ('title',)

class ProfileAdmin(admin.ModelAdmin):
    list_display = ('nickname', 'balance', )

admin.site.register(Profile, ProfileAdmin)

admin.site.register(Quote, QuoteAdmin)
admin.site.register(QuoteCategory, QuoteCategoryAdmin)
admin.site.register(Topic, TopicAdmin)
admin.site.register(QuoteAuthor)


class BalanceRechargeProductAdmin(admin.ModelAdmin):
    list_display = ('admin_title', 'google_play_product',) # 'app_store_product')

admin.site.register(BalanceRechargeProduct, BalanceRechargeProductAdmin)

from django.contrib import admin

from .models import *


class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category')

#
# class SectionInline(admin.TabularInline):
#     model = Section
#     fields = ('title',)
#

class QuoteCategoryAdmin(admin.ModelAdmin):
    list_display = ('title', 'section')
    # inlines = (SectionInline,)

class TopicAdmin(admin.ModelAdmin):
    list_display = ('title',)

class SectionAdmin(admin.ModelAdmin):
    list_display = ('title', 'topic')

class ProfileAdmin(admin.ModelAdmin):
    list_display = ('nickname', 'balance', )

admin.site.register(Profile, ProfileAdmin)

admin.site.register(Quote, QuoteAdmin)
admin.site.register(QuoteCategory, QuoteCategoryAdmin)

admin.site.register(Section, SectionAdmin)
admin.site.register(Topic, TopicAdmin)
admin.site.register(QuoteAuthor)
admin.site.register(Achievement)
admin.site.register(GameBalance)

class BalanceRechargeProductAdmin(admin.ModelAdmin):
    list_display = ('admin_title', 'google_play_product',) # 'app_store_product')

admin.site.register(BalanceRechargeProduct, BalanceRechargeProductAdmin)

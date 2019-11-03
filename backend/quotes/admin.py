from django.contrib import admin

from .models import *


from django import forms



class QuoteCategoryAdmin(admin.ModelAdmin):
    list_display = ('title', 'section')
    # inlines = (SectionInline,)
    exclude = ('is_event', 'event_due_date', 'event_title', 'event_icon', 'event_description','event_win_achievement', 'available_to_users', 'complete_by_users')
    fields = ( 'title', 'section', 'bonus_reward', 'is_payable', 'price_to_unlock', 'on_complete_achievement', 'icon',)
    search_fields = ['title']


class QuoteForm(forms.ModelForm):
    text = forms.CharField(widget=forms.Textarea)

    class Meta:
        model = Quote
        fields = '__all__'

class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category')
    exclude = ('available_to_users',)
    fields = ('text', 'author', 'order_in_category', 'on_complete_achievement', 'category')
    search_fields = ['text', 'author']
    view_on_site = True

    form = QuoteForm


class TopicAdmin(admin.ModelAdmin):
    list_display = ('title',)
    fields = ( 'title', 'bonus_reward', 'on_complete_achievement', 'hidden',)
    search_fields = ['title']


class SectionAdmin(admin.ModelAdmin):
    list_display = ('title', 'topic')
    fields = ( 'title', 'topic', 'bonus_reward', 'on_complete_achievement',)
    search_fields = ['title']


class ProfileAdmin(admin.ModelAdmin):
    list_display = ('nickname', 'balance', )
    date_hierarchy = 'last_active'
    exclude = ('device_sessions',)


class AchievementAdmin(admin.ModelAdmin):
    list_display = ('title', 'icon',)
    exclude = ('opened_by_users',)
    search_fields = ['title']


class AchievementReceivingAdmin(admin.ModelAdmin):
    list_display = ('profile', 'achievement',)
    list_select_related = (
        'achievement',
        'profile'
    )


class CategoryUnlockPurchaseAdmin(admin.ModelAdmin):
    date_hierarchy = 'date_created'


class BalanceRechargeProductAdmin(admin.ModelAdmin):
    list_display = ('admin_title', 'google_play_product',) # 'app_store_product')


admin.site.register(Topic, TopicAdmin)
admin.site.register(Section, SectionAdmin)
admin.site.register(Quote, QuoteAdmin)
admin.site.register(QuoteCategory, QuoteCategoryAdmin)
admin.site.register(QuoteAuthor)

admin.site.register(GameBalance)
admin.site.register(Profile, ProfileAdmin)

admin.site.register(Achievement, AchievementAdmin)
admin.site.register(BalanceRechargeProduct, BalanceRechargeProductAdmin)

admin.site.register(AchievementReceiving, AchievementReceivingAdmin)

admin.site.register(CategoryUnlockPurchase, CategoryUnlockPurchaseAdmin)

from django.contrib import admin

from .models import *
from api.models import PushSubscription

from django import forms


@admin.register(PushSubscription)
class PushSubscriptionAdmin(admin.ModelAdmin):
    pass


@admin.register(QuoteCategory)
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


@admin.register(Quote)
class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category')
    exclude = ('available_to_users',)
    fields = ('text', 'author', 'order_in_category', 'on_complete_achievement', 'category')
    search_fields = ['text', 'author']
    view_on_site = True

    form = QuoteForm


@admin.register(Topic)
class TopicAdmin(admin.ModelAdmin):
    list_display = ('title',)
    fields = ( 'title', 'bonus_reward', 'on_complete_achievement', 'hidden',)
    search_fields = ['title']


@admin.register(Section)
class SectionAdmin(admin.ModelAdmin):
    list_display = ('title', 'topic')
    fields = ( 'title', 'topic', 'bonus_reward', 'on_complete_achievement',)
    search_fields = ['title']


@admin.register(Profile)
class ProfileAdmin(admin.ModelAdmin):
    list_display = ('nickname', 'balance', )
    date_hierarchy = 'last_active'
    exclude = ('device_sessions',)


@admin.register(Achievement)
class AchievementAdmin(admin.ModelAdmin):
    list_display = ('title', 'icon',)
    exclude = ('opened_by_users',)
    search_fields = ['title']

@admin.register(AchievementReceiving)
class AchievementReceivingAdmin(admin.ModelAdmin):
    list_display = ('profile', 'achievement',)
    list_select_related = (
        'achievement',
        'profile'
    )

@admin.register(CategoryUnlockPurchase)
class CategoryUnlockPurchaseAdmin(admin.ModelAdmin):
    date_hierarchy = 'date_created'


@admin.register(BalanceRechargeProduct)
class BalanceRechargeProductAdmin(admin.ModelAdmin):
    list_display = ('admin_title', 'google_play_product',) # 'app_store_product')


@admin.register(GameBalance)
class GameBalanceAdmin(admin.ModelAdmin):
    pass


@admin.register(QuoteAuthor)
class QuoteAuthorAdmin(admin.ModelAdmin):
    pass

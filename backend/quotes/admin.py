from django.contrib import admin

from .models import *
from api.models import PushSubscription

from django import forms
from django.db.models import Count


from django.utils.safestring import mark_safe


def unpublish(modeladmin, request, queryset):
    for item in queryset:
        item.is_published = False
        item.save()
unpublish.short_description = 'Unpublish selected items'

def publish(modeladmin, request, queryset):
    for item in queryset:
        item.is_published = True
        item.save()
publish.short_description = 'Publish selected items'


def hide_from_rank(modeladmin, request, queryset):
    for item in queryset:
        item.hide_from_rank = True
        item.save()
hide_from_rank.short_description = 'Скрыть пользователей из топа'

def show_in_rank(modeladmin, request, queryset):
    for item in queryset:
        item.hide_from_rank = False
        item.save()
show_in_rank.short_description = 'Показывать пользователя в топе'



#
# @admin.register(PushSubscription)
# class PushSubscriptionAdmin(admin.ModelAdmin):
#     pass


class QuotesInline(admin.TabularInline):
    model = Quote
    exclude = [ 'on_complete_achievement', 'bonus_reward', 'complete_by_users']
    autocomplete_fields = ['author']


@admin.register(QuoteCategory)
class QuoteCategoryAdmin(admin.ModelAdmin):
    list_display = ('item_preview_image_view', 'title', 'section', 'num_subitems', 'order', )

    inlines = [QuotesInline,]

    exclude = ('is_event', 'event_due_date', 'event_title', 'event_icon',
               'event_description','event_win_achievement',
               'available_to_users', 'complete_by_users')
    fields = ( 'title', 'section', 'bonus_reward', 'is_payable',
               'price_to_unlock', 'on_complete_achievement', 'icon',
               'item_image', 'item_image_view', 'is_published', 'tags',
               'order')
    search_fields = ['title']

    autocomplete_fields = ['tags', 'on_complete_achievement', 'section']

    readonly_fields = ('item_image_view', 'item_preview_image_view')
    actions = [unpublish, publish]
    ordering = ('order',)

    def num_subitems(self, obj):
        return obj.num_subitems
    num_subitems.short_description = "# quotes"

    def get_queryset(self, request):
        """Use this so we can annotate with additional info."""

        qs = super(QuoteCategoryAdmin, self).get_queryset(request).select_related('section')
        return qs.annotate(num_subitems=Count('quote', distinct=True))



class QuoteForm(forms.ModelForm):
    text = forms.CharField(widget=forms.Textarea)

    class Meta:
        model = Quote
        fields = '__all__'



@admin.register(Tag)
class TagAdmin(admin.ModelAdmin):
    search_fields = ['tag_value']

@admin.register(Quote)
class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category', 'get_beautiful', 'bubbles')
    exclude = ('available_to_users',)
    ordering = ('order_in_category',)
    fields = ('text', 'author', 'order_in_category', 'on_complete_achievement', 'category', 'bubbles', 'get_beautiful')
    search_fields = ['text', 'author']
    view_on_site = True
    readonly_fields = ['bubbles', 'get_beautiful']

    autocomplete_fields = ['author', 'on_complete_achievement', 'category']

    form = QuoteForm






@admin.register(Topic)
class TopicAdmin(admin.ModelAdmin):
    list_display = ('title', 'num_subitems', 'order', )
    ordering = ('order',)
    fields = ( 'title', 'bonus_reward', 'on_complete_achievement', 'is_published', 'tags', 'order')
    search_fields = ['title']
    autocomplete_fields = ['tags', 'on_complete_achievement']
    actions = [unpublish, publish]


    def num_subitems(self, obj):
        return obj.num_subitems
    num_subitems.short_description = "# sections"

    def get_queryset(self, request):
        """Use this so we can annotate with additional info."""

        qs = super(TopicAdmin, self).get_queryset(request)
        return qs.annotate(num_subitems=Count('section', distinct=True))


@admin.register(Section)
class SectionAdmin(admin.ModelAdmin):
    list_display = ('title', 'topic', 'num_subitems', 'order')
    fields = ( 'title', 'topic', 'bonus_reward', 'on_complete_achievement', 'is_published', 'tags', 'order')
    search_fields = ['title']
    autocomplete_fields = ['tags', 'on_complete_achievement', 'topic']
    actions = [unpublish, publish]
    ordering = ('order',)


    def num_subitems(self, obj):
        return obj.num_subitems
    num_subitems.short_description = "# categories"

    def get_queryset(self, request):
        qs = super(SectionAdmin, self).get_queryset(request).select_related('topic')
        return qs.annotate(num_subitems=Count('quotecategory', distinct=True))



@admin.register(Profile)
class ProfileAdmin(admin.ModelAdmin):
    list_display = ('nickname', 'balance', 'is_banned')
    date_hierarchy = 'last_active'
    search_fields = ['device_sessions__token']
    # exclude = ('device_sessions',)

    actions = [hide_from_rank, show_in_rank]


@admin.register(Achievement)
class AchievementAdmin(admin.ModelAdmin):
    list_display = ('item_preview_image_view', 'title', 'icon')
    exclude = ('opened_by_users',)
    search_fields = ['title']

    readonly_fields = ('item_image_view', 'item_preview_image_view')

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
@admin.register(DoubleUpProduct)
class BalanceRechargeProductAdmin(admin.ModelAdmin):
    list_display = ('item_preview_image_view', 'admin_title', 'google_play_product', 'is_featured') # 'app_store_product')
    readonly_fields = ('item_image_view',)
    exclude = ('item_preview_image_view',)
    autocomplete_fields = ['scope_tags']


@admin.register(GameBalance)
class GameBalanceAdmin(admin.ModelAdmin):
    pass


@admin.register(QuoteAuthor)
class QuoteAuthorAdmin(admin.ModelAdmin):
    search_fields = ['name']

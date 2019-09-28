from django.contrib import admin

from .models import *


class QuoteAdmin(admin.ModelAdmin):
    list_display = ('text', 'author', 'category')


class QuoteCategoryAdmin(admin.ModelAdmin):
    list_display = ('title', 'language')


admin.site.register(Profile)

admin.site.register(Quote, QuoteAdmin)
admin.site.register(QuoteCategory, QuoteCategoryAdmin)
admin.site.register(QuoteAuthor)
admin.site.register(QuoteLanguage)


class ProductAdmin(admin.ModelAdmin):
    list_display = ('admin_title', 'google_play_product', 'app_store_product')

admin.site.register(Product, ProductAdmin)

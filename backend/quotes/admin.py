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

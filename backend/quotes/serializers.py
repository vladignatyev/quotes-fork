from rest_framework import serializers
from .models import Quote, QuoteCategory, QuoteAuthor


class QuoteAuthorSerializer(serializers.ModelSerializer):
    class Meta:
        model = QuoteAuthor
        fields = ['name']
#
class QuoteCategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = QuoteCategory
        fields = ['title', 'language']
#
class QuoteSerializer(serializers.ModelSerializer):
    author = QuoteAuthorSerializer()
    category = QuoteCategorySerializer()

    class Meta:
        model = Quote
        fields = ['text', 'author', 'category']
#

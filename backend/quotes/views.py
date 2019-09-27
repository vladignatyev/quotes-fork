from django.shortcuts import render
from rest_framework import viewsets

from .models import Quote, QuoteCategory, QuoteAuthor


from rest_framework import serializers


class QuoteAuthorSerializer(serializers.ModelSerializer):
    class Meta:
        model = QuoteAuthor
        fields = ['name']

class QuoteCategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = QuoteCategory
        fields = ['title', 'language']

class QuoteSerializer(serializers.ModelSerializer):
    author = QuoteAuthorSerializer()
    category = QuoteCategorySerializer()

    class Meta:
        model = Quote
        fields = ['text', 'author', 'category']

class QuoteViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Quote.objects.all()

    serializer_class = QuoteSerializer

class QuoteCategoryViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = QuoteCategory.objects.all()

    serializer_class = QuoteCategorySerializer

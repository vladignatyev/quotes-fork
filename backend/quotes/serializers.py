from rest_framework import serializers
from .models import *


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

class TopicSerializer(serializers.ModelSerializer):
    class Meta:
        model = Topic
        fields = ['title',]
#


class SectionSerializer(serializers.ModelSerializer):

    class Meta:
        model = Section
        fields = ['title', ]

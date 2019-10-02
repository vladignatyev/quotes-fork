from django.shortcuts import render
from rest_framework import viewsets

from django.http import Http404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status


from rest_framework.decorators import action

from .models import Quote, QuoteCategory, QuoteAuthor

from .serializers import *

class Quotes(viewsets.ViewSet):
    basename = 'quotes'

    def list(self, request, topic, category, format=None):
        quotes = Quote.objects.all()
        serializer = QuoteSerializer(quotes, many=True)
        return Response(serializer.data)


class Topics(viewsets.ViewSet):
    basename = 'topics'

    def list(self, request, format=None):
        topics = Topic.objects.all()
        serializer = TopicSerializer(topics, many=True)
        return Response(serializer.data)

class Sections(viewsets.ViewSet):
    basename = 'sections'

    def list(self, topic_pk, profile_pk, format=None):
        profile = Profile.objects.get(pk=profile_pk)
        # # topic = Topic.objects.get(pk=topic_pk)
        # sections = Section.objects.filter(topic_pk=topic_pk)
        #
        # QuoteCategory.objects.filter(section__in=sections, available_to_users__contains=profile)

        serializer = SectionSerializer(sections, many=True)
        return Response(serializer.data)

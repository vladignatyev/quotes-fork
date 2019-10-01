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
    # @action(detail=False)
    def list(self, request, topic, category, format=None):
        quotes = Quote.objects.all()
        serializer = QuoteSerializer(quotes, many=True)
        return Response(serializer.data)

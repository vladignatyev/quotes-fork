from django.shortcuts import render

from django.http import Http404
from django.db import models


from .models import *

# from tastypie import fields, utils
# from tastypie.resources import ModelResource, ALL, ALL_WITH_RELATIONS, Resource, Bundle


# class TopicResource(ModelResource):
#     sections = fields.ToManyField('quotes.views.SectionResource', 'section_set', full=True, full_detail=True, full_list=False)
#
#     class Meta:
#         queryset = Topic.objects.filter(hidden=False)
#         excludes = ['hidden']

#
# class SectionResource(ModelResource):
#     topic = fields.ForeignKey('quotes.views.TopicResource', 'topic')
#     categories = fields.ToManyField('quotes.views.QuoteCategoryResource', 'categories_set')
#
#
#     class Meta:
#         queryset = Section.objects.all()
#         resource_name = 'section'
#
#
# class QuoteCategoryResource(ModelResource):
#     # section = fields.ToOneField('quotes.views.SectionResource', 'section', related_name='categories')
#     section = fields.ForeignKey(SectionResource, 'quote_category_section')
#
#
#     class Meta:
#         queryset = QuoteCategory.objects.all()
#         resource_name = 'category'
#
#
# class QuoteResource(ModelResource):
#     category = fields.ToOneField('quotes.views.QuoteCategoryResource', 'category')
#
#     class Meta:
#         queryset = Quote.objects.all()
#         resource_name = 'level'
#
#
# class ProfileResource(ModelResource):
#     class Meta:
#         queryset = Profile.objects.all()
#         resource_name = 'profiles'
#
#
# class RiakObject(object):
#     def __init__(self, initial=None):
#         self.__dict__['_data'] = {}
#
#         if hasattr(initial, 'items'):
#             self.__dict__['_data'] = initial
#
#     def __getattr__(self, name):
#         return self._data.get(name, None)
#
#     def __setattr__(self, name, value):
#         self.__dict__['_data'][name] = value
#
#     def to_dict(self):
#         return self._data
#
# import logging
#
# # Get an instance of a logger
# logger = logging.getLogger(__name__)
#
#
#
# class TopicResource2(Resource):
#     title = fields.CharField(attribute='title')
#     sections = fields.ListField(attribute='sections', null=True)
#
#
#     class Meta:
#         object_class = RiakObject
#
#         resource_name = 'topic2'
#         detail_allowed_methods = ['get']
#         list_allowed_methods = ['get']
#
#     # The following methods will need overriding regardless of your
#     # data source.
#     def detail_uri_kwargs(self, bundle_or_obj):
#         kwargs = {}
#
#         if isinstance(bundle_or_obj, Bundle):
#             logger.error(bundle_or_obj.obj)
#             kwargs['pk'] = bundle_or_obj.obj['id']
#         else:
#             kwargs['pk'] = bundle_or_obj.id
#
#         return kwargs
#
#     def get_object_list(self, request):
#         results = Topic.objects.filter(hidden=False)
#         return results
#
#     def obj_get_list(self, bundle, **kwargs):
#         return self.get_object_list(bundle.request)
#
#     def obj_get(self, bundle, **kwargs):
#         return Topic.objects.get_flattened(pk=kwargs['pk'])
#         # return Topic.objects.get(pk=kwargs['pk'])
#
#     def obj_create(self, bundle, **kwargs):
#         return bundle
#
#     def obj_update(self, bundle, **kwargs):
#         return self.obj_create(bundle, **kwargs)
#
#     def obj_delete_list(self, bundle, **kwargs):
#         pass
#
#     def obj_delete(self, bundle, **kwargs):
#         pass
#
#     def rollback(self, bundles):
#         pass

from django.http import HttpResponse
from django.views.generic import ListView, View, DetailView

from django.http import JsonResponse
from django.forms.models import model_to_dict
from django.views.decorators.http import require_http_methods

from django.core.paginator import Paginator

from django.urls import reverse



def json_response(res_dict):
    return JsonResponse(res_dict, json_dumps_params={'indent': 4, 'ensure_ascii': False})



class RestDetailView(View):
    pass # todo: extract base class for all serializable detail views

class RestListView(View):
    pass # todo: extract base class for all serializable list views



class SectionDetail(View):
    def get(self, request, *args, **kwargs):
        section = Section.objects.get(pk=kwargs['pk'])
        return json_response(topic)


class TopicDetail(View):
    def get(self, request, *args, **kwargs):
        topic = Topic.objects.get_flattened(pk=kwargs['pk'])
        return json_response(topic)


class TopicList(View):
    queryset = Topic.objects.filter(hidden=False)
    ordering = '-pk'

    def get(self, request, *args, **kwargs):
        objects = [ model_to_dict(o) for o in self.queryset.order_by(self.ordering) ]
        for o in objects:
            o['uri'] = reverse('topic-detail', kwargs={'pk': o['id']})


        res_dict = {
            "objects": objects,
            "meta": {
                # "limit": self.paginate_by,
                # # "next": null,
                # "offset": 0,
                # # "previous": null,
                # "total_count": p.count,
                # "num_pages": p.num_pages
            }
        }

        return json_response(res_dict)


class AchievementList(View):
    def get(self, request, *args, **kwargs):
        
        res_dict = {
            "objects": achievements,
            "meta": {}
        }

        return json_response(res_dict)

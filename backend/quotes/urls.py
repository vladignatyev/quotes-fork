"""backend URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""

from django.urls import include, path
from django.conf import settings

from tastypie.api import Api

#
# from rest_framework import routers, serializers, viewsets
# from .views import * #, QuoteViewSet #, QuoteCategoryViewSet
#
# router = routers.DefaultRouter()
#
# # router.register(r'quotes/(?P<topic>.+)/(?P<category>.+)', Quotes, Quotes.basename)
# router.register(r'topics', Topics, Topics.basename)
#
# router.register(r'topic/(?P<topic_pk>\d+)/sections/(?P<profile_pk>\d+)', Sections, Sections.basename)
#
#
# # router.register(r'quotes', QuotesList, basename=QuotesList.basename)
#
# # router.register(r'categories', QuoteCategoryViewSet)

# urlpatterns = [
#     path('', include(router.urls), name='quotes-api'),
# ]

from .views import *


#
# v1_api = Api(api_name=f'v{settings.API_VERSION}')
# # v1_api.register(QuoteResource())
# # v1_api.register(SectionResource())
# # v1_api.register(TopicResource())
# v1_api.register(TopicResource2())
# v1_api.register(ProfileResource())

# v1_api.register(QuoteCategoryResource())
#
urlpatterns = [
    path('topic/<int:pk>/', TopicDetail.as_view(), name='topic-detail'),
    path('topic/list/', TopicList.as_view(), name='topic-list'),

    path('section/<int:pk>/', SectionDetail.as_view(), name='section-detail'),

    # path('topic/list/<int:page>', TopicList.as_view(), name='topic-list')
]

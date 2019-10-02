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

from rest_framework import routers, serializers, viewsets
from .views import * #, QuoteViewSet #, QuoteCategoryViewSet

router = routers.DefaultRouter()

# router.register(r'quotes/(?P<topic>.+)/(?P<category>.+)', Quotes, Quotes.basename)
router.register(r'topics', Topics, Topics.basename)

router.register(r'topic/(?P<topic_pk>\d+)/sections/(?P<profile_pk>\d+)', Sections, Sections.basename)


# router.register(r'quotes', QuotesList, basename=QuotesList.basename)

# router.register(r'categories', QuoteCategoryViewSet)
# urlpatterns = [router.urls,]
urlpatterns = [
    path('', include(router.urls)),
]

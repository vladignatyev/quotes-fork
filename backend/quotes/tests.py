from django.test import TestCase

from .models import Profile
from api.models import DeviceSession


class UtilsTest(TestCase):
    def test_truncate(self):
        from .models import _truncate

        self.assertEqual('Карл у Кла˚˚˚', _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚'))
        self.assertEqual('Карл у Клары украл коралы', _truncate('Карл у Клары украл коралы', length=100, suffix='˚˚˚'))


class ProfileTest(TestCase):
    def test_autocreate_profile_when_new_device_session_created(self):
        # Given
        self.assertEqual(0, len(Profile.objects.all()))

        # When
        session = DeviceSession.objects.create()

        # Then
        self.assertEqual(1, len(Profile.objects.filter(device_sessions__pk__contains=session).all()))

    def test_shouldnt_create_dupes(self):
        # Given
        self.assertEqual(0, len(Profile.objects.all()))
        session = DeviceSession.objects.create()

        # When
        session.save()

        # Then
        self.assertEqual(1, len(Profile.objects.filter(device_sessions__pk__contains=session).all()))

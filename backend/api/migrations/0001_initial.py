# Generated by Django 2.2.5 on 2019-10-12 16:48

import api.crypto
from django.db import migrations, models
import django.db.models.deletion
import uuid


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='AppStoreIAPSubscription',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
            ],
        ),
        migrations.CreateModel(
            name='AppStoreProduct',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
            ],
        ),
        migrations.CreateModel(
            name='Credentials',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('google_play_bundle_id', models.CharField(default='', max_length=256, verbose_name='Play Market Bundle ID')),
                ('google_play_api_key', models.CharField(default='', max_length=256, verbose_name='Google Play API Key')),
                ('date_added', models.DateTimeField(auto_now_add=True)),
                ('server_secret', models.CharField(default=api.crypto.generate_secret, max_length=256, verbose_name='Server-only secret')),
                ('shared_secret', models.CharField(default=api.crypto.generate_secret, max_length=256, verbose_name='Shared secret')),
            ],
            options={
                'verbose_name': 'Credentials',
                'verbose_name_plural': 'Credentials',
            },
        ),
        migrations.CreateModel(
            name='DeviceSession',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('token', models.CharField(max_length=256, unique=True, verbose_name='Токен устройства')),
                ('auth_token', models.CharField(default='', max_length=256, unique=True, verbose_name='Токен идентификатор сессии')),
                ('timestamp', models.DateTimeField(auto_now_add=True)),
            ],
        ),
        migrations.CreateModel(
            name='GooglePlayIAPSubscription',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
            ],
        ),
        migrations.CreateModel(
            name='GooglePlayProduct',
            fields=[
                ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('sku', models.CharField(blank=True, max_length=256, verbose_name='IAP SKU (Product ID)')),
            ],
        ),
        migrations.CreateModel(
            name='PushSubscription',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('token', models.CharField(max_length=256)),
                ('device_session', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.DeviceSession')),
            ],
        ),
        migrations.CreateModel(
            name='GooglePlayIAPPurchase',
            fields=[
                ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('type', models.CharField(choices=[('video', 'Reward Video'), ('purchase', 'Purchase')], default='purchase', max_length=16, verbose_name='Type: reward video or purchase')),
                ('status', models.CharField(choices=[('unknown', 'Unknown'), ('valid', 'Valid'), ('inprogress', 'In progress'), ('invalid', 'Invalid')], default='unknown', max_length=16, verbose_name='Validation status')),
                ('purchase_token', models.CharField(blank=True, max_length=256)),
                ('order_id', models.CharField(blank=True, max_length=256)),
                ('date_created', models.DateTimeField(auto_now_add=True)),
                ('date_updated', models.DateTimeField(auto_now=True)),
                ('device_session', models.ForeignKey(null=True, on_delete=django.db.models.deletion.CASCADE, to='api.DeviceSession')),
                ('product', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='api.GooglePlayProduct')),
            ],
            options={
                'abstract': False,
            },
        ),
        migrations.CreateModel(
            name='AppStoreIAPPurchase',
            fields=[
                ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('type', models.CharField(choices=[('video', 'Reward Video'), ('purchase', 'Purchase')], default='purchase', max_length=16, verbose_name='Type: reward video or purchase')),
                ('status', models.CharField(choices=[('unknown', 'Unknown'), ('valid', 'Valid'), ('inprogress', 'In progress'), ('invalid', 'Invalid')], default='unknown', max_length=16, verbose_name='Validation status')),
                ('device_session', models.ForeignKey(null=True, on_delete=django.db.models.deletion.CASCADE, to='api.DeviceSession')),
                ('product', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='api.AppStoreProduct')),
            ],
            options={
                'abstract': False,
            },
        ),
    ]

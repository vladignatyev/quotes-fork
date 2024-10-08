# Generated by Django 2.2.5 on 2019-11-26 18:27

from django.db import migrations, models
import django.db.models.deletion
import quotes.models
import uuid


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0010_auto_20191126_1827'),
        ('quotes', '0018_auto_20191122_1216'),
    ]

    operations = [
        migrations.CreateModel(
            name='DoubleUpProduct',
            fields=[
                ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('google_play_product', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='api.GooglePlayProduct', verbose_name='Соответствующий продукт в Google Play')),
            ],
            options={
                'verbose_name': 'Продукт\xa0«дабл-ап»',
                'verbose_name_plural': 'Продукты типа\xa0«дабл-ап»',
            },
            bases=(models.Model, quotes.models.ProductFlow),
        ),
    ]

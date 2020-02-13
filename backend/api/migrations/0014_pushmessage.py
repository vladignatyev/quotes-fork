# Generated by Django 2.2.5 on 2020-02-11 23:22

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0013_googleplayproduct_is_admob_rewarded_ssv'),
    ]

    operations = [
        migrations.CreateModel(
            name='PushMessage',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('title', models.CharField(blank=True, default='', max_length=256)),
                ('body', models.TextField(blank=True, default='')),
                ('image_url', models.CharField(blank=True, default='', max_length=256)),
                ('data', models.TextField(blank=True, default='')),
                ('topic', models.CharField(blank=True, default='', max_length=256, null=True)),
                ('condition', models.CharField(blank=True, default='', max_length=256, null=True)),
                ('schedule', models.CharField(blank=True, default='', max_length=256)),
            ],
        ),
    ]

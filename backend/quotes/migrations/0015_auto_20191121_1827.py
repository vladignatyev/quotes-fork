# Generated by Django 2.2.5 on 2019-11-21 18:27

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('quotes', '0014_auto_20191121_1028'),
    ]

    operations = [
        migrations.CreateModel(
            name='Tag',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('tag_value', models.CharField(default='', max_length=256, verbose_name='Значение tag для клиента')),
            ],
        ),
        migrations.AddField(
            model_name='topic',
            name='order',
            field=models.PositiveIntegerField(blank=True, default=1000, verbose_name='Порядок сортировки: чем ниже значение, тем выше Тема в списке'),
        ),
        migrations.AddField(
            model_name='quotecategory',
            name='tags',
            field=models.ManyToManyField(blank=True, to='quotes.Tag'),
        ),
        migrations.AddField(
            model_name='section',
            name='tags',
            field=models.ManyToManyField(blank=True, to='quotes.Tag'),
        ),
        migrations.AddField(
            model_name='topic',
            name='tags',
            field=models.ManyToManyField(blank=True, to='quotes.Tag'),
        ),
    ]

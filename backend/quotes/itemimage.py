import uuid

from django.core.files.base import ContentFile
from PIL import Image
from io import BytesIO




class ItemWithImageMixin:
    BLANK_IMAGE_PATH = 'https://via.placeholder.com/512'

    IMAGE_PREVIEW = (256, 256)

    def get_image_url(self):
        if self.item_image:
            return self.item_image.url
        else:
            return self.BLANK_IMAGE_PATH

    def get_image_preview_url(self):
        if self.item_image_preview:
            return self.item_image_preview.url
        else:
            return self.BLANK_IMAGE_PATH


    def save_images(self, *args, **kwargs):
        if not self.item_image_preview and self.item_image:
            # generate and save preview
            image = Image.open(self.item_image.file)
            w = image.width
            h = image.height
            image.thumbnail(self.IMAGE_PREVIEW, Image.ANTIALIAS)

            thumb_name, thumb_extension = os.path.splitext(self.item_image.file.name)
            thumb_extension = thumb_extension.lower()

            thumb_filename = thumb_name + '_thumb' + str(uuid.uuid4()) + thumb_extension

            if thumb_extension == '.png':
                FTYPE = 'PNG'
            elif thumb_extension in ('.jpg', '.jpeg'):
                FTYPE = 'JPEG'
            else:
                raise ValueError('Unsupported image.')

            # Save thumbnail to in-memory file as StringIO
            temp_thumb = BytesIO()
            image.save(temp_thumb, FTYPE)
            temp_thumb.seek(0)

            # set save=False, otherwise it will run in an infinite loop
            self.item_image_preview.save(thumb_filename, ContentFile(temp_thumb.read()), save=False)
            temp_thumb.close()

-- -----------------------------------------------------------------------------
-- Migration V8: Salon Gallery Images
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_images (
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_salon_images_salon ON salon_images(salon_id);

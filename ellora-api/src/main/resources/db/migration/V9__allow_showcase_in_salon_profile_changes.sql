ALTER TABLE salon_profile_changes
    DROP CONSTRAINT IF EXISTS salon_profile_changes_kind_check,
    ADD CONSTRAINT salon_profile_changes_kind_check
        CHECK (kind::text = ANY (ARRAY['PROFILE'::character varying, 'COVER'::character varying, 'GALLERY'::character varying, 'SHOWCASE'::character varying]::text[]));

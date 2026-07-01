ALTER TABLE public.assets
ALTER COLUMN dosage TYPE text;

ALTER TABLE public.asset_indications
ALTER COLUMN indication TYPE text;

ALTER TABLE public.asset_associations
ALTER COLUMN association TYPE text;
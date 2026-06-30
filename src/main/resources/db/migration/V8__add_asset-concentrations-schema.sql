ALTER TABLE assets ADD COLUMN concentration_min        NUMERIC(10, 4);
ALTER TABLE assets ADD COLUMN concentration_max        NUMERIC(10, 4);
ALTER TABLE assets ADD COLUMN concentration_usual      NUMERIC(10, 4);
ALTER TABLE assets ADD COLUMN concentration_unit       VARCHAR(20);
ALTER TABLE assets ADD COLUMN concentration_source     VARCHAR(100);
ALTER TABLE assets ADD COLUMN concentration_pharma_form VARCHAR(100);

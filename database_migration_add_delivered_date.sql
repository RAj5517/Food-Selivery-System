-- Migration script to add delivered_date column to order table
-- Run this script on your PostgreSQL database

ALTER TABLE "order" 
ADD COLUMN IF NOT EXISTS delivered_date TIMESTAMP;

-- Add index for better query performance on delivered_date
CREATE INDEX IF NOT EXISTS idx_order_delivered_date ON "order"(delivered_date);

-- Update existing delivered orders (optional - sets delivered_date to order_date for already delivered orders)
-- Uncomment the following line if you want to backfill data:
-- UPDATE "order" SET delivered_date = order_date WHERE status = 'DELIVERED' AND delivered_date IS NULL;


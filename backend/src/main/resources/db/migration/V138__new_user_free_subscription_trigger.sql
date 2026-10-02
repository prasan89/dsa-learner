CREATE OR REPLACE FUNCTION fn_create_free_lang_subscription()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO lang_subscriptions (user_id, plan_code, status)
    VALUES (NEW.id, 'FREE', 'ACTIVE')
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_new_user_free_sub
AFTER INSERT ON users
FOR EACH ROW EXECUTE FUNCTION fn_create_free_lang_subscription();

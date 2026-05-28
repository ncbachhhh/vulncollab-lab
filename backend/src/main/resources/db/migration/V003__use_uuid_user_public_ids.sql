UPDATE user_activities
SET resource_id = '11111111-1111-4111-8111-111111111111'
WHERE resource_type = 'USER'
  AND resource_id = 'usr-admin-000000000000000000000000001';

UPDATE user_activities
SET resource_id = '22222222-2222-4222-8222-222222222222'
WHERE resource_type = 'USER'
  AND resource_id = 'usr-alice-000000000000000000000000001';

UPDATE users
SET public_id = '11111111-1111-4111-8111-111111111111'
WHERE email = 'admin@test.com';

UPDATE users
SET public_id = '22222222-2222-4222-8222-222222222222'
WHERE email = 'alice@test.com';

UPDATE users
SET public_id = '33333333-3333-4333-8333-333333333333'
WHERE email = 'bob@test.com';

UPDATE users
SET public_id = '44444444-4444-4444-8444-444444444444'
WHERE email = 'charlie@test.com';

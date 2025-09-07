-- Users
INSERT INTO
    users (id, username, password, email, role, enabled)
VALUES
    (
        1,
        'admin',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'admin@example.com',
        'USER',
        true
    );

INSERT INTO
    users (id, username, password, email, role, enabled)
VALUES
    (
        2,
        'user',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'user@example.com',
        'USER',
        true
    );

-- Owners
INSERT INTO
    owner (id, name, email)
VALUES
    (1, 'Ana Pop', 'ana.pop@example.com');

INSERT INTO
    owner (id, name, email)
VALUES
    (2, 'Bogdan Ionescu', 'bogdan.ionescu@example.com');

-- Cars
INSERT INTO
    car (
        id,
        vin,
        make,
        model,
        year_of_manufacture,
        owner_id
    )
VALUES
    (1, 'VIN12345', 'Dacia', 'Logan', 2018, 1);

INSERT INTO
    car (
        id,
        vin,
        make,
        model,
        year_of_manufacture,
        owner_id
    )
VALUES
    (2, 'VIN67890', 'VW', 'Golf', 2021, 2);

-- Insurance Policies (fixing open-ended policy to have end date)
INSERT INTO
    insurancepolicy (id, car_id, provider, start_date, end_date)
VALUES
    (
        1,
        1,
        'Allianz',
        DATE '2024-01-01',
        DATE '2024-12-31'
    );

INSERT INTO
    insurancepolicy (id, car_id, provider, start_date, end_date)
VALUES
    (
        2,
        1,
        'Groupama',
        DATE '2025-01-01',
        DATE '2026-01-01'
    );

INSERT INTO
    insurancepolicy (id, car_id, provider, start_date, end_date)
VALUES
    (
        3,
        2,
        'Allianz',
        DATE '2025-03-01',
        DATE '2025-09-30'
    );

-- Sample Claims
INSERT INTO
    claim (
        id,
        car_id,
        claim_date,
        description,
        amount,
        created_at
    )
VALUES
    (
        1,
        1,
        DATE '2024-06-15',
        'Minor scratch on front bumper',
        500.00,
        TIMESTAMP '2024-06-15 10:30:00'
    );

INSERT INTO
    claim (
        id,
        car_id,
        claim_date,
        description,
        amount,
        created_at
    )
VALUES
    (
        2,
        2,
        DATE '2025-05-20',
        'Broken side mirror',
        300.00,
        TIMESTAMP '2025-05-20 14:15:00'
    );